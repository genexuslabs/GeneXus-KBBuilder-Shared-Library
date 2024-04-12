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

    String gxdprojFilePath = ''
    bat script: """
            "${args.msbuildExePath}" "${args.gxBasePath}\\deploy.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:TargetId="LOCAL" \
            /p:KBPath="${args.localKBPath}" \
            /p:KBEnvironment="${args.environmentName}" \
            /p:DeploymentUnit="${args.duName}" \
            /p:ProjectName="${args.duName}_${env.BUILD_NUMBER}" \
            /p:ObjectNames="DeploymentUnitCategory:${args.duName}" \
            /p:ApplicationServer="${args.duAppServer}" \
            /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}\\${env.BUILD_NUMBER}" \
            /p:CallTreeLogFile="${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}_gxdprojCallTree.log") \
            /p:USE_APPSERVER_DATASOURCE="False" \
            /p:DEPLOY_TYPE="BINARIES" \
            /p:APPLICATION_KEY="${args.duAppEncryKey}" \
            /p:INCLUDE_GAM="${args.duIncludeGAM}" \
            /p:INCLUDE_GXFLOW_BACKOFFICE="${args.duIncludeGXFlowBackoffice}" \
            /p:APP_UPDATE="${args.duAppUpdate}" \
            /p:ENABLE_KBN="${args.duEnableKBN}" \
            /p:TARGET_JRE="${args.duTargetJRE}" \
            /p:PACKAGE_FORMAT="Automatic" \
            /p:TimeStamp="${env.BUILD_NUMBER}" \
            /l:FileLogger,Microsoft.Build.Engine;logfile=c:\\fullgx\\temp\\CreateDeploy.log
            /t:CreateDeploy
        """
    gxdprojFilePath = "${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}.gxdproj"
    
    def packageLocationPath = "${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}"
    echo "DEBUG packageLocationPath::${packageLocationPath}"
    bat script: """
            "${args.msbuildExePath}" "${gxdprojFilePath}" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:TimeStamp="${env.BUILD_NUMBER}" \
            /p:DeployFileFullPath="${packageLocationPath}" \
            /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}\\${env.BUILD_NUMBER}" \
            /p:AppName="${args.duName}" \
            /t:CreatePackage
        """
        def generatedFile = powershell label: "Find generated file",
        script: """
            \$deployPath = \"${packageLocationPath}\"
            \$dirs = Get-ChildItem -Path \$deployPath 
            foreach(\$i in \$dirs) {
                \$file = Get-ChildItem -Path \$deployPath\\\$i | Where-Object { \$_.Name.StartsWith(\"${args.duName}_${env.BUILD_NUMBER}\" + \".\")}
                Write-Output(\$file.name)
            }
        """, returnStdout: true
    return "${packageLocationPath}\\${generatedFile.trim()}"
}