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
 * Renames and copies SQL reorganization script to a specified destination directory with a timestamp.
 * The script is renamed following the pattern: V[timestamp]___[projectName]_ReorganizationScript_[BUILD_NUMBER].sql
 *
 * @param args A map containing the following parameters:
 *   - sqlScriptDestinationDirectory: The destination directory where the SQL script will be copied.
 *   - projectName: The name of the project to be included in the renamed file.
 *   - reorgExportPath: The source directory containing the original reorganization script.
 *   - BUILD_NUMBER: (Environment variable) The current build number used in the file naming.
 *
 * The method uses PowerShell to:
 *   1. Generate a timestamp in format "yyyy.MM.dd.HHmmss"
 *   2. Construct the destination path with the new file name
 *   3. Copy the original SQL script to the destination if the destination directory exists
 */
void renameAndCopySQLScript(Map args = [:]) {
    powershell script: """
        \$ErrorActionPreference = "Stop"
        \$currentDate = Get-Date -Format \"yyyy.MM.dd.HHmmss\" 

        \$destinationPath = "${args.sqlScriptDestinationDirectory}\\V\$currentDate`___${args.projectName}_ReorganizationScript_${env.BUILD_NUMBER}.sql"
        
        \$originalSQLPath = "${args.reorgExportPath}\\${env.BUILD_NUMBER}_ReorganizationScript.txt"
                                
        if (Test-Path -Path ${args.sqlScriptDestinationDirectory}) {
            Copy-Item -Path "\$originalSQLPath" -Destination "\$destinationPath" -Force
        }
    """
}

return this
