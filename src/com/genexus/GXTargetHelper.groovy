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
void configureNexusServer(Map args = [:]) {
    try{
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents
        withCredentials([
            usernamePassword(credentialsId: "${args.moduleServerCredentialsId}", passwordVariable: 'moduleServerPass', usernameVariable: 'moduleServerUser')
        ]) {        
            bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:ServerId="${args.moduleServerId}" \
                /p:ServerSource="${args.moduleServerSource}" \
                /p:ServerUsername="${moduleServerUser}" \
                /p:ServerPassword="${moduleServerPass}" \
                /t:AddNugetServer
            """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void deleteObjects(Map args = [:], String objects) {
    try{
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents

        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:ObjsToDelete="${args.objects}" \
            /t:DeleteLocalObject
        """
        
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
void deleteObjectsByCategory(Map args = [:], String category) {
    try{
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents

        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:ObjCategory="${category}" \
            /t:DeleteLocalObjectsByCategory
        """

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
