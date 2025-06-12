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
    String packageLocationPath = "${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}"
    echo "[DEBUG] packageLocationPath::${packageLocationPath}"
    powershell script: """
        if(Test-Path -Path "${packageLocationPath}") { Remove-Item -Path "${packageLocationPath}" -Recurse -Force}
        New-Item -Path "${packageLocationPath}" -ItemType Directory  -Force | Out-Null
    """
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
    String gxdprojFilePath = "${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}.gxdproj"
    echo "[DEBUG] gxdprojFilePath::${gxdprojFilePath}"
    
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
    
    String generatedFile = powershell label: "Find generated file",
    script: """
        \$deployPath = \"${packageLocationPath}\"
        \$dirs = Get-ChildItem -Path \$deployPath 
        foreach(\$i in \$dirs) {
            \$file = Get-ChildItem -Path \$deployPath\\\$i | Where-Object { \$_.Name.StartsWith(\"${args.duName}_${env.BUILD_NUMBER}\" + \".\")}
            Write-Output(\$file.name)
        }
    """, returnStdout: true
    echo "[DEBUG] generatedFile::${generatedFile.trim()}"
    String fullPackageLocationPath = "${packageLocationPath}\\${generatedFile.trim()}"
    echo "[DEBUG] fullPackageLocationPath::${fullPackageLocationPath}"
    String fullDockerContextLocationPath = "${packageLocationPath}\\context"
    echo "[DEBUG] fullDockerContextLocationPath::${fullDockerContextLocationPath}"

    def observabilityProvider = ''
    if(args.observabilityProvider) {
        observabilityProvider = args.observabilityProvider
    }
    def msBuildCommand = """
            "${args.msbuildExePath}" "${args.gxBasePath}\\CreateCloudPackage.msbuild" \
            /p:WebSourcePath="${args.localKBPath}\\${args.targetPath}\\web" \
            /p:ProjectName="${args.duName}_${env.BUILD_NUMBER}" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:TargetId="DOCKER" \
            /p:DOCKER_MAINTAINER="GeneXus DevOps Team <devops@genexus.com>" \
            /p:DOCKER_IMAGE_NAME="${args.dockerImageName.toLowerCase()}" \
            /p:DOCKER_BASE_IMAGE="${args.dockerBaseImage}" \
            /p:DeploySource="${fullPackageLocationPath}" \
            /p:CreatePackageScript="createpackage.msbuild" \
            /p:GXDeployFileProject="${args.localKBPath}\\${args.targetPath}\\web\\${args.duName}_${env.BUILD_NUMBER}.gxdproj" \
            /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\IntegrationPipeline\\${args.duName}\\${env.BUILD_NUMBER}" \
            /p:GENERATOR="${args.generator}" \
            /p:ObservabilityProvider="${observabilityProvider}" \
            /p:DOCKER_WEBAPPLOCATION="${args.webAppLocation}" \
            /t:CreatePackage \
        """

    def extension = powershell script: "return [System.IO.Path]::GetExtension('${fullPackageLocationPath}')", returnStdout: true
    extension = extension.trim().toLowerCase()
    echo "[INFO] Package extension: ${extension}" 
    switch (extension) {
        case '.war':
            if(args.warName == null) { args.warName = 'ROOT' }
            msBuildCommand += " /p:WarName=\"${args.warName}\""
            break
        case '.jar':
            if(args.jarName == null) { args.jarName = 'ROOT' }
            msBuildCommand += " /p:JarName=\"${args.jarName}\""
            break
        case '.zip':
        default:
            throw new Exception("[ERROR] Unsupported package extension: ${extension}")
    }
    powershell script: """
        if (Test-Path -Path "${fullDockerContextLocationPath}") { Remove-Item -Path "${fullDockerContextLocationPath}" -Recurse -Force }
        New-Item -ItemType Directory -Path "${fullDockerContextLocationPath}" | Out-Null
        Copy-Item -Path "${gxdprojFilePath}" -Destination "${fullDockerContextLocationPath}" -Recurse -Force
        Copy-Item -Path "${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}_gxdprojCallTree.log" -Destination "${fullDockerContextLocationPath}" -Recurse -Force
    """
    bat label: "Create Docker context",
        script: "${msBuildCommand}"
    
    powershell script: """
        if (Test-Path -Path "${gxdprojFilePath}") { Remove-Item -Path "${gxdprojFilePath}" -Recurse -Force }
        if (Test-Path -Path "${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}_gxdprojCallTree.log") { Remove-Item -Path "${args.localKBPath}\\${args.targetPath}\\Web\\${args.duName}_${env.BUILD_NUMBER}_gxdprojCallTree.log" -Recurse -Force }
    """
    
    return  fullDockerContextLocationPath
}