/*
 * Job: importXPZ
 *
 * Description:
 * This job imports a group of objects into a GeneXus Knowledge Base (KB) from an XPZ file.
 * It synchronizes the `import.msbuild` file from the library resources, writes it to the workspace,
 * and executes the MSBuild script to import the specified XPZ file into the Knowledge Base.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - xpzFilePath: The path to the XPZ file to be imported.
 *
 * Workflow Steps:
 * 1. Check if `localKBPath` and `xpzFilePath` are provided.
 * 2. Sync `import.msbuild` from the library resources and write it to the workspace.
 * 3. Execute the MSBuild script to import the XPZ file into the Knowledge Base.
 */

def call(Map args = [:]) {
        // Sync cdxci.msbuild
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents
        
        bat label: "Import XPZ",
            script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:xpzFilePath="${args.xpzFilePath}" \
                /t:importXPZ
            """
    }
}