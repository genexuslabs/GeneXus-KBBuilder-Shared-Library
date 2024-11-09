package com.kbbuilder
import com.genexus.PropertiesHelper
import com.genexus.GXDeployEngineHelper


void buildPlatform(Map args = [:]) {
    try{
        def kbLibHelper = new PropertiesHelper()
        // ----------------------------- Print Debug vars
        echo "INFO GeneratedLanguage:: ${args.generatedLanguage}"
        echo "INFO DataSource:: ${args.dataSource}"
        echo "INFO TargetPath:: ${args.targetPath}"
        // ----------------------------- Clean target path
        powershell script: """
            \$ErrorActionPreference = 'Stop'
            if (Test-Path -Path "${args.localKBPath}\\${args.targetPath}") { Remove-Item -Path "${args.localKBPath}\\${args.targetPath}" -Recurse -Force }
            \$null = New-Item -Path "${args.localKBPath}\\${args.targetPath}\\web\\bin" -ItemType Directory
        """
        //----------------------------- Mark DB Reorganized
        markDBReorganized(args)
        //----------------------------- Apply ExternalObjectGenerator Pattern
        args.patternName = "ExternalObjectGenerator"
        applyPattern(args)
        //----------------------------- Build Configuration Environment (Configuration meens avoid configure database properties)
        buildConfigurationEnvironment(args)
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

String packagePlatform(Map args = [:]) {
    try{
        def gxLibDeployEngine = new GXDeployEngineHelper()
        // ----------------------------- Print Debug vars
        echo "INFO DeployTarget:: ${args.deployTarget}"
        echo "INFO PackagerResourcesDirPath:: ${args.packagerResourcesDirPath}"
        echo "INFO PackageAPIFile:: ${args.packageAPIFile}"
        echo "INFO PackageTarget:: ${args.packageTarget}"
        echo "INFO GamAPIResourcesRepository:: ${args.gamAPIResourcesRepository}"
        echo "INFO ExtObjGeneratorName:: ${args.extObjGeneratorName}"

        // ----------------------------- Create Patform Java-MySQL package
        bat script: """
                "${args.msbuildExePath}" "${args.localKBPath}\\${args.targetPath}\\Web\\${args.packageAPIFile}" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:KBGAMDirectory="${args.localKBPath}" \
                /p:KBEnvironment="${args.environmentName}" \
                /p:Generator="${args.generatedLanguage}" \
                /p:DBMS="${args.dataSource}" \
                /p:GenerateLibraryPath="${args.deployTarget}" \
                /p:PackagerResources="${args.packagerResourcesDirPath}" \
                /p:SolutionPath="${WORKSPACE}\\${gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${extObjGeneratorName}" \
                /t:${args.packageTarget}
        """
        // ----------------------------- Rename package for nuget
        args.packageName = powershell script: """
            \$ErrorActionPreference = 'Stop'
            \$packageFileName = (Get-ChildItem -Path "${args.deployTarget}" -Filter '*.zip').Name
            \$packageFullPath = Join-Path "${args.deployTarget}" "\$packageFileName"
            Rename-Item -Path \$packageFullPath -NewName \$packageFileName.replace('-', '.') -Force
            (Get-ChildItem -Path "${args.deployTarget}" -Filter '*.zip').Name
        """, returnStdout: true
        echo "[INFO] Package Name:: ${args.packageName.trim()}"
        args.packageLocation = "${args.deployTarget}\\${args.packageName.trim()}"
        echo "[INFO] Package Location:: ${args.packageLocation}"
        // ----------------------------- Archive artifacts
        dir("${args.deployTarget}") {
            archiveArtifacts artifacts: "${args.packageName.trim()}", followSymlinks: false
        }
        // ----------------------------- Create NuGet package
        args.packageName = args.packageName.replace(".zip", "").trim()
        args.packageVersion = args.componentVersion
        args.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(args)

        return "${args.componentId}.${args.packageName}"

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void publishPlatform(Map args = [:]) {
    try{


    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void updatePlatformFromZip(Map args = [:]) {
    try{


    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}


return this
