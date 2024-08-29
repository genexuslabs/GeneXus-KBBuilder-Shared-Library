/*
 * Job: configureDataStore
 *
 * Description:
 * This job configures a single Data Store within a specified GeneXus environment. It synchronizes
 * the properties.msbuild file and uses the provided credentials to set up the database connection
 * for the Data Store.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment.
 *   - dataStoreName: The name of the data store to configure.
 *   - dbName: The name of the database.
 *   - dbServerName: The name of the database server.
 *   - dbServerPort: The port of the database server.
 *   - dbServerCredentialsId: The credentials ID containing the username and password for the database server.
 *
 * Workflow Steps:
 * 1. Sync properties.msbuild with the template file from the library resources.
 * 2. Retrieve database server credentials using the provided credentials ID.
 * 3. Execute msbuild with the required parameters to configure the Data Store.
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
            /p:dataStoreName="${args.dataStoreName}" \
            /p:dbName="${args.dbName}" \
            /p:dbServerName="${args.dbServerName}" \
            /p:dbServerPort="${args.dbServerPort}" \
            /p:dbServerUser="${dbUsername}" \
            /p:dbServerPass="${dbPassword}" \
            /t:ConfigureDataStore
        """
    }
}