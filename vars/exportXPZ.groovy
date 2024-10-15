/*
 * Job: ExportXPZ
 *
 * Description:
 * This job Exports a group of objects into a GeneXus Knowledge Base (KB) from an XPZ file.
 * It synchronizes the `cdxci.msbuild` file from the library resources, writes it to the workspace,
 * and executes the MSBuild script to Export the specified XPZ file into the Knowledge Base.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - xpzFilePath: The path to the XPZ file to be Exported.
 *   - ObjectList: Objects to be Exported.
 *
 * Workflow Steps:
 * 1. Sync `cdxci.msbuild` from the library resources and write it to the workspace.
 * 2. Execute the MSBuild script to export the XPZ file from the Knowledge Base.
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents
    
    bat label: "Export XPZ",
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:xpzFilePath="${args.xpzFilePath}" \
            /p:ObjectList="${args.objectList}" \
            /t:ExportXPZ
        """
}