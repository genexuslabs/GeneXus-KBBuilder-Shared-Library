/*
 * Job: BuildConfigurationEnvironment
 *
 * Description:
 * This job synchronizes the necessary MSBuild files from the library resources, avoids datastore connections,
 * and builds the entire configuration environment. It uses the provided paths and parameters to execute the 
 * necessary MSBuild tasks. 
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment to be used.
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - forceRebuild: Boolean flag indicating whether to force a rebuild.
 *
 * Workflow Steps:
 * 1. Synchronize `cdxci.msbuild` from the library resources to the workspace.
 * 2. Synchronize `properties.msbuild` from the library resources to the workspace.
 * 3. Avoid datastore connections by running `properties.msbuild` with the corresponding parameters.
 * 4. Build the entire configuration environment by running `cdxci.msbuild` with the corresponding parameters.
 */
 
def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents
    // Sync properties.msbuild
    fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
    writeFile file: 'properties.msbuild', text: fileContents

    bat label: 'Avoid Datastore connections', 
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /t:AvoidDatastoreConnections
        """

    bat label: 'Build all', 
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /p:rebuild="${args.forceRebuild}" \
            /t:BuildConfigurationEnv
        """
}