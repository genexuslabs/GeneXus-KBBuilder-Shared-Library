/*
 * Job: reorganizeDatabase
 *
 * Description:
 * This job reads properties from the environment, performs a database reorganization, and updates the
 * installation model. It uses a predefined MSBuild template to execute the necessary tasks.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment to be used.
 *   - msbuildExePath: The path to the MSBuild executable.
 *
 * Workflow Steps:
 * 1. Load the MSBuild template from the library resources.
 * 2. Write the template to a file named 'cdxci.msbuild' in the workspace.
 * 3. Execute the MSBuild script to update the installation model.
 * 4. In case of error, mark the build as FAILURE and log the error message.
 */
 
def call(Map args = [:]) {
    try {
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents

        bat label: "MarkDBReorganized", 
            script: """ 
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:EnvironmentName="${args.environmentName}" \
                /t:UpdateInstallationModel
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo " ERROR ${error.getMessage()}"
        throw error
    }
}