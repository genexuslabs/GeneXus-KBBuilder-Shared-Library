/*
 * Job reorganizeDatabase >> Read properties from environment
 *
 * @Param args = [:]
 * +- gxBasePath
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
    String target = " /t:ApplyReorg"
    String msbuildGenArgs = ''
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "GX_PROGRAM_DIR", args.gxBasePath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "localKbPath", args.localKBPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "EnvironmentName", args.environmentName)

    bat label: "Apply reorganization", 
        script: "\"${args.msbuildExePath}\" .\\cdxci.msbuild ${target} ${msbuildGenArgs} /nologo "
}