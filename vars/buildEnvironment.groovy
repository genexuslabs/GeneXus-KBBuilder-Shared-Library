/*
 * Job buildKnowledgeBase >> Read properties from environment
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

    String target = " /t:BuildWithDB"
    String msbuildGenArgs = ''
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "GX_PROGRAM_DIR", args.localGXPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "localKbPath", args.localKBPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "EnvironmentName", args.environmentName)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "Generator", args.generator)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "DataSource", args.dataSource)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "rebuild", String.valueOf(args.forceRebuild))

    //Java flags
    if(args.generator == "Java") {
        String jdkToolPath = tool name: args.jdkInstallationId, type: 'jdk'
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "JavaPath", jdkToolPath)
        if(args.tomcatVersion) {
            msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "TomcatVersionName", parseTomcatVersion(args.tomcatVersion))
        }
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "ServletDir", "${args.localKBPath}\\${args.targetPath}\\web\\Deploy\\servlet")
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "StaticDir", "${args.localKBPath}\\${args.targetPath}\\web\\Deploy\\static")
        if(args.jdbcLogPath) {
            msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "JDBClogFile", args.jdbcLogPath)
        }
    }
    // Android flag
    if(Boolean.valueOf(args.genexusNeedAndroidSDK)) {
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "android", 'True')
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "androidSDKpath", "${args.genexusNeedAndroidSDK}")
    }

    bat label: 'Build all', 
        script: "\"${args.msbuildExePath}\" .\\cdxci.msbuild ${target} ${msbuildGenArgs} /nologo "
}