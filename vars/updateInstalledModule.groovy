/*
 * Job: updateInstalledModule
 *
 * Description:
 * This method runs the 'UpdateModule' task to update a specific module within the GeneXus installation.
 * It synchronizes and executes the `cdxci.msbuild` script with the provided parameters.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - moduleName: The name of the module to be updated.
 *   - msbuildExePath: The path to the MSBuild executable.
 *
 * Workflow Steps:
 * 1. Sync the `cdxci.msbuild` file from the library resource.
 * 2. Write the `cdxci.msbuild` file to the workspace.
 * 3. Run the MSBuild executable with the parameters to update the specified module.
 *
 */
def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    bat label: "Update module::${args.moduleName}", 
    script: """
        "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
        /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
        /p:localKbPath="${args.localKBPath}" \
        /p:moduleName="${args.moduleName}" \
        /t:UpdateInstalledModule
    """
}