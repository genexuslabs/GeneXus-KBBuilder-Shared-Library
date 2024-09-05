/**
 * Job: configureGxFlowDataStore
 *
 * Description:
 * This job configures the GXFlow Data Store for a GeneXus Knowledge Base (KB) by synchronizing
 * and applying the necessary settings and parameters. It uses an MSBuild script to configure
 * the data store with the specified database details.
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
 *   - msbuildExePath: The path to the MSBuild executable.
 *
 * Workflow Steps:
 * 1. Sync the properties.msbuild file from the library resources.
 * 2. Use the provided database server credentials to configure the GXFlow Data Store.
 * 3. Execute the MSBuild script to apply the configuration.
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