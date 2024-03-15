/*
 * Job restoreInstalledModule >> This method runs the 'RestoreModule' task to restore a specific module.
 * -- >> For detailed information on the task, refer to the documentation: https://wiki.genexus.com/commwiki/wiki?46830,Modules+MSBuild+Tasks#RestoreModule+Task
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - moduleName: The name of the module to be restored.
 *
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    bat label: "Restore module::${args.moduleName}", 
    script: """
        "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
        /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
        /p:localKbPath="${args.localKBPath}" \
        /p:moduleName="${args.moduleName}" \
        /t:RestoreInstalledModule
    """
}