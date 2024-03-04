/*
 * Job applyReorganization >> This method executes the task 'Reorganize'
 *
 * @Param args = [:]
 * +- gxBasePath
 * +- localKBPath
 * +- environmentName
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
        /p:moduleName="${args.moduleName}" \
        /t:ApplyReorg
    """
}