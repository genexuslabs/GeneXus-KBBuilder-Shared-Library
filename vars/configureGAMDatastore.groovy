/*
 * Job: configureGamDataStore
 *
 * Description:
 * This job configures the GAM (GeneXus Access Manager) Data Store. It synchronizes the properties file
 * and sets up the data store with the provided database credentials and configuration details.
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
 * 1. Synchronize the properties.msbuild file from the template.
 * 2. Retrieve database credentials using the provided credentials ID.
 * 3. Execute the MSBuild script to configure the GAM Data Store with the given parameters.
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
            /p:dataStoreName="GAM" \
            /p:dbName="${args.dbName}" \
            /p:dbServerName="${args.dbServerName}" \
            /p:dbServerPort="${args.dbServerPort}" \
            /p:dbServerUser="${dbUsername}" \
            /p:dbServerPass="${dbPassword}" \
            /t:ConfigureDataStore
        """
    }
}