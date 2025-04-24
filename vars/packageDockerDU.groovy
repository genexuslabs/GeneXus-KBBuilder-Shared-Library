/*
 * Job: PackageDockerDU
 *
 * Description:
 * This job compiles and deploys
 * a GeneXus Knowledge Base (KB) project using the provided paths and parameters. The method generates
 * a deployment package for the specified deployment unit (DU) and returns the path of the generated file.
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
    bat script: """
        "${args.msbuildExePath}" "${args.gxBasePath}\\deploy.msbuild" \
        /p:DEPLOY_TARGETS="${args.gxBasePath}\\DeploymentTargets\\Docker\\docker.targets" \
        /p:EXTRA_MSBUILD="${args.gxBasePath}\\ApplicationServers\\Templates\\JavaWeb\\msbuild\\TomcatContextSettings.msbuild" \
        /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
        /p:TargetId="DOCKER" \
        /p:KBPath="${args.localKBPath}" \
        /p:KBEnvironment="${args.environmentName}" \
        /p:DeploymentUnit="${args.duName}" \
        /p:ProjectName="${args.duName}_${env.BUILD_NUMBER}" \
        /p:ObjectNames="DeploymentUnitCategory:${args.duName}" \
        /p:ApplicationServer="${args.duAppServer}" \
        /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}\\${env.BUILD_NUMBER}" \
        /p:CallTreeLogFile="${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}_gxdprojCallTree.log" \
        /p:USE_APPSERVER_DATASOURCE="False" \
        /p:DEPLOY_TYPE="BINARIES" \
        /p:APPLICATION_KEY="${args.duAppEncryKey}" \
        /p:INCLUDE_GAM="${args.duIncludeGAM}" \
        /p:INCLUDE_GXFLOW_BACKOFFICE="${args.duIncludeGXFlowBackoffice}" \
        /p:APP_UPDATE="${args.duAppUpdate}" \
        /p:ENABLE_KBN="${args.duEnableKBN}" \
        /p:TARGET_JRE="${args.duTargetJRE}" \
        /p:PACKAGE_FORMAT="Automatic" \
        /p:DOCKER_CONTAINER_RUNTIME="" \
        /p:DOCKER_IMAGE_REGISTRY="" \
        /p:DOCKER_BASE_IMAGE="${args.dockerBaseImage}" \
        /p:DOCKER_MAINTAINER="GeneXus DevOps Team <devops@genexus.com>" \
        /p:DOCKER_WEBAPPLOCATION="" \
        /p:DOCKER_IMAGE_NAME="${args.dockerImageName.toLowerCase()}" \
        /p:K8S_GENERATE_KUBERNETES="False" \
        /p:K8S_NAMESPACE="" \
        /p:K8S_INITIAL_REPLICAS="" \
        /p:K8S_SERVICE_TYPE="" \
        /p:K8S_ENABLE_REDIS="False" \
        /p:TimeStamp="${env.BUILD_NUMBER}" \
        /l:FileLogger,Microsoft.Build.Engine \
        /t:CreateDeploy
    """
    String packageLocationPath = "${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}"
    String gxdprojFilePath = "${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}.gxdproj"
    
    echo "[DEBUG] packageLocationPath::${packageLocationPath}"
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
    return "${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}\\context"
}