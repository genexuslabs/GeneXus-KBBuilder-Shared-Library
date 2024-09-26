package com.genexus

/**
 * This methods
 * @param localGXPath
 */
def createDockerContext(Map args = [:]) {
    try {
        def msBuildCommand = """
                "${args.msbuildExePath}" "${args.gxBasePath}\\CreateCloudPackage.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:TargetId="DOCKER" \
                /p:DOCKER_MAINTAINER="GeneXus DevOps Team <devops@genexus.com>" \
                /p:DOCKER_IMAGE_NAME="${args.dockerImageName.toLowerCase()}" \
                /p:DOCKER_BASE_IMAGE="${args.dockerBaseImage}" \
                /p:DeploySource="${args.packageLocation}" \
                /p:CreatePackageScript="createpackage.msbuild" \
                /p:WebSourcePath="${args.localKBPath}\\${args.targetPath}\\web" \
                /p:ProjectName="${args.duName}_${env.BUILD_NUMBER}" \
                /p:GENERATOR="${args.generator}" \
                /p:DOCKER_WEBAPPLOCATION="${args.webAppLocation}" \
                /t:CreatePackage \
            """

            def extension = powershell script: "return [System.IO.Path]::GetExtension('${args.packageLocation}')", returnStdout: true
            def contextLocation = ''
            extension = extension.trim().toLowerCase()
            echo "[INFO] Package extension: ${extension}" 
            switch (extension) {
                case '.war':
                    msBuildCommand += " /p:WarName=\"${args.warName}\""
                    contextLocation = args.packageLocation.replace("${args.duName}_${env.BUILD_NUMBER}.war","context")
                    break
                case '.jar':
                    msBuildCommand += " /p:JarName=\"${args.jarName}\""
                    contextLocation = args.packageLocation.replace("${args.duName}_${env.BUILD_NUMBER}.jar","context")
                    break
                case '.zip':
                    contextLocation = args.packageLocation.replace("${args.duName}_${env.BUILD_NUMBER}.zip","context")
                default:
                    throw new Exception("[ERROR] Unsupported package extension: ${extension}")
            }
    
        bat label: "Create Docker context",
            script: "${msBuildCommand}"
            
        return  contextLocation
    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo "[ERROR] ${error.getMessage()}"
        throw error
    }
}
return this
