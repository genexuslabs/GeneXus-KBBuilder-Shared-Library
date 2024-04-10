package com.genexus
/**
 * This methods
 * @param localGXPath
 */
void createDockerContext(Map args = [:]) {
    try {
        bat label: "Create Docker context",
            script: """
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
                /p:DOCKER_WEBAPPLOCATION="${args.webAppLocation}" \
                /p:GENERATOR="${args.generator}" \
                /t:CreatePackage
            """
//                /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\${args.duName}\\${args.duName}\\${env.BUILD_NUMBER}" \
    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo " ERROR ${error.getMessage()}"
        throw error
    }
}
return this
