package com.genexus

/**
 * Configures module server settings for the GeneXus environment.
 * -- >> For detailed information on the task, refer to the documentation: https://wiki.genexus.com/commwiki/wiki?46830,Modules+MSBuild+Tasks#AddModulesServer+Task
 *
 * @param args A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - moduleServerId: Name or URL of the module server.
 *   - moduleServerSource: module server source URL.
 *   - moduleServerCredentialsId: Jenkins credentials ID for module server authentication.
 *
 */
void syncFlywayPackages(Map args = [:]) {
    try{
        // download repo
        // 
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents
        if(args.moduleServerCredentialsId) {
            withCredentials([
                usernamePassword(credentialsId: "${args.moduleServerCredentialsId}", passwordVariable: 'moduleServerPass', usernameVariable: 'moduleServerUser')
            ]) {        
                bat script: """
                    "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                    /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                    /p:ServerId="${args.moduleServerId}" \
                    /p:ServerSource="${args.moduleServerSource}" \
                    /p:ServerType="${args.moduleServerType}" \
                    /p:ServerUsername="${moduleServerUser}" \
                    /p:ServerPassword="${moduleServerPass}" \
                    /t:AddModuleServer
                """
            }
        } else {
            bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:ServerId="${args.moduleServerId}" \
                /p:ServerSource="${args.moduleServerSource}" \
                /p:ServerType="${args.moduleServerType}" \
                /t:AddModuleServer
            """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Renames and copies an SQL reorganization script to a specified destination directory within an IaC repository.
 * The script is renamed following the pattern: V[timestamp]___[flywayApplicationName]_ReorganizationScript_[BUILD_NUMBER].txt
 * If the destination directory does not exist, it is created, and the script is copied with a different naming pattern:
 * V[timestamp]___[baseApplicationName]_Schema_Initial.txt.
 *
 * @param args A map containing the following parameters:
 *   - iacRepoLocalPath: The local path to the Infrastructure as Code (IaC) repository.
 *   - flywayRepoDestination: The destination directory within the IaC repository where the SQL script will be copied.
 *   - flywayApplicationName: The name of the application to be included in the renamed file.
 *   - baseApplicationName: The base name of the application to be used if the destination directory does not exist.
 *   - reorgExportPath: The source directory containing the original reorganization script.
 *
 * The method uses PowerShell to:
 *   1. Generate a timestamp in the format "yyyy.MM.dd.HHmmss".
 *   2. Construct the destination path with the new file name based on whether the destination directory exists.
 *   3. Copy the original SQL script to the destination directory with the appropriate naming pattern.
 *   4. Create the destination directory if it does not exist and copy the script with the initial naming pattern.
 *
 * @throws Exception if any error occurs during the execution of the PowerShell script.
 */
void addMigrationToIacRepository(Map args = [:]) {
    try {
        powershell script: """
            \$currentDate = Get-Date -Format \"yyyy.MM.dd.HHmmss\" 
            \$flywaySQLPath = "${WORKSPACE}\\${args.iacRepoLocalPath}\\${args.flywayRepoDestination}\\V\$currentDate`___${args.flywayApplicationName}_ReorganizationScript_${env.BUILD_NUMBER}.txt"
            \$flywayUpdateSQLPath = "${WORKSPACE}\\${args.iacRepoLocalPath}\\${args.flywayRepoDestination}\\"
            \$originalPath = "${args.reorgExportPath}\\${env.BUILD_NUMBER}_ReorganizationScript.txt"
            
            if (Test-Path -Path \$flywayUpdateSQLPath) {
                Write-Output((Get-Date -Format G) + " [INFO] Sync update reorganizationScript (#${env.BUILD_NUMBER})")
                Write-Output((Get-Date -Format G) + " [DEBUG] Copy -Path \$originalPath -Destination \$flywaySQLPath")
                Copy-Item -Path "\$originalPath" -Destination "\$flywaySQLPath" -Force
            }
            else {
                Write-Output((Get-Date -Format G) + " [INFO] Sync initial reorganizationScript (#${env.BUILD_NUMBER})")
                \$parentDirectory = Split-Path -Path \$flywayUpdateSQLPath -Parent
                Write-Output((Get-Date -Format G) + " [DEBUG] Calculated parent dir: \$parentDirectory")
                \$flywayCreationSQLPath = "\$parentDirectory\\V\$currentDate`___${args.baseApplicationName}_Schema_Initial.txt"
                Write-Output((Get-Date -Format G) + " [DEBUG] Create dir: \$flywayCreationSQLPath")
                New-Item -Path "\$flywayUpdateSQLPath" -ItemType Directory -Force | Out-Null
                Write-Output((Get-Date -Format G) + " [DEBUG] -Path \$originalPath -Destination \$flywayCreationSQLPath")
                Copy-Item -Path "\$originalPath" -Destination "\$flywayCreationSQLPath" -Force
            }
        """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Renames and copies an SQL reorganization script to a specified destination directory with a timestamp.
 * The script is renamed following the pattern: V[timestamp]___[baseApplicationName]_ReorganizationScript_[BUILD_NUMBER].txt
 * If the destination directory does not exist, it is created, and the script is copied with a different naming pattern.
 *
 * @param args A map containing the following parameters:
 *   - sqlScriptDestinationDirectory: The destination directory where the SQL script will be copied. (Not directly used in the script)
 *   - projectName: The name of the project to be included in the renamed file. (Not directly used in the script)
 *   - reorgExportPath: The source directory containing the original reorganization script. (Not directly used in the script)
 *
 * The method uses PowerShell to:
 *   1. Generate a timestamp in the format "yyyy.MM.dd.HHmmss".
 *   2. Construct the destination path with the new file name.
 *   3. Copy the original SQL script to the destination if the destination directory exists.
 *   4. If the destination directory does not exist, it creates the directory and copies the script with a different naming pattern.
 *
 * @throws Exception if any error occurs during the execution of the PowerShell script.
 */
void renameAndCopySQLScript(Map args = [:]) {
    try {
        powershell script: """
            \$currentDate = Get-Date -Format \"yyyy.MM.dd.HHmmss\" 
            \$flywaySQLPath = "${WORKSPACE}\\${args.iacRepoLocalPath}\\${args.flywayRepoDestination}\\V\$currentDate`___${args.baseApplicationName}_ReorganizationScript_${env.BUILD_NUMBER}.txt"
            \$flywayUpdateSQLPath = "${WORKSPACE}\\${args.iacRepoLocalPath}\\${args.flywayRepoDestination}\\"

            \$originalPath = "${args.reorgExportPath}\\${env.BUILD_NUMBER}_ReorganizationScript.txt"
            
            if (Test-Path -Path \$flywayUpdateSQLPath) {
                Copy-Item -Path "\$originalPath" -Destination "\$flywaySQLPath" -Force
            }
            else {
                \$parentDirectory = Split-Path -Path \$flywayUpdateSQLPath -Parent
                \$flywayCreationSQLPath = "\$parentDirectory\\V\$currentDate`___${args.baseApplicationName}_CreationScript.txt"
                New-Item -Path "\$flywayUpdateSQLPath" -ItemType Directory -Force
                Copy-Item -Path "\$originalPath" -Destination "\$flywayCreationSQLPath" -Force
            }
        """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
