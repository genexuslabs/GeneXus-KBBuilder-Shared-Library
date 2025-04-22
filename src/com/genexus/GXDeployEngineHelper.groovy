package com.genexus

def downloadNuGet() {
    powershell script: """
        \$ErrorActionPreference = "Stop"

        \$nuGetExePath = Join-Path "\$env:USERPROFILE\\GeneXusBuilderTools" "nuget.exe"
        \$nuGetExeUrl = "https://dist.nuget.org/win-x86-commandline/latest/nuget.exe"

        if (-Not (Test-Path -Path "\$nuGetExePath")) {
            New-Item -ItemType Directory -Force -Path "\$env:USERPROFILE\\GeneXusBuilderTools"
            Write-Output((Get-Date -Format G) + " [INFO] Downloading nuget.exe from \$nuGetExeUrl to \$nuGetExePath")
            Invoke-WebRequest -Uri \$nuGetExeUrl -OutFile \$nuGetExePath
            Write-Output((Get-Date -Format G) + " [INFO] nuget.exe downloaded to \$nuGetExePath")

            & \$nuGetExePath eula

        } else {
            Write-Output((Get-Date -Format G) + " [INFO] nuget.exe already exists at \$nuGetExePath.")
        }
    """
}
/**
 * This methods
 * @param localGXPath
 */
def createDockerContext(Map args = [:]) {
    try {
        def observabilityProvider = ''
        if(args.observabilityProvider) {
            observabilityProvider = args.observabilityProvider
        }
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
                /p:GXDeployFileProject="${args.localKBPath}\\${args.targetPath}\\web\\${args.duName}.gxdproj" \
                /p:ProjectName="${args.duName}_${env.BUILD_NUMBER}" \
                /p:GENERATOR="${args.generator}" \
                /p:ObservabilityProvider="${observabilityProvider}" \
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
            powershell script: """
        
              if (Test-Path -Path ${contextLocation}) { Remove-Item -Path ${contextLocation} -Recurse -Force }
              New-Item -ItemType Directory -Path ${contextLocation} | Out-Null
        """
        bat label: "Create Docker context",
            script: "${msBuildCommand}"
            
        return  contextLocation
    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo "[ERROR] ${error.getMessage()}"
        throw error
    }
}

String createNuGetPackageFromZip(Map args = [:]) {
    try{
        //----- Parse Package Location
        workingDir = powershell script: "Split-Path \"${args.packageLocation}\" -Parent", returnStdout: true
        echo "[DEBUG] workingDir::${workingDir.trim()}"

        //---- Set Package Name
        def packageId = ""
        if(args.prefix) { packageId += "${args.prefix}." }
        packageId += "${args.componentId}.${args.packageName}"
        if(args.sufix) { packageId += ".${args.sufix}" }
        echo "[INFO] packageId::${packageId}"

        //----Set .nuspec properties
        String nuspecFileName = "${packageId}.${args.packageVersion}.nuspec"
        echo "[DEBUG] nuspecFileName::${nuspecFileName}"
        String nuspecPath = "${workingDir.trim()}\\${nuspecFileName}"
        echo "[DEBUG] nuspecPath::${nuspecPath}"
        def nuspecContent = """<?xml version="1.0" encoding="utf-8"?>
<package xmlns="http://schemas.microsoft.com/packaging/2012/06/nuspec.xsd">
<metadata>
    <id>${packageId}</id>
    <version>${args.packageVersion}</version>
    <authors>GeneXus</authors>
    <description>Generated ${args.ComponentId} package for integration with GeneXus</description>
</metadata>
<files>
    <file src="${args.packageLocation}" />
</files>
</package>
"""
        //---- Write nuspec content
        writeFile file: "${nuspecPath}", text: "${nuspecContent}"

        downloadNuGet()

        // Execute NuGet package
        def nupkgPath = nuspecPath.replace("nuspec", "nupkg")
        echo "[DEBUG] nupkgPath::${nupkgPath}"
        powershell script: """
            \$ErrorActionPreference = "Stop"
            \$nuGetExePath = Join-Path "\$env:USERPROFILE\\GeneXusBuilderTools" "nuget.exe"
            & \$nuGetExePath pack "${nuspecPath}" -OutputDirectory ${workingDir.trim()}
            if(-not(Test-Path -Path "${nupkgPath}")) {
                Write-Output((Get-Date -Format G) + " [ERROR] ${nupkgPath} not found")
                throw "[ERROR] ${nupkgPath} not found"
            }
        """

        return nupkgPath

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

def publishNuGetPackage(Map args = [:]) {
    try {
        downloadNuGet()

        withCredentials([usernamePassword(
            credentialsId: "${args.moduleServerApiKeyId}", usernameVariable: 'publisherUser', passwordVariable: 'apiKeyValue')]
        ) {
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                \$nuGetExePath = Join-Path "\$env:USERPROFILE\\GeneXusBuilderTools" "nuget.exe"
                & \$nuGetExePath push "${args.nupkgPath}" -Source "${args.moduleServerSource}" -ApiKey ${apiKeyValue} -SkipDuplicate
            """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo "[ERROR] ${error.getMessage()}"
        throw error
    }
}

return this
