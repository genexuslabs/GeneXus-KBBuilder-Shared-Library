 /*
 * Job: BuildInstallationEnvironment
 *
 * Description:
 * This job synchronizes the `cdxci.msbuild` file from the library resource and builds the project using
 * MSBuild. It reads the necessary parameters from the `args` map and executes the build all task, specifying
 * the environment and rebuild options.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment to be used.
 *   - forceRebuild: Boolean flag indicating whether to force a rebuild of the project.
 *
 * Workflow Steps:
 * 1. Sync the `cdxci.msbuild` file from the library resource.
 * 2. Write the file to the workspace.
 * 3. Execute the MSBuild process with the specified parameters and targets.
 */
 
def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

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