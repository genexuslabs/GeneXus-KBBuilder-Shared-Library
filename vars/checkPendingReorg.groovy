/*
 * Job checkPendingReorg >> Read properties from environment
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

    Boolean foundReorganization = false
    try {
        bat label: "Impact Analysis", 
            script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.localGXPath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:environmentName="${args.environmentName}" \
                /t:CheckReorgRequired
            """
        echo "NOT found pending reorganization"
    } catch (error) {
        echo "Found pending reorganization"
        foundReorganization = true
    }
    return foundReorganization
}