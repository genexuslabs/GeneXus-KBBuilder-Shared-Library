/*
 * Job reorganizeDatabase >> Read properties from environment
 *
 * @Param args = [:]
 * +- localGXPath
 * +- localKBPath
 * +- environmentName
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:]) {
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents
    
    bat label: "Apply Reorganization",
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.localGXPath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /t:ApplyReorg
        """
}