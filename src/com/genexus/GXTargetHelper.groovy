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

/**
 * Configura la configuración del servidor Maven para el entorno de GeneXus.
 * -- >> Para obtener información detallada sobre la tarea, consulta la documentación: https://wiki.genexus.com/commwiki/wiki?46830,Modules+MSBuild+Tasks#AddModulesServer+Task
 *
 * @param args Un mapa que contiene los siguientes parámetros:
 *   - gxBasePath: La ruta base de la instalación de GeneXus.
 *   - mavenServerCredentialsId: ID de credenciales de Jenkins para la autenticación del servidor Maven.
 *   - mavenServerUrl: URL del servidor Maven.
 *   - mavenSettingsPath: Ruta del archivo de configuración de Maven (settings.xml).
 *
 */
void configureMavenServer(Map args = [:]) {
    try {
        if (!fileExists("${WORKSPACE}\\cdxci.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
            writeFile file: 'cdxci.msbuild', text: fileContents
        }
        withCredentials([
            usernamePassword(credentialsId: "${args.mavenServerCredentialsId}", passwordVariable: 'mavenServerPass', usernameVariable: 'mavenServerUser')
        ]) {
            bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:ServerName="${args.mavenServerName}" \
                /p:ServerSource="${args.mavenServerSource}" \
                /p:ServerUsername="${mavenServerUser}" \
                /p:ServerPassword="${mavenServerPass}" \
                /t:AddMavenServer
            """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
