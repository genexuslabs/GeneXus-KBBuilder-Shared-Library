/*
 * Job: checkPendingReorg
 * 
 * Description:
 * This job executes the 'Reorganize' task with the 'FailIfReorg=true' flag. If the task fails, it 
 * indicates that there is a pending reorganization in the GeneXus Knowledge Base (KB).
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment.
 *   - msbuildExePath: The path to the MSBuild executable.
 *
 * Return Type:
 * - Boolean: Returns true if a pending reorganization is found, false otherwise.
 *
 * Workflow Steps:
 * 1. Sync cdxci.msbuild template from the library resource.
 * 2. Execute the 'Impact Analysis' task using MSBuild with the CheckReorgRequired target.
 * 3. Catch any errors to determine if there is a pending reorganization.
 * 4. Return true if a pending reorganization is found, false otherwise.
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    Boolean foundReorganization = false
    try {
        bat label: "Impact Analysis", 
            script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:environmentName="${args.environmentName}" \
                /t:CheckReorgRequired
            """
        echo "[INFO] NOT found pending reorganization"
    } catch (error) {
        echo "[INFO] Found pending reorganization"
        foundReorganization = true
    }
    return foundReorganization
}