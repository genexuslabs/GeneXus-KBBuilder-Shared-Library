/*
 * Job exportReorganization >> Read properties from environment
 *
 * @Param args = [:]
 * +- localGXPath
 * +- localKBPath
 * +- environmentName
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:]) {
    bat label: "Export reorganization to ${reorgExportPath}",
        script: """
            "${args.msbuildExePath}" "${args.localGXPath}\\deploy.msbuild" \
            /p:GX_PROGRAM_DIR="${args.localGXPath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:SourcePath="${args.environmentName}" \
            /p:SourcePath="${args.localKBPath}\\${args.targetPath}" \
            /p:ReorgDestination="${args.reorgExportPath}" \
            /p:FileName="${env.BUILD_NUMBER}_reorg.jar" \
            /p:Generator="${args.generator}" \
            /p:JavaPath="${args.javaPath}" \
            /p:TomcatVersionName="${args.tomcatVersionName}" \
            /p:JDBCDrivers="${args.jdbcDrivers}" \
            /p:PackageName="${args.javaPackageName}" \
            /p:TargetJRE="${args.targetJRE}" \
            /t:ExportReorganization
        """
    /*
    if Java
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "MySQL", "true")
    else 
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "SQLServer", "true")
    */

    powershell script: "Copy-Item \"${args.localKBPath}\\${args.targetPath}\\Web\\ReorganizationScript.txt\" \"${reorgExportPath}\\${env.BUILD_NUMBER}_ReorganizationScript.txt\""
}