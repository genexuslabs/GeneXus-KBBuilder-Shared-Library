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


return this
