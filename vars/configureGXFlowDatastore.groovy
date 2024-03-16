/**
 * Job: configureDataStore >> This job configures the GXFlow Data Store.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment.
 *   - dbName: The name of the database.
 *   - dbServerName: The name of the database server.
 *   - dbServerPort: The port of the database server.
 *   - dbServerCredentialsId: The credentials ID containing the username and password for the database server.
 *
 */

def call(Map args = [:]) {
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
    writeFile file: 'properties.msbuild', text: fileContents

    withCredentials([usernamePassword(credentialsId: args.dbServerCredentialsId, usernameVariable: 'dbUsername', passwordVariable: 'dbPassword')]) {
        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /p:dataStoreName="GXFlow" \
            /p:dbName="${args.dbName}" \
            /p:dbServerName="${args.dbServerName}" \
            /p:dbServerPort="${args.dbServerPort}" \
            /p:dbServerUser="${dbUsername}" \
            /p:dbServerPass="${dbPassword}" \
            /t:ConfigureDataStore
        """
    }
}