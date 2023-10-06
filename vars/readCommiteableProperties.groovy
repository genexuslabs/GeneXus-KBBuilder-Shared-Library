/*
 * Job readCommiteableProperties >> Read properties from environment
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
    def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
    writeFile file: 'properties.msbuild', text: fileContents

    String propertiesFilePath = "${args.localKBPath}\\commiteableProperties.json"
    String target = " /t:ReadCommiteableProperties"
    String msbuildGenArgs = ''
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "GX_PROGRAM_DIR", args.localGXPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "localKbPath", args.localKBPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "EnvironmentName", args.environmentName)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "PropFileAbsolutePath", propertiesFilePath)
    bat label: 'Writing commiteable porperties',
        script: "\"${args.msbuildExePath}\" .\\properties.msbuild ${target} ${msbuildGenArgs} /nologo "

    LinkedHashMap fileDefinition = readJSON file: propertiesFilePath
    args.targetPath = fileDefinition.targetPath
    args.dataSource = fileDefinition.dataSource
    args.generator = fileDefinition.generator
    args.javaPackageName = fileDefinition.javaPackageName
    return args
}