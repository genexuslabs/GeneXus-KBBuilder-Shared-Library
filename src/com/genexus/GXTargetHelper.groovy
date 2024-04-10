package com.genexus

/**
 * Configures NuGet server settings for the GeneXus environment.
 * -- >> For detailed information on the task, refer to the documentation: https://wiki.genexus.com/commwiki/wiki?46830,Modules+MSBuild+Tasks#AddModulesServer+Task
 *
 * @param args A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - nugetServerCredentialsId: Jenkins credentials ID for NuGet server authentication.
 *   - nugetServerName: Name or URL of the NuGet server.
 *   - nugetServerSource: NuGet server source URL.
 *
 */
void configureNugetServer(Map args = [:]) {
    try{
        if (!fileExists("${WORKSPACE}\\cdxci.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
            writeFile file: 'cdxci.msbuild', text: fileContents
        }
        withCredentials([
            usernamePassword(credentialsId: "${args.nugetServerCredentialsId}", passwordVariable: 'nugetServerPass', usernameVariable: 'nugetServerUser')
        ]) {        
            bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:ServerName="${args.nugetServerName}" \
                /p:ServerSource="${args.nugetServerSource}" \
                /p:ServerUsername="${nugetServerUser}" \
                /p:ServerPassword="${nugetServerPass}" \
                /t:AddNugetServer
            """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
