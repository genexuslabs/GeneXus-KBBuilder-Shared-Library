/*
 * Job updateInstalledModule >> This method run UpdateModule Task to update a specific module
 * -- >> Task documentation:: https://wiki.genexus.com/commwiki/wiki?46830,Modules+MSBuild+Tasks#UpdateModule+Task 
 *
 * @Param args = [:]
 * +- gxBasePath
 * +- localKBPath
 * +- propertiesFilePath
 * +- moduleName
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