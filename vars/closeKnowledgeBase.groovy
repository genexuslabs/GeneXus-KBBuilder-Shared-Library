/*
 * Job: closeKnowledgeBase
 *
 * Description:
 * This job closes a GeneXus Knowledge Base (KB), commonly used to force detachment from the local database.
 * It synchronizes the `cdxci.msbuild` file from the library resources, writes it to the workspace, and
 * executes the MSBuild script to close the specified Knowledge Base.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - msbuildExePath: The path to the MSBuild executable.
 *
 * Workflow Steps:
 * 1. Check if `localKBPath` is provided.
 * 2. Sync `cdxci.msbuild` from the library resources and write it to the workspace.
 * 3. Execute the MSBuild script to close the Knowledge Base.
 */

def call(Map args = [:]) {
    if(args.localKBPath != null) {
        // Sync cdxci.msbuild
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents
        
        bat label: "Close Knowledge Base",
            script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /t:CloseKB
            """
    }
}