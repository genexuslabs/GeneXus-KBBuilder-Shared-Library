/*
 * Job: applyReorganization
 *
 * Description:
 * This job executes the 'Reorganize' task to apply database reorganization in the specified environment.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment.
 *   - msbuildExePath: The path to the MSBuild executable.
 *
 * Prerequisites:
 * - Configure database connection in Datasources.
 *
 * Workflow Steps:
 * 1. Sync cdxci.msbuild template file.
 * 2. Write cdxci.msbuild content to the workspace.
 * 3. Execute MSBuild with the 'ApplyReorg' target using the provided parameters.
 *
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    bat label: "Apply reorganization for env::${args.environmentName}", 
    script: """
        "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
        /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
        /p:localKbPath="${args.localKBPath}" \
        /p:environmentName="${args.environmentName}" \
        /t:ApplyReorg
    """
}


