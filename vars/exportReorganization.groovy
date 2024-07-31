/*
 * Job exportReorganization >> This method 
 * -- >> For detailed information on the task, refer to the documentation: https://wiki.genexus.com/commwiki/wiki?42568,Reorganization+Deployment+MSBuild+Tasks
 *
 * @Param args = [:]
 * +- gxBasePath
 * +- localKBPath
 * +- environmentName
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:], String reorgExportPath) {
    String target = " /t:ExportReorganization"
    String msbuildGenArgs = ''
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "GX_PROGRAM_DIR", args.gxBasePath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "localKbPath", args.localKBPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "EnvironmentName", args.environmentName)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "SourcePath", "${args.localKBPath}\\${args.targetPath}")
    echo "[INFO] export reorganization to ${reorgExportPath}"
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "ReorgDestination", reorgExportPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "FileName", "${env.BUILD_NUMBER}_reorg.jar")
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "Generator", args.generator)

    if(args.generator == "Java") {
        String jdkToolPath = tool name: args.jdkInstallationId, type: 'jdk'
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "JavaPath", jdkToolPath)
        /*
        if(args.tomcatVersion) {
            msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "TomcatVersionName", parseTomcatVersion(args.tomcatVersion))
        }
        */
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "JDBCDrivers", "com.mysql.jdbc.Driver;com.mysql.jdbc.Driver")
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "PackageName", args.javaPackageName)
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "TargetJRE", "9")
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "MySQL", "true")
    }
    else {
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "SQLServer", "true")
    }
    
    bat label: 'Export reorganization', 
        script: "\"${args.msbuildExePath}\" \"${args.gxBasePath}\\deploy.msbuild\" ${target} ${msbuildGenArgs} /nologo "

    powershell script: "Copy-Item \"${args.localKBPath}\\${args.targetPath}\\Web\\ReorganizationScript.txt\" \"${reorgExportPath}\\${env.BUILD_NUMBER}_ReorganizationScript.txt\""
}