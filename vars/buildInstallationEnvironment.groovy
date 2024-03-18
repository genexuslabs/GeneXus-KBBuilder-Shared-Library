/*
 * Job buildConfigurationEnvironment >> This method executes the 'BuildAll' task after configuring specific properties to customize the build environment.
 *
 * Custom Configuration:
 * - "Keep GAM database updated" = false
 * - "Deploy business processes on build" = No
 * - "Populate Data" = false
 * - "Reorganize server tables" = No
 * - "Deploy to cloud" = No
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment.
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - forceRebuild: A boolean indicating whether to force a rebuild.
 *
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