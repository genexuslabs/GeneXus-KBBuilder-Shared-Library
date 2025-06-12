/*
 * Job: Package Angular DU
 *
 * Description:
 *  The method generates a deployment package for the specified deployment unit (Angular) and returns
 *  the path of the generated file.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment to be used.
 *   - propertiesFilePath: The path to the properties file.
 *   - machineFilePath: The path to the machine configuration file.
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - duName: The name of the deployment unit.
 *   - duAppServer: The application server for the deployment unit.
 *   - targetPath: The target path for the deployment.
 *   - duAppEncryKey: The application encryption key for the deployment unit.
 *   - duIncludeGAM: Boolean flag indicating whether to include GAM (GeneXus Access Manager).
 *   - duIncludeGXFlowBackoffice: Boolean flag indicating whether to include GXFlow Backoffice.
 *   - duAppUpdate: Boolean flag indicating whether to update the application.
 *   - duEnableKBN: Boolean flag indicating whether to enable KBN (Knowledge Base Navigator).
 *   - duTargetJRE: The target JRE for the deployment unit.
 *
 * Workflow Steps:
 * 1. Compile and deploy the GeneXus KB project using MSBuild.
 * 2. Generate the deployment package for the specified deployment unit.
 * 3. Return the path of the generated deployment package file.
 */

def call(Map args = [:]) {

    String gxdprojFilePath = ''
    bat script: """
            "${args.msbuildExePath}" "${args.gxBasePath}\\deploy.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:TargetId="STATICFRONTEND" \
            /p:KBPath="${args.localKBPath}" \
            /p:KBEnvironment="${args.environmentName}" \
            /p:DeploymentUnit="${args.duName}" \
            /p:ProjectName="${args.duName}_${env.BUILD_NUMBER}" \
            /p:ObjectNames="DeploymentUnitCategory:${args.duName}" \
            /p:ApplicationServer="${args.duAppServer}" \
            /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}\\${env.BUILD_NUMBER}" \
            /p:CallTreeLogFile="${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}_gxdprojCallTree.log" \
            /p:DEPLOY_TARGETS="${args.gxBasePath}\\DeploymentTargets\\StaticFrontEnd\\staticfrontend.targets" \
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
            /l:FileLogger,Microsoft.Build.Engine \
            /t:CreateDeploy
        """
    gxdprojFilePath = "${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}.gxdproj"
    
    def packageLocationPath = "${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}"
    echo "[DEBUG] packageLocationPath::${packageLocationPath}"
    powershell script: """
        if(Test-Path -Path "${packageLocationPath}") { Remove-Item -Path "${packageLocationPath}" -Recurse -Force}
        New-Item -Path "${packageLocationPath}" -ItemType Directory  -Force | Out-Null
    """

    bat script: """
            "${args.msbuildExePath}" "${args.gxBasePath}\\CreateFrontendPackage.msbuild" \
            /p:"GX_PROGRAM_DIR"="${args.gxBasePath}" \
            /p:"GXDeployFileProject"="${gxdprojFilePath}" \
            /p:"ProjectRootDirectory"="${args.localKBPath}\\${args.targetPath}\\mobile\\Angular" \
            /p:"GenExtensionName"="Angular" \
            /p:"DeploymentScriptDocker"="deploy.angular.docker.msbuild" \
            /p:"STATICFRONTEND_DOCKER_APPLOCATION"="app" \
            /p:"STATICFRONTEND_PROVIDER"="docker" \
            /p:"DeployFullPath"="${packageLocationPath}" \
            /p:"DeployDirectory"="${packageLocationPath}" \
            /t:CreatePackage
        """

    return "${packageLocationPath}\\context"
}