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
                \$auxDestinationZip = \"${destinationZip}\\*\"
                if(Test-Path -Path \$auxDestinationZip) { Remove-Item -Path \$auxDestinationZip}
                if (Get-Command 7z -ErrorAction SilentlyContinue) {
                    Write-Output "7z command found. Using 7z to compress the directory."
                    & 7z a -tzip \$auxDestinationZip ${sourceDir}
                } else {
                    Write-Output "7z command not found. Using Compress-Archive to compress the directory."
                    Compress-Archive -Path ${sourceDir} -DestinationPath \$auxDestinationZip
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

return this