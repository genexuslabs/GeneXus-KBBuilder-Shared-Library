/*
 * Job checkPendingReorg >> This methos executes the 'Reorganize' task with the 'FailIfReorg=true' flag. If the task fails, it indicates that there is a pending reorganization
 *
 * @Param args = [:]
 * +- gxBasePath
 * +- localKBPath
 * +- environmentName
 *
 * @Return Boolean
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
        echo "NOT found pending reorganization"
    } catch (error) {
        echo "Found pending reorganization"
        foundReorganization = true
    }
    return foundReorganization
}