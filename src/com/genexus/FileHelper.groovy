package com.genexus

/**
 * This method combines two path components to create a joined path.
 *
 * @param a The first path component.
 * @param b The second path component.
 * @return The combined path resulting from joining the two input path components.
 */
String joinPath(String a, String b) {
    try {
        def joinResult = powershell script: """
            Join-Path \"${a}\" \"${b}\"
            #if(!(Test-Path -Path \$ret)) { Write-Error -Message \"joinPath fail when Test-Path\"}
            #Write-Host \$ret
        """, returnStdout: true
        return "${joinResult.trim()}"
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method returns the absolute path from a given relative or absolute path.
 *
 * @param auxPath The path, either relative or absolute, for which to obtain the absolute path.
 * @return The absolute path corresponding to the provided path.
 */
String getFullPath(String auxPath) {
    try {
        def absolutePath = powershell script: "[System.IO.Path]::GetFullPath(\"${auxPath}\")", returnStdout: true
        return absolutePath.trim()
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method returns the absolute path from a relative path within the Jenkins workspace.
 *
 * @param relativePath The relative path within the Jenkins workspace.
 *                    If the path does not exist, the method creates the necessary directories.
 * @return The absolute path corresponding to the provided relative path within the Jenkins workspace.
 */
String getAbsolutePathFromWS(String relativePath) {
    try {
        String auxPath = env.WORKSPACE + relativePath
        def absolutePath = powershell script: """
            if(!(Test-Path -Path \"${auxPath}\")) {\$null = New-Item -Path \"${auxPath}\" -ItemType Directory}
            Set-Location -Path \"${auxPath}\"
            (Get-Location).Path
        """, returnStdout: true
        return absolutePath.trim()
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}


def searchKeywordInMessages(messages, keyword) {
    def keywordFound = false 

    messages.each { message ->
        if (message.contains(keyword)) {
            keywordFound = true
        }
    }

    return keywordFound
}


/**
 * Compresses a directory using 7-Zip if available, otherwise uses PowerShell's Compress-Archive.
 *
 * @param sourceDir The full path to the directory to be compressed.
 * @param destinationZip The path where the resulting zip file will be saved.
 * @throws Exception if an error occurs during the compression process.
 */
void winCompressDirectory(String sourceDir, String destinationZip) {
    try {
        powershell label: "Compress Directory",
            script: """
                if(Test-Path -Path "${destinationZip}") { Remove-Item -Path "${destinationZip}"}
                if (Get-Command 7z -ErrorAction SilentlyContinue) {
                    Write-Output "7z command found. Using 7z to compress the directory."
                    & 7z a -tzip "${destinationZip}" "${sourceDir}"
                } else {
                    Write-Output "7z command not found. Using Compress-Archive to compress the directory."
                    Compress-Archive -Path "${sourceDir}" -DestinationPath "${destinationZip}"
                }
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}


/**
 * Removes a directory and its contents if it exists.
 *
 * @param dirPath The full path to the directory to be removed.
 * @throws Exception if an error occurs during the removal process.
 */
void removeDirectoryPath(String dirPath) {
    try {
        // Check if the path is not null or empty before proceeding
        if (dirPath == null || dirPath.isEmpty()) {
            throw new IllegalArgumentException("The provided path is null or empty.");
        }

        powershell label: "Remove path: ${dirPath}",
            script: """
                if (Test-Path -Path '${dirPath}') {
                    Remove-Item -Path '${dirPath}' -Recurse -Force
                    Write-Output "Directory removed successfully."
                } else {
                    Write-Output "Directory does not exist: ${dirPath}"
                }
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Updates files at the target path by downloading and extracting a ZIP archive from a specified URI.
 *
 * This method retrieves a PowerShell script from the shared library, executes it to update files
 * at the specified target path using a ZIP file from the provided source URI. It also handles the exit code 
 * from the robocopy command to determine if the file copying was successful.
 *
 * @param sourceUri The URI of the ZIP archive to download and extract. This parameter is mandatory.
 * @param sourceFolder The name of the folder within the ZIP archive to copy. Optional; if not provided, 
 *                     the entire contents of the ZIP archive will be copied.
 * @param targetPath The path where the extracted files will be copied. This parameter is mandatory.
 *
 * @throws Exception if there is an error during script execution or file operations.
 *
 * @example
 * // Update files in the target path from a ZIP archive at the specified URI.
 * updateFromZip("https://example.com/archive.zip", null, "C:\\Program Files\\MyApp");
 */
void updateFromZip(String sourceUri, String sourceFolder, String targetPath) {
    try {
        // Load the PowerShell script from the shared library
        fileContents = libraryResource 'com/genexus/pwshScripts/common/update-from-zip.ps1'
        writeFile file: 'update-from-zip.ps1', text: fileContents

        try {
            // Execute the PowerShell script and capture the exit code
            def exitCode = powershell(script: ".\\update-from-zip.ps1 -SourceUri:'${sourceUri}' -SourceFolder:'${sourceFolder}' -TargetPath:'${targetPath}'", returnStatus: true)

            // Check the exit code of the robocopy command
            if (exitCode >= 0 && exitCode < 8) {
                echo "ROBOCOPY completed successfully with exit code: ${exitCode}"
            } else {
                echo "ROBOCOPY encountered an error. Exit code: ${exitCode}"
                // Optionally, you can throw an error or handle it as required
                throw new Exception("ROBOCOPY failed with exit code: ${exitCode}")
            }
        } catch (e) {
            echo "Error executing PowerShell script: ${e.getMessage()}"
            throw e
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Updates files at the target path by downloading and extracting a ZIP archive from an AWS S3 bucket.
 *
 * This method retrieves a PowerShell script from the shared library, executes it to update files
 * at the specified target path using a ZIP file from an S3 URI. It also handles the exit code 
 * from the robocopy command to determine if the file copying was successful.
 *
 * @param sourceUri The S3 URI of the ZIP archive to download and extract. This parameter is mandatory.
 * @param sourceFolder The name of the folder within the ZIP archive to copy. Optional; if not provided, 
 *                     the entire contents of the ZIP archive will be copied.
 * @param targetPath The path where the extracted files will be copied. This parameter is mandatory.
 *
 * @throws Exception if there is an error during script execution or file operations.
 *
 * @example
 * // Update files in the target path from a ZIP archive in an S3 bucket.
 * updateFromS3("s3://example-bucket/archive.zip", null, "C:\\Program Files\\MyApp");
 */
void updateFromS3(String sourceUri, String sourceFolder, String targetPath) {
    try {
        // Load the PowerShell script from the shared library
        fileContents = libraryResource 'com/genexus/pwshScripts/common/update-from-s3.ps1'
        writeFile file: 'update-from-s3.ps1', text: fileContents

        try {
            // Execute the PowerShell script and capture the exit code
            def exitCode = powershell(script: ".\\update-from-s3.ps1 -SourceUri:'${sourceUri}' -SourceFolder:'${sourceFolder}' -TargetPath:'${targetPath}'", returnStatus: true)

            // Check the exit code of the robocopy command
            if (exitCode >= 0 && exitCode < 8) {
                echo "ROBOCOPY completed successfully with exit code: ${exitCode}"
            } else {
                echo "ROBOCOPY encountered an error. Exit code: ${exitCode}"
                // Optionally, you can throw an error or handle it as required
                throw new Exception("ROBOCOPY failed with exit code: ${exitCode}")
            }
        } catch (e) {
            echo "Error executing PowerShell script: ${e.getMessage()}"
            throw e
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

String standarizeVersion(String version, String label, int position){
    def standarizedVersion = powershell label: "Add Label to Given Position",
            script: """
                \$auxVersionParts = "${version}"
                \$versionParts = \$auxVersionParts.Split('.')
                \$versionParts[${position}] = "\$(\$auxVersionParts[${position}])-${label}"
                \$modifiedVersion = \$versionParts -join '.'
                Write-Output \$modifiedVersion
            """, returnStdout: true
    return standarizedVersion.trim()
}

/**
 * Constructs a Semantic Versioning (SemVer) expression based on the provided version string.
 * The major and minor values are derived from the given version, while the build number 
 * serves as the patch version component. An optional label can be specified to indicate 
 * the development stage, such as 'beta', 'preview', or any other identifier. An optional 
 * int argument can be provided to increase the major version with an offset, useful when the
 * version cannot handle a label and there is a need to generate different versions for different
 * channels (g.e. Beta and Preview).
 *
 * @param {string} version - The version string in the format "major.minor.patch" (e.g., "1.3.5").
 * @param {string} buildNumber - The build number to be used as the patch version (e.g., "88").
 * @param {string} [label] - An optional label to denote the version stage (e.g., "beta"). 
 *                           If provided, it will be appended to the version string.
 * @param {int} [majorOffset] - An optional value to add to the major version.
 *
 * @throws {Error} Throws an error if the provided version string is not in a valid SemVer format 
 *                 (digits separated by dots).
 *
 * @returns {string} The constructed SemVer expression.
 *
 * @example
 * // Standard version without a label
 * const version1 = standarizeVersionForSemVer("1.3.5", "88", ""); // Returns "1.3.88"
 *
 * // Version with a 'beta' label but without increasing major version
 * const version2 = standarizeVersionForSemVer("2.1.5", "457", "beta", 100); // Returns "2.1.0-beta.457"
 *
 * // Increase major version by 100 when no label is defined
 * const version3 = standarizeVersionForSemVer("1.3.5", "88", "", 100); // Returns "101.3.88"
 */
String standarizeVersionForSemVer(String version, String buildNumber, String label, int majorOffset = 0) {
    def sanitizedLabel = label == null ? "" : label // Ensure label is an empty string if null
    def standarizedVersion = powershell label: "Define a SemVer expression with the BuildNumber",
            script: """
                \$versionParts = "${version}".Split('.')
                \$majorVersion = [int]\$versionParts[0]
                if ([string]::IsNullOrEmpty("${sanitizedLabel}")) {
                    \$majorVersion += ${majorOffset}
                }
                \$minorVersion = [int]\$versionParts[1]
                if (-not [string]::IsNullOrEmpty("${sanitizedLabel}")) {
                    \$buildVer = "0-${sanitizedLabel}.${buildNumber}"
                }
                else {
                    \$buildVer = "${buildNumber}"
                }
                \$versionList = @(\$majorVersion, \$minorVersion, \$buildVer)
                \$standarizedVersion = \$versionList -join "."
                Write-Output \$standarizedVersion
            """, returnStdout: true
    
    return standarizedVersion.trim()
}

/**
 * Constructs a standardized semantic version expression with four components:
 * major, minor, revision, and patch (build number). The major, minor, and revision 
 * values are derived from the provided version string, while the build number 
 * serves as the patch version component. An optional offset can be added to the 
 * build number, allowing for versioning flexibility.
 *
 * @param {string} version - The version string in the format "major.minor.revision" 
 *                           (e.g., "1.3.5"). If the revision is omitted, it defaults to "0".
 * @param {string} buildNumber - The base build number to be used as the patch version 
 *                               (e.g., "88"). It must be a numeric string.
 * @param {int} offset - An integer value that will be added to the build number. 
 *                       This allows for versioning adjustments (e.g., "100").
 *
 * @throws {Error} Throws an error if the provided version string does not conform 
 *                 to the expected format of "major.minor.revision" or is invalid 
 *                 in any way (e.g., non-numeric values).
 *
 * @returns {string} The constructed version expression in the format 
 *                   "major.minor.revision.build" (e.g., "1.3.5.88").
 *
 * @example
 * // Standard version without an offset
 * const version1 = getFourDigitVersion("1.3.5", "88", 0); // Returns "1.3.5.88"
 *
 * // Version with minor and no revision, using an offset
 * const version2 = getFourDigitVersion("2.1", "10", 100); // Returns "2.1.0.110"
 *
 * // Version with an existing revision and a build number offset
 * const version3 = getFourDigitVersion("3.4.2", "50", 25); // Returns "3.4.2.75"
 */
String getFourDigitVersion(String version, String buildNumber, String buildOffset){
    def standarizedVersion = powershell label: "Define a four digit version with the BuildNumber",
            script: """
                \$versionParts = "${version}".Split('.')

                \$majorNumber = @("1", \$versionParts[0])[-not [string]::IsNullOrEmpty(\$versionParts[0])]
                \$minorNumber = @("0", \$versionParts[1])[\$versionParts.Length -gt 1]
                \$revisionNumber = @("0", \$versionParts[2])[\$versionParts.Length -gt 2]
                \$buildVer = [int]\$buildNumber
                if (-not [string]::IsNullOrEmpty("${buildOffset}")) {
                    \$buildVer += [int]${buildOffset}
                }
                \$standarizedVersion = @(\$majorNumber, \$minorNumber, \$revisionNumber, \$buildVer) -join "."
                Write-Output \$standarizedVersion
            """, returnStdout: true
    return standarizedVersion.trim()
}

/**
 * Archives a specified file from a given file path. If an artifact name is provided, the file is copied
 * to the new name before archiving. If no artifact name is provided, the original file name is used for archiving.
 *
 * @param {String} filePath - The full path to the file that needs to be archived.
 * @param {String} [artifactName=null] - Optional. The name to use for the archived artifact. If not provided,
 *                                       the original file name is used.
 *
 * The function performs the following steps:
 * 1. Extracts the parent directory and file name from the provided file path using PowerShell commands.
 * 2. Changes the working directory to the parent directory of the file.
 * 3. If an artifact name is provided, copies the file to the new name.
 * 4. Archives the file (or the newly named file) using the Jenkins `archiveArtifacts` plugin.
 * 5. If any error occurs during these operations, the build result is set to 'FAILURE' and the error is thrown.
 */
void archiveArtifact(String filePath, String artifactName = null) {
    try {
        def parentDirPath = powershell script: "Split-Path \"${projectDefinition.targetPath}\" -Parent", returnStdout: true
        def fileName = powershell script: "Split-Path \"${projectDefinition.targetPath}\" -Leaf", returnStdout: true
        def artifactName = null
        dir(parentDirPath.trim()) {
            if (artifactName != null) {
                powershell script: """Copy-Item -Path "${fileName.trim()}" -Destination ${artifactName.trim()}"""
            } else {
                artifactName = fileName.trim()
            }
            echo "INFO: artifactName = ${artifactName}"
            archiveArtifacts artifacts: artifactName, followSymlinks: false
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
