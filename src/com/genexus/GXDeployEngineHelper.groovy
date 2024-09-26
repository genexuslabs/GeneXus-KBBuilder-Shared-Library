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
                /p:DOCKER_WEBAPPLOCATION="${args.webAppLocation}" \
                /p:JarName="${args.jarName}" \
                /p:WarName="${args.warName}" \
                /t:CreatePackage
            """


            // if ((powershell script: "return [System.IO.Path]::GetExtension(${args.packageLocation})").trim() == '.war') {
            //     msBuildCommand += ' /p:WarName="asdfghjhgfds"'
            // }
            // #TODO USER SWITCH, SI ES WAR --> .WAR, SI ES ZIP --> .ZIP, SI ES JAR --> .JAR, DEFAULT --> THROW ERROR


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
