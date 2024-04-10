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
                /p:DOCKER_IMAGE_NAME="${args.dockerImageNametoLowerCase()}" \
                /p:DOCKER_BASE_IMAGE="${args.dockerBaseImage}" \
                /p:DeployFullPath="${args.localKBPath}\\${args.targetPath}\\${args.duName}\\${args.duName}\\${env.BUILD_NUMBER}" \
                /p:DeploySource="${args.packageLocation}" \
                /p:CreatePackageScript="createpackage.msbuild" \
                /p:WebSourcePath="${args.localKBPath}\\${args.targetPath}\\web" \
                /p:ProjectName="${args.duName}_${env.BUILD_NUMBER}" \
                /p:DOCKER_WEBAPPLOCATION="${args.duName}" \
                /t:CreatePackage
            """
            /*
        String target = "\"${args.gxBasePath}\\CreateCloudPackage.msbuild\" /t:CreatePackage"
        String msbuildGenArgs = ''
        msbuildGenArgs = concatArgs(msbuildGenArgs, "GX_PROGRAM_DIR", "${localGXPath}")
        def duExtentionName = ''
        String projectName = "${deploymentUnitDefinition.name}_${env.BUILD_NUMBER}"
        switch (generatorLenguage) {
            case 'Java':
                def auxFileExtention = powershell label: "Find generated Java file",
                    script: """
                        \$deployPath = \"${integrationDeployTargetPath}\\\\${deploymentUnitDefinition.name}\"
                        \$dirs = Get-ChildItem -Path \$deployPath 
                        foreach(\$i in \$dirs) {
                            \$file = Get-ChildItem -Path \$deployPath\\\$i | Where-Object { \$_.Name.StartsWith(\"${projectName}\" + \".\")}
                            \$aux = \$file.name
                            if(![string]::IsNullOrEmpty(\$aux)) {
                                \$aux.Replace(\"${projectName}\", \"\")
                            } 
                        }
                    """, returnStdout: true
                def fileExtention = auxFileExtention.trim()
                echo " DEBUG fileExtention::${fileExtention}"
                if(fileExtention.equals(".jar")) {
                    if(deploymentUnitDefinition.urlSubdirectory) {
                        msbuildGenArgs = concatArgs(msbuildGenArgs, "JarName", deploymentUnitDefinition.urlSubdirectory)
                    }
                    else {
                        msbuildGenArgs = concatArgs(msbuildGenArgs, "JarName", "ROOT")
                    }
                }
                else {
                    if(deploymentUnitDefinition.urlSubdirectory) {
                        msbuildGenArgs = concatArgs(msbuildGenArgs, "WarName", deploymentUnitDefinition.urlSubdirectory)
                    }
                    else {
                        msbuildGenArgs = concatArgs(msbuildGenArgs, "WarName", "ROOT")
                    }
                }
                duExtentionName = "${projectName}${fileExtention}"
                break

            case '.NET':
                duExtentionName = "${projectName}.zip"
                break

            default:
                currentBuild.result = 'FAILURE'
                error 'Generator is not Java or .Net.'
                break
        }

        bat label: 'Creating Docker context', 
            script: "\"${msbuildExePath}\" ${target} ${msbuildGenArgs} /nologo "

        powershell script: "Remove-Item -Path \"${integrationDeployTargetPath}\\${deploymentUnitDefinition.name}\\${duExtentionName}\" -Recurse"
        */
    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo " ERROR ${error.getMessage()}"
        throw error
    }
}
return this
