package com.genexus

/**
 * Extracts the file extension from a given file path.
 * @param filePath The full path of the file.
 * @return The file extension.
 */
def getFileExtension(String filePath) {
    return filePath.substring(filePath.lastIndexOf('.') + 1)
}
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
                /p:DOCKER_WEBAPPLOCATION="${args.webAppLocation}" \
                /p:GENERATOR="${args.generator}" \
                /t:CreatePackage
            """
        if (args.generator.toLowerCase().contains("java")) {
            if (getFileExtension(args.packageLocation) == "jar"){
                msBuildCommand = msBuildCommand + " /p:JarName=\"ROOT\""
            }else{
                msBuildCommand = msBuildCommand + " /p:WarName=\"ROOT\""
            }
            echo "[INFO] Java Generator detected"
            echo "[DEBUG] get file extension: ${getFileExtension(args.packageLocation)}"
        }

        bat label: "Create Docker context",
            script: "${msBuildCommand}"
            
    return args.packageLocation.replace("${args.duName}_${env.BUILD_NUMBER}.zip","context")
    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo "[ERROR] ${error.getMessage()}"
        throw error
    }
}
return this
