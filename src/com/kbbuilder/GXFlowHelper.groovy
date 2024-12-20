package com.kbbuilder
import com.genexus.FileHelper
import com.genexus.GeneXusHelper
import com.genexus.PropertiesHelper
import com.genexus.GXDeployEngineHelper

void setDataSourceVersion(Map args = [:]) {
    try {
        if(args.dbmsVersion) {
            def kbLibHelper = new PropertiesHelper()
            String propertyName
            switch (args.dataSource) {
                case 'DB2ISeries':
                    propertyName =  "OS for ISeries version"
                    break
                case 'SQL':
                    propertyName =  "SQL Server version"
                    break
                case 'Oracle9to11g':
                    propertyName =  "Oracle version"
                    break
                case 'SapHana':
                    propertyName =  "SAP Hana version"
                    break
                default:
                    propertyName =  "${args.dataSource} version"
                    break
            }
            kbLibHelper.setDataStoreProperty(args, "Default", propertyName, args.dbmsVersion)
            kbLibHelper.setDataStoreProperty(args, "GAM", propertyName, args.dbmsVersion)
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Return packageName for packageList in dispatch
 */
void buildNoStandardNetFWPlatforms(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try {
        // -------------------------- NetFW - MySQL
        envArgs.dataSource = 'mysql5'
        envArgs.dbmsModelConst = 'MySQL'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // // -------------------------- NetFW - DB2 ISeries
        // envArgs.dataSource = 'DB2ISeries'
        // envArgs.dbmsModelConst = 'DB2400'
        // envArgs.dbmsVersion = 'V6R1 to V7R1'
        // envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetDB2ISeries'
        // envArgs.gamPlatformVersion = '18.11.41'
        // envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetDB2ISeries'
        // envArgs.reorgPlatformVersion = '18.11.0'
        // integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - DB2 Common
        envArgs.dataSource = 'DB2UDB'
        envArgs.dbmsModelConst = 'DB2Common'
        envArgs.dbmsVersion = '8.0 to 10.1'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetDB2UDB'
        envArgs.gamPlatformVersion = '18.11.41'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetDB2UDB'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - Informix
        envArgs.dataSource = 'Informix'
        envArgs.dbmsModelConst = 'Informix'
        envArgs.dbmsVersion = '11 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetInformix'
        envArgs.gamPlatformVersion = '18.11.41'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetInformix'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - Oracle 12
        envArgs.dataSource = 'Oracle'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '12c or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetOracle'
        envArgs.gamPlatformVersion = '18.11.100'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetOracle'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - Oracle 9 to 11
        envArgs.dataSource = 'Oracle9to11g'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '9 to 11g'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetOracle9to11g'
        envArgs.gamPlatformVersion = '18.11.41'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetOracle9to11g'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - Postgres
        envArgs.dataSource = 'PostgreSQL'
        envArgs.dbmsModelConst = 'POSTGRESQL'
        envArgs.dbmsVersion = '8.3 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetPostgreSQL'
        envArgs.gamPlatformVersion = '18.11.41'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetPostgreSQL'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - SAP Hana
        envArgs.dataSource = 'SapHana'
        envArgs.dbmsModelConst = 'HANA'
        envArgs.dbmsVersion = '1.0 SPS 11 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetSapHana'
        envArgs.gamPlatformVersion = '18.11.41'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetSapHana'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
/**
 * Return packageName for packageList in dispatch
 */
String integrateNetFWPlatform(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try{
        // ------------------------ INIT IMPORT ------------------------ 
        def sysLibHelper = new FileHelper()
        def gxLibHelper = new GeneXusHelper()
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()
        // ------------------------ DOWNLOAD PLATFORM ------------------------ 
        if(envArgs.gamPlatformId) {
            stage("Download GAM Platform") {
                gxLibHelper.downloadNugetPackage(envArgs.gxBasePath, envArgs.gamPlatformId, envArgs.gamPlatformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
            }
        }
        stage("Prepare ENV") {
            kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
            envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            clientDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            engineDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            if(envArgs.dbmsModelConst == 'Oracle') {
                kbLibHelper.setGeneratorProperty(envArgs, "Default", "Initialize_not_referenced_attributes", "No")
            }
            setDataSourceVersion(envArgs)
            if(envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' || envArgs.dataSource == 'SapHana' || envArgs.dataSource == 'Dameng' ) {
                kbLibHelper.resetDataStoreProperty(envArgs, "Default", "Database schema")
                kbLibHelper.resetDataStoreProperty(envArgs, "GAM", "Database schema")
            }
            if(envArgs.dataSource == 'DB2UDB' || envArgs.dataSource == 'Informix' ||   envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql' ) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "Database schema", "gam")
                kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Database schema", "gam")
            }
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "Declare referential integrity", "No")
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Declare referential integrity", "No")
            // ------------------------ SYNC FREEZE FILES ------------------------ 
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                # --------------------- Remove Model Dir
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -ItemType Directory

                # --------------------- TODO: Update reference to circle dependency
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${WORKSPACE}\\Libs\\Net\\*")
                Copy-Item -Path "${WORKSPACE}\\Libs\\Net\\*" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\" -Force -Recurse
                
                # --------------------- Sync files from GeneXus Installation
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\dotNet\\GXflow.Programs.Rules.dll")
                copy-item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\dotNet\\GXflow.Programs.Rules.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\GXflow.Programs.Rules.dll" -Force
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\dotNet\\wfcache.dll")
                copy-item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\dotNet\\wfcache.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\wfcache.dll" -Force
                Write-Output((Get-Date -Format G) + " [INFO] Extract NetProt.zip")
                Expand-Archive -Path "${envArgs.gxBasePath}\\Packages\\GXPM\\Protection\\NetProt.zip" -DestinationPath "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -Force                        
                
                # --------------------- Create VirtualDirCreationDisabled file
                Write-Output((Get-Date -Format G) + " [INFO] Create VirtualDirCreationDisabled file")
                if(-not (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled")) {\$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled"}
            """
        }
        // ------------------------ BUILD ENVIRONMENT ------------------------ 
        stage("Build ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
            kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "HTML5")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "isolation_level", "Read Uncommitted")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", ".Net Application Namespace", "GXflow.Programs")
            if(envArgs.dataSource == 'db2common' || envArgs.dataSource == 'Informix' || envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' || envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql2012' ) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "database schema", "GXFLOW_SCHEMA")
            }
            buildConfigurationEnvironment(envArgs)

            // //------------------ W.A for connection.gam
            bat label: 'Create connection.gam',
                script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
        }
        // ------------------------ MAKE DB SCHEMA DYNAMIC ------------------------ 
        stage("Make DB Schema Dynamic") {
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:KBEnvironment="${envArgs.environmentName}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:TargetDbms="${envArgs.dataSource}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /t:MakeDBSchemaDynamic
            """
        }
        // ------------------------ PACKAGE PLATFORM ------------------------ 
        stage("Package ENV:${envArgs.targetPath} Platform") {
            // ----------------------------- Package Platform resources
            envArgs.deployTarget = "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration"
            powershell script: """
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration" -Recurse -Force }
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline" -Recurse -Force }
            """
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:KBEnvironment="${envArgs.environmentName}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:TargetDbms="${envArgs.dataSource}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
                /t:PackageWorkflow
            """
            // ----------------------------- Create Package for DU:Client
            clientDuArgs.packageLocation = packageLocalDU(clientDuArgs)
            echo "INFO DU Package Location:: ${clientDuArgs.packageLocation}"
            clientDuArgs.packageName = "WF${clientDuArgs.duName}.zip"
            echo "INFO Package Name:: ${clientDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${clientDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${clientDuArgs.targetPath}\\${clientDuArgs.packageName}"
                Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
                Split-Path "${clientDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
            }
            // ----------------------------- Create Package for DU:Engine
            engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
            echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
            engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
            echo "INFO Package Name:: ${engineDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
                Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
                Split-Path "${engineDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
            }
            // ----------------------------- Generate files extra from WFClient n WFEngine local package
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
                /t:GenerateGXDeployDescriptor
            """
            // ----------------------------- Zip package
            envArgs.packageName = "Platform.${envArgs.generatedLanguage}${envArgs.dataSource}.zip"
            powershell script: """
                & 7z a -tzip "${envArgs.deployTarget}\\${envArgs.packageName}" "${envArgs.deployTarget}\\Packages"
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
            // ----------------------------- Add Reorganization files
            if(envArgs.reorgPlatformId) {
                gxLibHelper.downloadNugetPackage(envArgs.deployTarget, envArgs.reorgPlatformId, envArgs.reorgPlatformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
                powershell script: """
                Write-Host "[DEBUG] INPUT: envArgs.packageLocation::${envArgs.packageLocation}"
                Write-Host "[DEBUG] INPUT: Packages::${envArgs.deployTarget}\\Packages"
                    & 'C:\\Program Files\\7-Zip\\7z.exe' a "${envArgs.packageLocation}" "${envArgs.deployTarget}\\Packages"
                """
            }
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
            // // ----------------------------- Create NuGet package
            // envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
            // envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            // envArgs.packageVersion = envArgs.componentVersion
            // envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

            // // ----------------------------- Publish NuGet package
            // envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            // gxLibDeployEngine.publishNuGetPackage(envArgs)
        // }
        def ret = "${envArgs.componentId}.${envArgs.packageName}"
        return ret
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }    
}
/**
 * Return packageName for packageList in dispatch
 */
void buildNoStandardJavaPlatforms(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try {
        // -------------------------- Java - MySQL
        envArgs.dataSource = 'mysql5'
        envArgs.dbmsModelConst = 'MySQL'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Dameng
        envArgs.dataSource = 'Dameng'
        envArgs.dbmsModelConst = 'Dameng'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.JavaDameng'
        envArgs.gamPlatformVersion = '18.11.43'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.JavaDameng'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - DB2 ISeries
        envArgs.dataSource = 'DB2ISeries'
        envArgs.dbmsModelConst = 'DB2400'
        envArgs.dbmsVersion = 'V6R1 to V7R1'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.JavaDB2ISeries'
        envArgs.gamPlatformVersion = '18.11.43'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.JavaDB2ISeries'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - DB2 Common
        envArgs.dataSource = 'DB2UDB'
        envArgs.dbmsModelConst = 'DB2Common'
        envArgs.dbmsVersion = '8.0 to 10.1'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.JavaDB2UDB'
        envArgs.gamPlatformVersion = '18.11.43'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.JavaDB2UDB'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Informix
        envArgs.dataSource = 'Informix'
        envArgs.dbmsModelConst = 'Informix'
        envArgs.dbmsVersion = '11 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.JavaInformix'
        envArgs.gamPlatformVersion = '18.11.43'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.JavaInformix'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Oracle 12
        envArgs.dataSource = 'Oracle'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '12c or higher'
        envArgs.dbmsVersion = '12c or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.JavaOracle'
        envArgs.gamPlatformVersion = '18.11.100'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.JavaOracle'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Oracle 9 to 11
        envArgs.dataSource = 'Oracle9to11g'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '9 to 11g'
        envArgs.dbmsVersion = '9 to 11g'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.JavaOracle9to11g'
        envArgs.gamPlatformVersion = '18.11.47'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.JavaOracle9to11g'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Postgres
        envArgs.dataSource = 'PostgreSQL'
        envArgs.dbmsModelConst = 'POSTGRESQL'
        envArgs.dbmsVersion = '8.3 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.JavaPostgreSQL'
        envArgs.gamPlatformVersion = '18.11.47'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.JavaPostgreSQL'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - SAP Hana
        envArgs.dataSource = 'SapHana'
        envArgs.dbmsModelConst = 'HANA'
        envArgs.dbmsVersion = '1.0 SPS 11 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.JavaSapHana'
        envArgs.gamPlatformVersion = '18.11.47'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.JavaSapHana'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
/**
 * Return packageName for packageList in dispatch
 */
String integrateJavaPlatform(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try{
        // ------------------------ INIT IMPORT ------------------------ 
        def sysLibHelper = new FileHelper()
        def gxLibHelper = new GeneXusHelper()
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()
        // ------------------------ DOWNLOAD PLATFORM ------------------------ 
        if(envArgs.gamPlatformId) {
            stage("Download GAM Platform") {
                gxLibHelper.downloadNugetPackage(envArgs.gxBasePath, envArgs.gamPlatformId, envArgs.gamPlatformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
            }
        }
        stage("Prepare ENV") {
            kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
            envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            clientDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            engineDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            if(envArgs.dbmsModelConst == 'Oracle') {
                kbLibHelper.setGeneratorProperty(envArgs, "Default", "Initialize_not_referenced_attributes", "No")
            }
            setDataSourceVersion(envArgs)
            if(envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' || envArgs.dataSource == 'SapHana' || envArgs.dataSource == 'Dameng' ) {
                kbLibHelper.resetDataStoreProperty(envArgs, "Default", "Database schema")
                kbLibHelper.resetDataStoreProperty(envArgs, "GAM", "Database schema")
            }
            if(envArgs.dataSource == 'DB2UDB' || envArgs.dataSource == 'Informix' ||   envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql' ) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "Database schema", "gam")
                kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Database schema", "gam")
            }
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "Declare referential integrity", "No")
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Declare referential integrity", "No")
            // ------------------------ SYNC FREEZE FILES ------------------------ 
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib" -ItemType Directory

                # --------------------- TODO: Update reference to circle dependency
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${WORKSPACE}\\Libs\\Java\\*")
                Copy-Item -Path "${WORKSPACE}\\Libs\\Java\\*" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib\\" -Force -Recurse
                
                # --------------------- Sync files from GeneXus Installation
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\Java\\com.gxflow.rules.jar")
                Copy-Item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\Java\\com.gxflow.rules.jar" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib\\" -Force -Recurse
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\Java\\wfcache.jar")
                Copy-Item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\Java\\wfcache.jar" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib\\" -Force -Recurse
                Write-Output((Get-Date -Format G) + " [INFO] Extract JavaProt.zip")
                Expand-Archive -Path "${envArgs.gxBasePath}\\Packages\\GXPM\\Protection\\JavaProt.zip" -DestinationPath "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib" -Force
                
                # --------------------- Create VirtualDirCreationDisabled file
                Write-Output((Get-Date -Format G) + " [INFO] Create VirtualDirCreationDisabled file in modelDir")
                if(-not (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled")) {\$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled"}
            """
        }
        // ------------------------ BUILD ENVIRONMENT ------------------------
        stage("Build ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
            kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "HTML5")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "java package name", "com.gxflow")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "generate prompt programs", "No")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "compiler_options", "-J-Xms1024m -J-Xmx2048m -O -source 1.8 -target 1.8")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema -donotexecute")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "Java platform support", "Both Platforms")
            if(envArgs.dataSource == 'db2common' || envArgs.dataSource == 'Informix' || envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' || envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql2012' ) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "database schema", "GXFLOW_SCHEMA")
            }
            
            buildConfigurationEnvironment(envArgs)

            // //------------------ W.A for connection.gam
            bat label: 'Create connection.gam',
                script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
        }
        // ------------------------ MAKE DB SCHEMA DYNAMIC ------------------------ 
        stage("Make DB Schema Dynamic") {
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:KBEnvironment="${envArgs.environmentName}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:TargetDbms="${envArgs.dataSource}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /t:MakeDBSchemaDynamic
            """
        }
        // ------------------------ PACKAGE PLATFORM ------------------------ 
        stage("Package ENV:${envArgs.targetPath} Platform") {
            // ----------------------------- Package Platform resources
            envArgs.deployTarget = "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration"
            powershell script: """
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration" -Recurse -Force }
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline" -Recurse -Force }
            """
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:KBEnvironment="${envArgs.environmentName}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:TargetDbms="${envArgs.dataSource}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
                /t:PackageWorkflow
            """
            // ----------------------------- Create Package for DU:Client
            clientDuArgs.packageLocation = packageLocalDU(clientDuArgs)
            echo "INFO DU Package Location:: ${clientDuArgs.packageLocation}"
            clientDuArgs.packageName = "WF${clientDuArgs.duName}.zip"
            echo "INFO Package Name:: ${clientDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${clientDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${clientDuArgs.targetPath}\\${clientDuArgs.packageName}"
                Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
                Split-Path "${clientDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
            }
            // ----------------------------- Create Package for DU:Engine
            engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
            echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
            engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
            echo "INFO Package Name:: ${engineDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
                Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
                Split-Path "${engineDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
            }
            // ----------------------------- Generate files extra from WFClient n WFEngine local package
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:TargetDbms="${envArgs.dataSource}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
                /t:GenerateGXDeployDescriptor
            """
            // ----------------------------- Zip package
            envArgs.packageName = "Platform.${envArgs.generatedLanguage}${envArgs.dataSource}.zip"
            powershell script: """
                & 7z a -tzip "${envArgs.deployTarget}\\${envArgs.packageName}" "${envArgs.deployTarget}\\Packages"
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
            // ----------------------------- Add Reorganization files
            if(envArgs.reorgPlatformId) {
                gxLibHelper.downloadNugetPackage(envArgs.deployTarget, envArgs.reorgPlatformId, envArgs.reorgPlatformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
                powershell script: """
                Write-Host "[DEBUG] INPUT: envArgs.packageLocation::${envArgs.packageLocation}"
                Write-Host "[DEBUG] INPUT: Packages::${envArgs.deployTarget}\\Packages"
                    & 'C:\\Program Files\\7-Zip\\7z.exe' a "${envArgs.packageLocation}" "${envArgs.deployTarget}\\Packages"
                """
            }
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
            // // ----------------------------- Create NuGet package
            // envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
            // envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            // envArgs.packageVersion = envArgs.componentVersion
            // envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

            // // ----------------------------- Publish NuGet package
            // envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            // gxLibDeployEngine.publishNuGetPackage(envArgs)
        // }
        def ret = "${envArgs.componentId}.${envArgs.packageName}"
        return ret
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
/**
 * Return packageName for packageList in dispatch
 */
void buildNoStandardNetPlatforms(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try {
        // -------------------------- NetCore - MySQL
        envArgs.dataSource = 'mysql5'
        envArgs.dbmsModelConst = 'MySQL'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - DB2 ISeries
        envArgs.dataSource = 'DB2ISeries'
        envArgs.dbmsModelConst = 'DB2400'
        envArgs.dbmsVersion = 'V6R1 to V7R1'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetCoreDB2ISeries'
        envArgs.gamPlatformVersion = '18.11.43'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetCoreDB2ISeries'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - DB2 Common
        envArgs.dataSource = 'DB2UDB'
        envArgs.dbmsModelConst = 'DB2Common'
        envArgs.dbmsVersion = '8.0 to 10.1'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetCoreDB2UDB'
        envArgs.gamPlatformVersion = '18.11.43'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetCoreDB2UDB'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - Informix
        envArgs.dataSource = 'Informix'
        envArgs.dbmsModelConst = 'Informix'
        envArgs.dbmsVersion = '11 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetCoreInformix'
        envArgs.gamPlatformVersion = '18.11.43'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetCoreInformix'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - Oracle 12
        envArgs.dataSource = 'Oracle'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '12c or higher'
        envArgs.dbmsVersion = '12c or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetCoreOracle'
        envArgs.gamPlatformVersion = '18.11.100'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetCoreOracle'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - Oracle 9 to 11
        envArgs.dataSource = 'Oracle9to11g'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '9 to 11g'
        envArgs.dbmsVersion = '9 to 11g'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetCoreOracle9to11g'
        envArgs.gamPlatformVersion = '18.11.47'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetCoreOracle9to11g'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - Postgres
        envArgs.dataSource = 'PostgreSQL'
        envArgs.dbmsModelConst = 'POSTGRESQL'
        envArgs.dbmsVersion = '8.3 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetCorePostgreSQL'
        envArgs.gamPlatformVersion = '18.11.47'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetCorePostgreSQL'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - SAP Hana
        envArgs.dataSource = 'SapHana'
        envArgs.dbmsModelConst = 'HANA'
        envArgs.dbmsVersion = '1.0 SPS 11 or higher'
        envArgs.gamPlatformId = 'GXDeps.GAM.Platform.NetCoreSapHana'
        envArgs.gamPlatformVersion = '18.11.47'
        envArgs.reorgPlatformId = 'GXDeps.GXFlow.Reorgs.NetCoreSapHana'
        envArgs.reorgPlatformVersion = '18.11.0'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
/**
 * Return packageName for packageList in dispatch
 */
String integrateNetPlatform(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try{
        // ------------------------ INIT IMPORT ------------------------ 
        def sysLibHelper = new FileHelper()
        def gxLibHelper = new GeneXusHelper()
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()
        // ------------------------ DOWNLOAD PLATFORM ------------------------ 
        if(envArgs.gamPlatformId) {
            stage("Download GAM Platform") {
                gxLibHelper.downloadNugetPackage(envArgs.gxBasePath, envArgs.gamPlatformId, envArgs.gamPlatformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
            }
        }
        stage("Prepare ENV") {
            kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
            envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            clientDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            engineDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            if(envArgs.dbmsModelConst == 'Oracle') {
                kbLibHelper.setGeneratorProperty(envArgs, "Default", "Initialize_not_referenced_attributes", "No")
            }
            setDataSourceVersion(envArgs)
            if(envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' || envArgs.dataSource == 'SapHana' || envArgs.dataSource == 'Dameng' ) {
                kbLibHelper.resetDataStoreProperty(envArgs, "Default", "Database schema")
                kbLibHelper.resetDataStoreProperty(envArgs, "GAM", "Database schema")
            }
            if(envArgs.dataSource == 'DB2UDB' || envArgs.dataSource == 'Informix' ||   envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql' ) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "Database schema", "gam")
                kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Database schema", "gam")
            }
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "Declare referential integrity", "No")
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Declare referential integrity", "No")
            // ------------------------ SYNC FREEZE FILES ------------------------ 
            powershell script: """
                \$ErrorActionPreference = 'Stop'
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
                \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -ItemType Directory

                # --------------------- TODO: Update reference to circle dependency
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${WORKSPACE}\\Libs\\NetCore\\*")
                Copy-Item -Path "${WORKSPACE}\\Libs\\NetCore\\*.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\" -Force -Recurse
                
                # --------------------- Sync files from GeneXus Installation
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\dotNetCore\\GXflow.Programs.Rules.dll")
                Copy-Item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\dotNetCore\\GXflow.Programs.Rules.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\GXflow.Programs.Rules.dll" -Force -Recurse
                Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\dotNetCore\\wfcache.dll")
                Copy-Item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\dotNetCore\\wfcache.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\wfcache.dll" -Force -Recurse
                Write-Output((Get-Date -Format G) + " [INFO] Extract NetProtCore.zip")
                Expand-Archive -Path "${envArgs.gxBasePath}\\Packages\\GXPM\\Protection\\NetProtCore.zip" -DestinationPath "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -Force                        
                
                # --------------------- Create VirtualDirCreationDisabled file
                Write-Output((Get-Date -Format G) + " [INFO] Create VirtualDirCreationDisabled file in modelDir")
                if(-not (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled")) {\$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled"}
            """
        }
        // ------------------------ BUILD ENVIRONMENT ------------------------v
        stage("Build ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
            kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "Do not specify")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema -donotexecute")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", ".net_application_namespace", "GXflow.Programs")                    
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "isolation_level", "Read Uncommitted")
            if(envArgs.dataSource == 'db2common' || envArgs.dataSource == 'Informix' || envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' || envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql2012' ) {
                kbLibHelper.setDataStoreProperty(envArgs, "Default", "database schema", "GXFLOW_SCHEMA")
            }
            
            buildConfigurationEnvironment(envArgs)

            // //------------------ W.A for connection.gam
            bat label: 'Create connection.gam',
                script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
        }
        // ------------------------ MAKE DB SCHEMA DYNAMIC ------------------------ 
        stage("Make DB Schema Dynamic") {
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:KBEnvironment="${envArgs.environmentName}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:TargetDbms="${envArgs.dataSource}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /t:MakeDBSchemaDynamic
            """
        }
        // ------------------------ PACKAGE PLATFORM ------------------------ 
        stage("Package ENV:${envArgs.targetPath} Platform") {
            // ----------------------------- Package Platform resources
            envArgs.deployTarget = "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration"
            powershell script: """
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration" -Recurse -Force }
                if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline" -Recurse -Force }
            """
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:KBEnvironment="${envArgs.environmentName}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:TargetDbms="${envArgs.dataSource}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
                /t:PackageWorkflow
            """
            // ----------------------------- Create Package for DU:Client
            clientDuArgs.packageLocation = packageLocalDU(clientDuArgs)
            echo "INFO DU Package Location:: ${clientDuArgs.packageLocation}"
            clientDuArgs.packageName = "WF${clientDuArgs.duName}.zip"
            echo "INFO Package Name:: ${clientDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${clientDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${clientDuArgs.targetPath}\\${clientDuArgs.packageName}"
                Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
                Split-Path "${clientDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
            }
            // ----------------------------- Create Package for DU:Engine
            engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
            echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
            engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
            echo "INFO Package Name:: ${engineDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
                Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
                Split-Path "${engineDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
            }
            // ----------------------------- Generate files extra from WFClient n WFEngine local package
            bat script: """
                "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
                /p:GXInstall="${envArgs.gxBasePath}" \
                /p:KBFolder="${envArgs.localKBPath}" \
                /p:TargetLanguage="${envArgs.generatedLanguage}" \
                /p:TargetDbms="${envArgs.dataSource}" \
                /p:KBEnvironmentPath="${envArgs.targetPath}" \
                /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
                /t:GenerateGXDeployDescriptor
            """
            // ----------------------------- Zip package
            envArgs.packageName = "Platform.${envArgs.generatedLanguage}${envArgs.dataSource}.zip"
            powershell script: """
                & 7z a -tzip "${envArgs.deployTarget}\\${envArgs.packageName}" "${envArgs.deployTarget}\\Packages"
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
            // ----------------------------- Add Reorganization files
            if(envArgs.reorgPlatformId) {
                gxLibHelper.downloadNugetPackage(envArgs.deployTarget, envArgs.reorgPlatformId, envArgs.reorgPlatformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
                powershell script: """
                Write-Host "[DEBUG] INPUT: envArgs.packageLocation::${envArgs.packageLocation}"
                Write-Host "[DEBUG] INPUT: Packages::${envArgs.deployTarget}\\Packages"
                    & 'C:\\Program Files\\7-Zip\\7z.exe' a "${envArgs.packageLocation}" "${envArgs.deployTarget}\\Packages"
                """
            }
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
        //     // ----------------------------- Create NuGet package
        //     envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
        //     envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
        //     envArgs.packageVersion = envArgs.componentVersion
        //     envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

        //     // ----------------------------- Publish NuGet package
        //     envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
        //     gxLibDeployEngine.publishNuGetPackage(envArgs)
        // }
        def ret = "${envArgs.componentId}.${envArgs.packageName}"
        return ret

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
return this
