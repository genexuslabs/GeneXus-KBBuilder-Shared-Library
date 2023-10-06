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
    String target = " /t:CheckReorgRequired"
    String msbuildGenArgs = ''
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "GX_PROGRAM_DIR", args.localGXPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "localKbPath", args.localKBPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "EnvironmentName", args.environmentName)
    try {
        bat label: "Impact Analysis", 
            script: "\"${args.msbuildExePath}\" .\\cdxci.msbuild ${target} ${msbuildGenArgs} /nologo "
        echo "NOT found pending reorganization"
    } catch (error) {
        echo "Found pending reorganization"
        foundReorganization = true
    }
    return foundReorganization
}