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
            /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\${args.duName}\\${args.duName}\\${env.BUILD_NUMBER}" \
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
            /t:CreateDeploy
        """
    gxdprojFilePath = "${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}.gxdproj"
        
    bat script: """
            "${args.msbuildExePath}" "${gxdprojFilePath}" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:TimeStamp="${env.BUILD_NUMBER}" \
            /p:DeployFileFullPath="${args.localKBPath}\\${args.targetPath}\\${args.duName}" \
            /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\${args.duName}\\${args.duName}\\${env.BUILD_NUMBER}" \
            /p:AppName="${args.duName}" \
            /t:CreatePackage
        """
}