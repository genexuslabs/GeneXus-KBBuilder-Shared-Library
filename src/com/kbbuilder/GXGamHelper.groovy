package com.kbbuilder
import com.genexus.FileHelper
import com.genexus.PropertiesHelper
import com.genexus.GXDeployEngineHelper

void completePlatformIntegration(Map envArgs = [:]) {
    try{
        def sysLibHelper = new FileHelper()
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()
        // ----------------------------- Print Debug vars
        echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
        echo "INFO dataSource:: ${envArgs.dataSource}"
        echo "INFO targetPath:: ${envArgs.targetPath}"
        stage("Build Platform ${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            // ----------------------------- Clean target path
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -ItemType Directory
            """
            //----------------------------- Mark DB Reorganized
            markDBReorganized(envArgs)
            //----------------------------- Apply ExternalObjectGenerator Pattern
            envArgs.patternName = "ExternalObjectGenerator"
            applyPattern(envArgs)
            //----------------------------- Build Configuration Environment (Configuration meens avoid configure database properties)
            buildConfigurationEnvironment(envArgs)
        }
        stage("Package Platform ${envArgs.targetPath}") {
            envArgs.deployTarget = sysLibHelper.getFullPath("${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration").trim()
            // ----------------------------- Clean deployTarget
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.deployTarget}") { Remove-Item -Path "${envArgs.deployTarget}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.deployTarget}" -ItemType Directory
            """
            // ----------------------------- Package Patform
            bat script: """
                    "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.packageAPIFile}" \
                    /p:GX_PROGRAM_DIR="${envArgs.gxBasePath}" \
                    /p:KBGAMDirectory="${envArgs.localKBPath}" \
                    /p:KBEnvironment="${envArgs.environmentName}" \
                    /p:Generator="${envArgs.generatedLanguage}" \
                    /p:DBMS="${envArgs.dataSource}" \
                    /p:GenerateLibraryPath="${envArgs.deployTarget}" \
                    /p:PackagerResources="${envArgs.packagerResourcesDirPath}" \
                    /p:SolutionPath="${WORKSPACE}\\${envArgs.gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${envArgs.extObjGeneratorName}" \
                    /t:${envArgs.packageTarget}
            """
            // ----------------------------- Rename package for nuget
            envArgs.packageName = powershell script: """
                \$ErrorActionPreference = 'Stop'
                \$packageFileName = (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
                \$packageFullPath = Join-Path "${envArgs.deployTarget}" "\$packageFileName"
                Rename-Item -Path \$packageFullPath -NewName \$packageFileName.replace('-', '.') -Force
                (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
            """, returnStdout: true
            echo "[INFO] Package Name:: ${envArgs.packageName.trim()}"
            envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName.trim()}"
            echo "[INFO] Package Location:: ${envArgs.packageLocation}"
            // ----------------------------- Archive artifacts
            dir("${envArgs.deployTarget}") {
                archiveArtifacts artifacts: "${envArgs.packageName.trim()}", followSymlinks: false
            }
            // ----------------------------- Create NuGet package
            envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            envArgs.packageVersion = envArgs.componentVersion
            envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

            // ----------------------------- Publish NuGet package
            envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            gxLibDeployEngine.publishNuGetPackage(envArgs)
        }
        stage("Update Platform ${envArgs.targetPath}") {
            // ----------------------------- Update Platform package in GeneXus Installation
            powershell script: """
                \$platformDir = "${envArgs.gxBasePath}\\Library\\GAM\\Platforms\\${envArgs.platformDirectory}"
                Get-ChildItem -Path \$platformDir -File | Where-Object { \$_.Name -ne 'ReorganizationScript.txt' -and \$_.Name -ne 'reorganization.jar' } | Remove-Item -Force
                Get-ChildItem -Path \$platformDir -Directory | Where-Object { \$_.Name -ne 'Reorgs' } | Remove-Item -Recurse -Force
                Expand-Archive -Path "${envArgs.deployTarget}\\${envArgs.packageName.trim()}.zip" -DestinationPath "${envArgs.gxBasePath}" -Force 
            """
        }

        def ret = "${envArgs.componentId}.${envArgs.packageName}"
        if(envArgs.generatedLanguage == 'Net') {
            ret = ret.replace("Net", "CSharp")
        }
        return ret.replace("GAM","Gam")
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
void completeJavaPlatformIntegration(Map envArgs = [:]) {
    try{
        def sysLibHelper = new FileHelper()
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()
        // ----------------------------- Print Debug vars
        echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
        echo "INFO dataSource:: ${envArgs.dataSource}"
        echo "INFO targetPath:: ${envArgs.targetPath}"
        stage("Build Platform ${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            // ----------------------------- Clean target path
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib" -ItemType Directory
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\src\\main\\java\\genexus\\security\\api" -ItemType Directory
            """
            //----------------------------- Mark DB Reorganized
            markDBReorganized(envArgs)
            //----------------------------- Apply ExternalObjectGenerator Pattern
            envArgs.patternName = "ExternalObjectGenerator"
            applyPattern(envArgs)
            //----------------------------- Sync .java files generated by ExternalObjectGenerator
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                Write-Output((Get-Date -Format G) + " [INFO] Sync ExternalObjectGenerator .java objs")
                Copy-Item -Path "${WORKSPACE}\\${envArgs.gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${envArgs.extObjGeneratorName}\\genexus\\security\\GAMSecurityProvider.java" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\GAMSecurityProvider.java" -Force -Recurse
                Copy-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\*java" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\src\\main\\java\\genexus\\security\\" -Force -Recurse
            """
            //----------------------------- Build Configuration Environment (Configuration meens avoid configure database properties)
            buildConfigurationEnvironment(envArgs)
        }
        stage("Package Platform ${envArgs.targetPath}") {
            envArgs.deployTarget = sysLibHelper.getFullPath("${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration").trim()
            // ----------------------------- Clean deployTarget
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.deployTarget}") { Remove-Item -Path "${envArgs.deployTarget}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.deployTarget}" -ItemType Directory
            """
            // ----------------------------- Package Patform
            bat script: """
                    "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.packageAPIFile}" \
                    /p:GX_PROGRAM_DIR="${envArgs.gxBasePath}" \
                    /p:KBGAMDirectory="${envArgs.localKBPath}" \
                    /p:KBEnvironment="${envArgs.environmentName}" \
                    /p:Generator="${envArgs.generatedLanguage}" \
                    /p:DBMS="${envArgs.dataSource}" \
                    /p:GenerateLibraryPath="${envArgs.deployTarget}" \
                    /p:PackagerResources="${envArgs.packagerResourcesDirPath}" \
                    /p:SolutionPath="${WORKSPACE}\\${envArgs.gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${envArgs.extObjGeneratorName}" \
                    /t:${envArgs.packageTarget}
            """
            // ----------------------------- Rename package for nuget
            envArgs.packageName = powershell script: """
                \$ErrorActionPreference = 'Stop'
                \$packageFileName = (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
                \$packageFullPath = Join-Path "${envArgs.deployTarget}" "\$packageFileName"
                Rename-Item -Path \$packageFullPath -NewName \$packageFileName.replace('-', '.') -Force
                (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
            """, returnStdout: true
            echo "[INFO] Package Name:: ${envArgs.packageName.trim()}"
            envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName.trim()}"
            echo "[INFO] Package Location:: ${envArgs.packageLocation}"
            // ----------------------------- Archive artifacts
            dir("${envArgs.deployTarget}") {
                archiveArtifacts artifacts: "${envArgs.packageName.trim()}", followSymlinks: false
            }
            // ----------------------------- Create NuGet package
            envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            envArgs.packageVersion = envArgs.componentVersion
            envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

            // ----------------------------- Publish NuGet package
            envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            gxLibDeployEngine.publishNuGetPackage(envArgs)
        }
        stage("Update Platform ${envArgs.targetPath}") {
            // ----------------------------- Update Platform package in GeneXus Installation
            powershell script: """
                \$platformDir = "${envArgs.gxBasePath}\\Library\\GAM\\Platforms\\${envArgs.platformDirectory}"
                Get-ChildItem -Path \$platformDir -File | Where-Object { \$_.Name -ne 'ReorganizationScript.txt' -and \$_.Name -ne 'reorganization.jar' } | Remove-Item -Force
                Get-ChildItem -Path \$platformDir -Directory | Where-Object { \$_.Name -ne 'Reorgs' } | Remove-Item -Recurse -Force
                Expand-Archive -Path "${envArgs.deployTarget}\\${envArgs.packageName.trim()}.zip" -DestinationPath "${envArgs.gxBasePath}" -Force 
            """
        }

        def ret = "${envArgs.componentId}.${envArgs.packageName}"
        if(envArgs.generatedLanguage == 'Net') {
            ret = ret.replace("Net", "CSharp")
        }
        return ret.replace("GAM","Gam")
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}


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

String updatePlatform(Map args = [:]) {
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
                /p:SolutionPath="${WORKSPACE}\\${args.gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${args.extObjGeneratorName}" \
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

        // ----------------------------- Publish NuGet package
        args.moduleServerSource = "${args.moduleServerSourceBase}${args.artifactsServerId}"
        gxLibDeployEngine.publishNuGetPackage(args)

        // ----------------------------- Update Platform package in GeneXus Installation
        powershell script: """
            \$platformDir = "${args.gxBasePath}\\Library\\GAM\\Platforms\\${args.platformDirectory}"
            Get-ChildItem -Path \$platformDir -File | Where-Object { \$_.Name -ne 'ReorganizationScript.txt' -and \$_.Name -ne 'reorganization.jar' } | Remove-Item -Force
            Get-ChildItem -Path \$platformDir -Directory | Where-Object { \$_.Name -ne 'Reorgs' } | Remove-Item -Recurse -Force
            Expand-Archive -Path "${args.deployTarget}\\${args.packageName.trim()}.zip" -DestinationPath "${args.gxBasePath}" -Force 
        """

        return "${args.componentId}.${args.packageName}"

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

String updateInitResources(Map args = [:]) {
    try{
        def gxLibDeployEngine = new GXDeployEngineHelper()
        // ----------------------------- Print Debug vars
        echo "INFO DeployTarget:: ${args.deployTarget}"
        echo "INFO PackagerResourcesDirPath:: ${args.packagerResourcesDirPath}"
        echo "INFO PackageAPIFile:: ${args.packageAPIFile}"
        echo "INFO PackageTarget:: ${args.packageTarget}"
        echo "INFO GamAPIResourcesRepository:: ${args.gamAPIResourcesRepository}"
        echo "INFO ExtObjGeneratorName:: ${args.extObjGeneratorName}"
        // ----------------------------- Clean deployTarget
        powershell script: """
            \$ErrorActionPreference = 'Stop'
            if (Test-Path -Path "${args.deployTarget}") { Remove-Item -Path "${args.deployTarget}" -Recurse -Force }
            \$null = New-Item -Path "${args.deployTarget}" -ItemType Directory
        """
        // ----------------------------- Create GAM Init Resources package
        bat script: """
            "${args.msbuildExePath}" "${args.localKBPath}\\${args.targetPath}\\Web\\${args.packageAPIFile}" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:KBGAMDirectory="${args.localKBPath}" \
            /p:KBEnvironment="${args.environmentName}" \
            /p:ModelDir="${args.targetPath}" \
            /p:Generator="${args.generatedLanguage}" \
            /p:DBMS="${args.dataSource}" \
            /p:GenerateLibraryPath="${args.deployTarget}" \
            /p:PackagerResources="${args.packagerResourcesDirPath}" \
            /t:${args.packageTarget}
        """
        // ----------------------------- Rename package for nuget standar
        args.packageName = powershell script: """
            \$ErrorActionPreference = 'Stop'
            \$packageFileName = (Get-ChildItem -Path "${args.deployTarget}" -Filter '*.zip').Name
            write-host "Package Name:: \$packageFileName"
            \$packageFullPath = Join-Path "${args.deployTarget}" "\$packageFileName"
            write-host "Package Full Path:: \$packageFullPath"
            Rename-Item -Path \$packageFullPath -NewName \$packageFileName.replace('GAM', '') -Force
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

        // ----------------------------- Publish NuGet package
        args.moduleServerSource = "${args.moduleServerSourceBase}${args.artifactsServerId}"
        gxLibDeployEngine.publishNuGetPackage(args)
        
        return "${args.componentId}.${args.packageName}"
        
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}


void buildNoStandardNetFWPlatforms(Map envArgs = [:]) {
    try {
        // // -------------------------- Net Framework - DB2 ISeries
        // envArgs.dataSource = 'DB2ISeries'
        // envArgs.dbmsModelConst = 'DB2400'
        // buildNoStandardNetFWPlatform(envArgs)
        // // -------------------------- Net Framework - DB2 Common
        // envArgs.dataSource = 'DB2UDB'
        // envArgs.dbmsModelConst = 'DB2Common'
        // buildNoStandardNetFWPlatform(envArgs)
        // // -------------------------- Net Framework - Informix
        // envArgs.dataSource = 'Informix'
        // envArgs.dbmsModelConst = 'Informix'
        // buildNoStandardNetFWPlatform(envArgs)
        // // -------------------------- Net Framework - Oracle 11
        // envArgs.dataSource = 'Oracle'
        // envArgs.dbmsModelConst = 'Oracle'
        // envArgs.dbmsVersion = '12c or higher'
        // buildNoStandardNetFWPlatform(envArgs)
        // // -------------------------- Net Framework - Oracle 9
        // envArgs.dataSource = 'Oracle9to11g'
        // envArgs.dbmsModelConst = 'Oracle'
        // envArgs.dbmsVersion = '9 to 11g'
        // buildNoStandardNetFWPlatform(envArgs)
        // // -------------------------- Net Framework - Postgre
        // envArgs.dataSource = 'PostgreSQL'
        // envArgs.dbmsModelConst = 'POSTGRESQL'
        // buildNoStandardNetFWPlatform(envArgs)
        // // -------------------------- Net Framework - SAP Hana
        // envArgs.dataSource = 'SapHana'
        // envArgs.dbmsModelConst = 'HANA'
        // buildNoStandardNetFWPlatform(envArgs)
        echo "COMMENT PLATFORMS"
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
void buildNoStandardNetFWPlatform(Map envArgs = [:]) {
    try{
        def sysLibHelper = new FileHelper()
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()

        stage("Prepare ENV") {
            kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
            envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            if(envArgs.dbmsModelConst == 'Oracle' && envArgs.dbmsVersion) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "Oracle version", envArgs.dbmsVersion)
                kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Oracle version", envArgs.dbmsVersion)
            }            
        }
        stage("Build Platform ${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            // ----------------------------- Clean target path
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -ItemType Directory
            """
            //----------------------------- Mark DB Reorganized
            markDBReorganized(envArgs)
            //----------------------------- Apply ExternalObjectGenerator Pattern
            envArgs.patternName = "ExternalObjectGenerator"
            applyPattern(envArgs)
            //----------------------------- Build Configuration Environment (Configuration meens avoid configure database properties)
            buildConfigurationEnvironment(envArgs)
        }
        stage("Package Platform ${envArgs.targetPath}") {
            envArgs.deployTarget = sysLibHelper.getFullPath("${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration").trim()
            // ----------------------------- Clean deployTarget
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.deployTarget}") { Remove-Item -Path "${envArgs.deployTarget}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.deployTarget}" -ItemType Directory
            """
            // ----------------------------- Package Patform
            bat script: """
                    "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.packageAPIFile}" \
                    /p:GX_PROGRAM_DIR="${envArgs.gxBasePath}" \
                    /p:KBGAMDirectory="${envArgs.localKBPath}" \
                    /p:KBEnvironment="${envArgs.environmentName}" \
                    /p:Generator="${envArgs.generatedLanguage}" \
                    /p:DBMS="${envArgs.dataSource}" \
                    /p:GenerateLibraryPath="${envArgs.deployTarget}" \
                    /p:PackagerResources="${envArgs.packagerResourcesDirPath}" \
                    /p:SolutionPath="${WORKSPACE}\\${envArgs.gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${envArgs.extObjGeneratorName}" \
                    /t:${envArgs.packageTarget}
            """
            // ----------------------------- Rename package for nuget
            envArgs.packageName = powershell script: """
                \$ErrorActionPreference = 'Stop'
                \$packageFileName = (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
                \$packageFullPath = Join-Path "${envArgs.deployTarget}" "\$packageFileName"
                Rename-Item -Path \$packageFullPath -NewName \$packageFileName.replace('-', '.') -Force
                (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
            """, returnStdout: true
            echo "[INFO] Package Name:: ${envArgs.packageName.trim()}"
            envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName.trim()}"
            echo "[INFO] Package Location:: ${envArgs.packageLocation}"
            // ----------------------------- Archive artifacts
            dir("${envArgs.deployTarget}") {
                archiveArtifacts artifacts: "${envArgs.packageName.trim()}", followSymlinks: false
            }
        }
        stage("Publish Platform ${envArgs.targetPath}") {
            // ----------------------------- Create NuGet package
            envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            envArgs.packageVersion = envArgs.componentVersion
            envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)
            // ----------------------------- Publish NuGet package
            envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            gxLibDeployEngine.publishNuGetPackage(envArgs)
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void buildNoStandardNetPlatforms(Map envArgs = [:]) {
    try {
        // -------------------------- Net - DB2 ISeries
        envArgs.dataSource = 'DB2ISeries'
        envArgs.dbmsModelConst = 'DB2400'
        buildNoStandardNetPlatform(envArgs)
        // -------------------------- Net - DB2 Common
        envArgs.dataSource = 'DB2UDB'
        envArgs.dbmsModelConst = 'DB2Common'
        buildNoStandardNetPlatform(envArgs)
        // -------------------------- Net - Informix
        envArgs.dataSource = 'Informix'
        envArgs.dbmsModelConst = 'Informix'
        buildNoStandardNetPlatform(envArgs)
        // -------------------------- Net - Oracle 11
        envArgs.dataSource = 'Oracle'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '12c or higher'
        buildNoStandardNetPlatform(envArgs)
        // -------------------------- Net - Oracle 9
        envArgs.dataSource = 'Oracle9to11g'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '9 to 11g'
        buildNoStandardNetPlatform(envArgs)
        // -------------------------- Net - Postgre
        envArgs.dataSource = 'PostgreSQL'
        envArgs.dbmsModelConst = 'POSTGRESQL'
        buildNoStandardNetPlatform(envArgs)
        // -------------------------- Net - SAP Hana
        envArgs.dataSource = 'SapHana'
        envArgs.dbmsModelConst = 'HANA'
        buildNoStandardNetPlatform(envArgs)
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
void buildNoStandardNetPlatform(Map envArgs = [:]) {
    try{
        def sysLibHelper = new FileHelper()
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()

        stage("Prepare ENV") {
            kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
            envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            if(envArgs.dbmsModelConst == 'Oracle' && envArgs.dbmsVersion) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "Oracle version", envArgs.dbmsVersion)
                kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Oracle version", envArgs.dbmsVersion)
            }
        }
        stage("Build Platform ${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            // ----------------------------- Clean target path
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -ItemType Directory
            """
            //----------------------------- Mark DB Reorganized
            markDBReorganized(envArgs)
            //----------------------------- Apply ExternalObjectGenerator Pattern
            envArgs.patternName = "ExternalObjectGenerator"
            applyPattern(envArgs)
            //----------------------------- Build Configuration Environment (Configuration meens avoid configure database properties)
            buildConfigurationEnvironment(envArgs)
        }
        stage("Package Platform ${envArgs.targetPath}") {
            envArgs.deployTarget = sysLibHelper.getFullPath("${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration").trim()
            // ----------------------------- Clean deployTarget
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.deployTarget}") { Remove-Item -Path "${envArgs.deployTarget}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.deployTarget}" -ItemType Directory
            """
            // ----------------------------- Package Patform
            bat script: """
                    "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.packageAPIFile}" \
                    /p:GX_PROGRAM_DIR="${envArgs.gxBasePath}" \
                    /p:KBGAMDirectory="${envArgs.localKBPath}" \
                    /p:KBEnvironment="${envArgs.environmentName}" \
                    /p:Generator="${envArgs.generatedLanguage}" \
                    /p:DBMS="${envArgs.dataSource}" \
                    /p:GenerateLibraryPath="${envArgs.deployTarget}" \
                    /p:PackagerResources="${envArgs.packagerResourcesDirPath}" \
                    /p:SolutionPath="${WORKSPACE}\\${envArgs.gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${envArgs.extObjGeneratorName}" \
                    /t:${envArgs.packageTarget}
            """
            // ----------------------------- Rename package for nuget
            envArgs.packageName = powershell script: """
                \$ErrorActionPreference = 'Stop'
                \$packageFileName = (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
                \$packageFullPath = Join-Path "${envArgs.deployTarget}" "\$packageFileName"
                Rename-Item -Path \$packageFullPath -NewName \$packageFileName.replace('-', '.') -Force
                (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
            """, returnStdout: true
            echo "[INFO] Package Name:: ${envArgs.packageName.trim()}"
            envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName.trim()}"
            echo "[INFO] Package Location:: ${envArgs.packageLocation}"
            // ----------------------------- Archive artifacts
            dir("${envArgs.deployTarget}") {
                archiveArtifacts artifacts: "${envArgs.packageName.trim()}", followSymlinks: false
            }
        }
        stage("Publish Platform ${envArgs.targetPath}") {
            // ----------------------------- Create NuGet package
            envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            envArgs.packageVersion = envArgs.componentVersion
            envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)
            // ----------------------------- Publish NuGet package
            envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            gxLibDeployEngine.publishNuGetPackage(envArgs)
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void buildNoStandardJavaPlatforms(Map envArgs = [:]) {
    try {
        // // -------------------------- Java - Dameng
        // envArgs.dataSource = 'Dameng'
        // envArgs.dbmsModelConst = 'Dameng'
        // buildNoStandardJavaPlatform(envArgs)
        // // -------------------------- Java - DB2 ISeries
        // envArgs.dataSource = 'DB2ISeries'
        // envArgs.dbmsModelConst = 'DB2400'
        // buildNoStandardJavaPlatform(envArgs)
        // // -------------------------- Java - DB2 Common
        // envArgs.dataSource = 'DB2UDB'
        // envArgs.dbmsModelConst = 'DB2Common'
        // buildNoStandardJavaPlatform(envArgs)
        // // -------------------------- Java - Informix
        // envArgs.dataSource = 'Informix'
        // envArgs.dbmsModelConst = 'Informix'
        // buildNoStandardJavaPlatform(envArgs)
        // -------------------------- Java - Oracle 12
        envArgs.dataSource = 'Oracle'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '12c or higher'
        buildNoStandardJavaPlatform(envArgs)
        // -------------------------- Java - Oracle 9 to 11
        envArgs.dataSource = 'Oracle9to11g'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '9 to 11g'
        buildNoStandardJavaPlatform(envArgs)
        // -------------------------- Java - Postgre
        envArgs.dataSource = 'PostgreSQL'
        envArgs.dbmsModelConst = 'POSTGRESQL'
        buildNoStandardJavaPlatform(envArgs)
        // -------------------------- Java - SAP Hana
        envArgs.dataSource = 'SapHana'
        envArgs.dbmsModelConst = 'HANA'
        buildNoStandardJavaPlatform(envArgs)
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
void buildNoStandardJavaPlatform(Map envArgs = [:]) {
    try{
        def sysLibHelper = new FileHelper()
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()
        
        stage("Prepare ENV") {
            kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
            if(envArgs.dbmsModelConst == 'Oracle' && envArgs.dbmsVersion) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "Oracle version", envArgs.dbmsVersion)
                kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Oracle version", envArgs.dbmsVersion)
            }
            envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        }
        stage("Build Platform ${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            // ----------------------------- Clean target path
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib" -ItemType Directory
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\src\\main\\java\\genexus\\security\\api" -ItemType Directory
            """
            //----------------------------- Mark DB Reorganized
            markDBReorganized(envArgs)
            //----------------------------- Apply ExternalObjectGenerator Pattern
            envArgs.patternName = "ExternalObjectGenerator"
            applyPattern(envArgs)
            //----------------------------- Sync .java files generated by ExternalObjectGenerator
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                Write-Output((Get-Date -Format G) + " [INFO] Sync ExternalObjectGenerator .java objs")
                Copy-Item -Path "${WORKSPACE}\\${envArgs.gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${envArgs.extObjGeneratorName}\\genexus\\security\\GAMSecurityProvider.java" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\GAMSecurityProvider.java" -Force -Recurse
                Copy-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\*java" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\src\\main\\java\\genexus\\security\\" -Force -Recurse
            """
            //----------------------------- Build Configuration Environment (Configuration meens avoid configure database properties)
            buildConfigurationEnvironment(envArgs)
        }
        stage("Package Platform ${envArgs.targetPath}") {
            envArgs.deployTarget = sysLibHelper.getFullPath("${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration").trim()
            // ----------------------------- Clean deployTarget
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.deployTarget}") { Remove-Item -Path "${envArgs.deployTarget}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.deployTarget}" -ItemType Directory
            """
            // ----------------------------- Package Patform
            bat script: """
                    "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.packageAPIFile}" \
                    /p:GX_PROGRAM_DIR="${envArgs.gxBasePath}" \
                    /p:KBGAMDirectory="${envArgs.localKBPath}" \
                    /p:KBEnvironment="${envArgs.environmentName}" \
                    /p:Generator="${envArgs.generatedLanguage}" \
                    /p:DBMS="${envArgs.dataSource}" \
                    /p:GenerateLibraryPath="${envArgs.deployTarget}" \
                    /p:PackagerResources="${envArgs.packagerResourcesDirPath}" \
                    /p:SolutionPath="${WORKSPACE}\\${envArgs.gamAPIResourcesRepository}\\Solutions\\ExternalObject\\${envArgs.extObjGeneratorName}" \
                    /t:${envArgs.packageTarget}
            """
            // ----------------------------- Rename package for nuget
            envArgs.packageName = powershell script: """
                \$ErrorActionPreference = 'Stop'
                \$packageFileName = (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
                \$packageFullPath = Join-Path "${envArgs.deployTarget}" "\$packageFileName"
                Rename-Item -Path \$packageFullPath -NewName \$packageFileName.replace('-', '.') -Force
                (Get-ChildItem -Path "${envArgs.deployTarget}" -Filter '*.zip').Name
            """, returnStdout: true
            echo "[INFO] Package Name:: ${envArgs.packageName.trim()}"
            envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName.trim()}"
            echo "[INFO] Package Location:: ${envArgs.packageLocation}"
            // ----------------------------- Archive artifacts
            dir("${envArgs.deployTarget}") {
                archiveArtifacts artifacts: "${envArgs.packageName.trim()}", followSymlinks: false
            }
        }
        stage("Publish Platform ${envArgs.targetPath}") {
            // ----------------------------- Create NuGet package
            envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            envArgs.packageVersion = envArgs.componentVersion
            envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

            // ----------------------------- Publish NuGet package
            envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            gxLibDeployEngine.publishNuGetPackage(envArgs)
        }

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
