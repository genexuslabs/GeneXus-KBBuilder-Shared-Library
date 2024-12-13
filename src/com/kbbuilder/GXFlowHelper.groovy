package com.kbbuilder
import com.genexus.FileHelper
import com.genexus.GeneXusHelper
import com.genexus.PropertiesHelper
import com.genexus.GXDeployEngineHelper

/**
 * Return packageName for packageList in dispatch
 */
void buildNoStandardNetFWPlatforms(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try {
        // -------------------------- NetFW - MySQL
        envArgs.dataSource = 'mysql5'
        envArgs.dbmsModelConst = 'MySQL'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - DB2 ISeries
        envArgs.dataSource = 'DB2ISeries'
        envArgs.dbmsModelConst = 'DB2400'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetDB2ISeries'
        envArgs.platformVersion = '18.11.41'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - DB2 Common
        envArgs.dataSource = 'DB2UDB'
        envArgs.dbmsModelConst = 'DB2Common'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetDB2UDB'
        envArgs.platformVersion = '18.11.41'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - Informix
        envArgs.dataSource = 'Informix'
        envArgs.dbmsModelConst = 'Informix'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetInformix'
        envArgs.platformVersion = '18.11.41'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - Oracle 12
        envArgs.dataSource = 'Oracle'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '12c or higher'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetOracle'
        envArgs.platformVersion = '18.11.41'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - Oracle 9 to 11
        envArgs.dataSource = 'Oracle9to11g'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '9 to 11g'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetOracle9to11g'
        envArgs.platformVersion = '18.11.41'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - Postgres
        envArgs.dataSource = 'PostgreSQL'
        envArgs.dbmsModelConst = 'POSTGRESQL'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetPostgreSQL'
        envArgs.platformVersion = '18.11.41'
        integrateNetFWPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetFW - SAP Hana
        envArgs.dataSource = 'SapHana'
        envArgs.dbmsModelConst = 'HANA'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetSapHana'
        envArgs.platformVersion = '18.11.41'
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
        if(envArgs.platformId) {
            stage("Download GAM Platform") {
                gxLibHelper.downloadNugetPackage(envArgs.gxBasePath, envArgs.platformId, envArgs.platformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
            }
        }
        // ------------------------ PREPARE ENVIRONMENT ------------------------ 
        kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
        kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
        kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
        if(envArgs.dbmsModelConst == 'Oracle' && envArgs.dbmsVersion) {
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "Oracle version", envArgs.dbmsVersion)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Oracle version", envArgs.dbmsVersion)
        }
        envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        clientDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        engineDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
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
            // ----------------------------- Create NuGet package
            envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
            envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            envArgs.packageVersion = envArgs.componentVersion
            envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

            // // ----------------------------- Publish NuGet package
            // envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            // gxLibDeployEngine.publishNuGetPackage(envArgs)
        }
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
        envArgs.platformId = 'GXDeps.GAM.Platform.JavaDameng'
        envArgs.platformVersion = '18.11.43'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - DB2 ISeries
        envArgs.dataSource = 'DB2ISeries'
        envArgs.dbmsModelConst = 'DB2400'
        envArgs.platformId = 'GXDeps.GAM.Platform.JavaDB2ISeries'
        envArgs.platformVersion = '18.11.43'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - DB2 Common
        envArgs.dataSource = 'DB2UDB'
        envArgs.dbmsModelConst = 'DB2Common'
        envArgs.platformId = 'GXDeps.GAM.Platform.JavaDB2UDB'
        envArgs.platformVersion = '18.11.43'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Informix
        envArgs.dataSource = 'Informix'
        envArgs.dbmsModelConst = 'Informix'
        envArgs.platformId = 'GXDeps.GAM.Platform.JavaInformix'
        envArgs.platformVersion = '18.11.43'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Oracle 12
        envArgs.dataSource = 'Oracle'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '12c or higher'
        envArgs.platformId = 'GXDeps.GAM.Platform.JavaOracle'
        envArgs.platformVersion = '18.11.47'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Oracle 9 to 11
        envArgs.dataSource = 'Oracle9to11g'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '9 to 11g'
        envArgs.platformId = 'GXDeps.GAM.Platform.JavaOracle9to11g'
        envArgs.platformVersion = '18.11.47'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - Postgres
        envArgs.dataSource = 'PostgreSQL'
        envArgs.dbmsModelConst = 'POSTGRESQL'
        envArgs.platformId = 'GXDeps.GAM.Platform.JavaPostgreSQL'
        envArgs.platformVersion = '18.11.47'
        integrateJavaPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- Java - SAP Hana
        envArgs.dataSource = 'SapHana'
        envArgs.dbmsModelConst = 'HANA'
        envArgs.platformId = 'GXDeps.GAM.Platform.JavaSapHana'
        envArgs.platformVersion = '18.11.47'
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
        if(envArgs.platformId) {
            stage("Download GAM Platform") {
                gxLibHelper.downloadNugetPackage(envArgs.gxBasePath, envArgs.platformId, envArgs.platformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
            }
        }
        // ------------------------ PREPARE ENVIRONMENT ------------------------ 
        kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
        kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
        kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
        if(envArgs.dbmsModelConst == 'Oracle' && envArgs.dbmsVersion) {
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "Oracle version", envArgs.dbmsVersion)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Oracle version", envArgs.dbmsVersion)
        }
        envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        clientDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        engineDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
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
            // ----------------------------- Create NuGet package
            envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
            envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            envArgs.packageVersion = envArgs.componentVersion
            envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

            // // ----------------------------- Publish NuGet package
            // envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            // gxLibDeployEngine.publishNuGetPackage(envArgs)
        }
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
        // -------------------------- NetCore - Dameng
        envArgs.dataSource = 'Dameng'
        envArgs.dbmsModelConst = 'Dameng'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetCoreDameng'
        envArgs.platformVersion = '18.11.43'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - DB2 ISeries
        envArgs.dataSource = 'DB2ISeries'
        envArgs.dbmsModelConst = 'DB2400'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetCoreDB2ISeries'
        envArgs.platformVersion = '18.11.43'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - DB2 Common
        envArgs.dataSource = 'DB2UDB'
        envArgs.dbmsModelConst = 'DB2Common'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetCoreDB2UDB'
        envArgs.platformVersion = '18.11.43'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - Informix
        envArgs.dataSource = 'Informix'
        envArgs.dbmsModelConst = 'Informix'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetCoreInformix'
        envArgs.platformVersion = '18.11.43'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - Oracle 12
        envArgs.dataSource = 'Oracle'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '12c or higher'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetCoreOracle'
        envArgs.platformVersion = '18.11.47'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - Oracle 9 to 11
        envArgs.dataSource = 'Oracle9to11g'
        envArgs.dbmsModelConst = 'Oracle'
        envArgs.dbmsVersion = '9 to 11g'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetCoreOracle9to11g'
        envArgs.platformVersion = '18.11.47'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - Postgres
        envArgs.dataSource = 'PostgreSQL'
        envArgs.dbmsModelConst = 'POSTGRESQL'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetCorePostgreSQL'
        envArgs.platformVersion = '18.11.47'
        integrateNetPlatform(envArgs, clientDuArgs, engineDuArgs)
        // -------------------------- NetCore - SAP Hana
        envArgs.dataSource = 'SapHana'
        envArgs.dbmsModelConst = 'HANA'
        envArgs.platformId = 'GXDeps.GAM.Platform.NetCoreSapHana'
        envArgs.platformVersion = '18.11.47'
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
        if(envArgs.platformId) {
            stage("Download GAM Platform") {
                gxLibHelper.downloadNugetPackage(envArgs.gxBasePath, envArgs.platformId, envArgs.platformVersion, "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}\\index.json")
            }
        }
        // ------------------------ PREPARE ENVIRONMENT ------------------------ 
        kbLibHelper.setEnvironmentProperty(envArgs, "DataSource", envArgs.dbmsModelConst)
        kbLibHelper.setDataStoreProperty(envArgs, "Default", "DBMS", envArgs.dbmsModelConst)
        kbLibHelper.setDataStoreProperty(envArgs, "GAM", "DBMS", envArgs.dbmsModelConst)
        if(envArgs.dbmsModelConst == 'Oracle' && envArgs.dbmsVersion) {
            kbLibHelper.setDataStoreProperty(envArgs, "Default", "Oracle version", envArgs.dbmsVersion)
            kbLibHelper.setDataStoreProperty(envArgs, "GAM", "Oracle version", envArgs.dbmsVersion)
        }
        envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        clientDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        engineDuArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"
        kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
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
            // ----------------------------- Create NuGet package
            envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
            envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
            envArgs.packageVersion = envArgs.componentVersion
            envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

            // ----------------------------- Publish NuGet package
            envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
            gxLibDeployEngine.publishNuGetPackage(envArgs)
        }
        def ret = "${envArgs.componentId}.${envArgs.packageName}"
        return ret

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

// /**
//  * Return packageName for packageList in dispatch
//  */
// String updatePlatformNetFW(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
//     try{
//         def kbLibHelper = new PropertiesHelper()
//         def gxLibDeployEngine = new GXDeployEngineHelper()
//         // ----------------------------- Print Debug vars
//         echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
//         echo "INFO dataSource:: ${envArgs.dataSource}"
//         echo "INFO targetPath:: ${envArgs.targetPath}"

//         stage("Prepare ENV:${envArgs.targetPath}") {
//             kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
//             powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 # --------------------- Remove Model Dir
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
//                 \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -ItemType Directory

//                 # --------------------- TODO: Update reference to circle dependency
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${WORKSPACE}\\Libs\\Net\\*")
//                 Copy-Item -Path "${WORKSPACE}\\Libs\\Net\\*" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\" -Force -Recurse
                
//                 # --------------------- Sync files from GeneXus Installation
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\dotNet\\GXflow.Programs.Rules.dll")
//                 copy-item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\dotNet\\GXflow.Programs.Rules.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\GXflow.Programs.Rules.dll" -Force
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\dotNet\\wfcache.dll")
//                 copy-item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\dotNet\\wfcache.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\wfcache.dll" -Force
//                 Write-Output((Get-Date -Format G) + " [INFO] Extract NetProt.zip")
//                 Expand-Archive -Path "${envArgs.gxBasePath}\\Packages\\GXPM\\Protection\\NetProt.zip" -DestinationPath "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -Force                        
                
//                 # --------------------- Create VirtualDirCreationDisabled file
//                 Write-Output((Get-Date -Format G) + " [INFO] Create VirtualDirCreationDisabled file")
//                 if(-not (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled")) {\$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled"}
//             """
//         }
//         stage("Build ENV:${envArgs.targetPath}") {
//             kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
//             kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "HTML5")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "isolation_level", "Read Uncommitted")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", ".Net Application Namespace", "GXflow.Programs")
//             if(envArgs.dataSource == 'db2common' || envArgs.dataSource == 'Informix' || envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' ||  envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql2012' ) {
//                 kbLibHelper.setDataStoreProperty(envArgs, "Default", "database schema", "GXFLOW_SCHEMA")
//             }
            
//             buildConfigurationEnvironment(envArgs)

//             // //------------------ W.A for connection.gam
//             bat label: 'Create connection.gam',
//                 script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
//         }
//         stage("Make DB Schema Dynamic") {
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:KBEnvironment="${envArgs.environmentName}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:TargetDbms="${envArgs.dataSource}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /t:MakeDBSchemaDynamic
//             """
//         }
//         stage("Package ENV:${envArgs.targetPath} Platform") {
//             // ----------------------------- Package Platform resources
//             envArgs.deployTarget = "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration"
//             powershell script: """
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration" -Recurse -Force }
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline" -Recurse -Force }
//             """
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:KBEnvironment="${envArgs.environmentName}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:TargetDbms="${envArgs.dataSource}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
//                 /t:PackageWorkflow
//             """
//             // ----------------------------- Create Package for DU:Client
//             clientDuArgs.packageLocation = packageLocalDU(clientDuArgs)
//             echo "INFO DU Package Location:: ${clientDuArgs.packageLocation}"
//             clientDuArgs.packageName = "WF${clientDuArgs.duName}.zip"
//             echo "INFO Package Name:: ${clientDuArgs.packageName}"
//             deployDirPath = powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 Copy-Item -Path "${clientDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${clientDuArgs.targetPath}\\${clientDuArgs.packageName}"
//                 Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
//                 Split-Path "${clientDuArgs.packageLocation}" -Parent
//             """, returnStdout: true
//             echo "INFO Deploy Dir Path:: ${deployDirPath}"
//             dir("${deployDirPath.trim()}") {
//                 archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
//             }
//             // ----------------------------- Create Package for DU:Engine
//             engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
//             echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
//             engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
//             echo "INFO Package Name:: ${engineDuArgs.packageName}"
//             deployDirPath = powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
//                 Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
//                 Split-Path "${engineDuArgs.packageLocation}" -Parent
//             """, returnStdout: true
//             echo "INFO Deploy Dir Path:: ${deployDirPath}"
//             dir("${deployDirPath.trim()}") {
//                 archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
//             }
//             // ----------------------------- Generate files extra from WFClient n WFEngine local package
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
//                 /t:GenerateGXDeployDescriptor
//             """
//             // ----------------------------- Zip package
//             envArgs.packageName = "Platform.${envArgs.generatedLanguage}${envArgs.dataSource}.zip"
//             powershell script: """
//                 & 7z a -tzip "${envArgs.deployTarget}\\${envArgs.packageName}" "${envArgs.deployTarget}\\Packages"
//             """
//             // ----------------------------- Create NuGet package
//             envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
//             envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
//             envArgs.packageVersion = envArgs.componentVersion
//             envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

//             // ----------------------------- Publish NuGet package
//             envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
//             gxLibDeployEngine.publishNuGetPackage(envArgs)
//         }
        
//         def ret = "${envArgs.componentId}.${envArgs.packageName}"
//         return ret
//     } catch (error) {
// //         currentBuild.result = 'FAILURE'
// //         throw error
//     }
// }

// /**
//  *  
//  */
// String updatePlatformJava(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
//     try{
//         def kbLibHelper = new PropertiesHelper()
//         def gxLibDeployEngine = new GXDeployEngineHelper()
//         // ----------------------------- Print Debug vars
//         echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
//         echo "INFO dataSource:: ${envArgs.dataSource}"
//         echo "INFO targetPath:: ${envArgs.targetPath}"
        
//         stage("Prepare ENV:${envArgs.targetPath}") {
//             kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath) 
//             powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
//                 \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib" -ItemType Directory

//                 # --------------------- TODO: Update reference to circle dependency
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${WORKSPACE}\\Libs\\Java\\*")
//                 Copy-Item -Path "${WORKSPACE}\\Libs\\Java\\*" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib\\" -Force -Recurse
                
//                 # --------------------- Sync files from GeneXus Installation
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\Java\\com.gxflow.rules.jar")
//                 Copy-Item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\Java\\com.gxflow.rules.jar" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib\\" -Force -Recurse
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\Java\\wfcache.jar")
//                 Copy-Item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\Java\\wfcache.jar" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib\\" -Force -Recurse
//                 Write-Output((Get-Date -Format G) + " [INFO] Extract JavaProt.zip")
//                 Expand-Archive -Path "${envArgs.gxBasePath}\\Packages\\GXPM\\Protection\\JavaProt.zip" -DestinationPath "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\lib" -Force
                
//                 # --------------------- Create VirtualDirCreationDisabled file
//                 Write-Output((Get-Date -Format G) + " [INFO] Create VirtualDirCreationDisabled file in modelDir")
//                 if(-not (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled")) {\$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled"}
//             """
//         }
//         stage("Build ENV:${envArgs.targetPath}") {
//             kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
//             kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "HTML5")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "java package name", "com.gxflow")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "generate prompt programs", "No")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "compiler_options", "-J-Xms1024m -J-Xmx2048m -O -source 1.8 -target 1.8")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema -donotexecute")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "Java platform support", "Both Platforms")
//             if(envArgs.dataSource == 'db2common' || envArgs.dataSource == 'Informix' || envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' || envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql2012' ) {
//                 kbLibHelper.setDataStoreProperty(envArgs, "Default", "database schema", "GXFLOW_SCHEMA")
//             }
            
//             buildConfigurationEnvironment(envArgs)

//             // //------------------ W.A for connection.gam
//             bat label: 'Create connection.gam',
//                 script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
//         }
//         stage("Make DB Schema Dynamic") {
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:KBEnvironment="${envArgs.environmentName}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:TargetDbms="${envArgs.dataSource}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /t:MakeDBSchemaDynamic
//             """
//         }
//         stage("Package ENV:${envArgs.targetPath} Platform") {
//             // ----------------------------- Package Platform resources
//             envArgs.deployTarget = "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration"
//             powershell script: """
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration" -Recurse -Force }
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline" -Recurse -Force }
//             """
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:KBEnvironment="${envArgs.environmentName}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:TargetDbms="${envArgs.dataSource}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
//                 /t:PackageWorkflow
//             """
//             // ----------------------------- Create Package for DU:Client
//             clientDuArgs.packageLocation = packageLocalDU(clientDuArgs)
//             echo "INFO DU Package Location:: ${clientDuArgs.packageLocation}"
//             clientDuArgs.packageName = "WF${clientDuArgs.duName}.zip"
//             echo "INFO Package Name:: ${clientDuArgs.packageName}"
//             deployDirPath = powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 Copy-Item -Path "${clientDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${clientDuArgs.targetPath}\\${clientDuArgs.packageName}"
//                 Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
//                 Split-Path "${clientDuArgs.packageLocation}" -Parent
//             """, returnStdout: true
//             echo "INFO Deploy Dir Path:: ${deployDirPath}"
//             dir("${deployDirPath.trim()}") {
//                 archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
//             }
//             // ----------------------------- Create Package for DU:Engine
//             engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
//             echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
//             engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
//             echo "INFO Package Name:: ${engineDuArgs.packageName}"
//             deployDirPath = powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
//                 Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
//                 Split-Path "${engineDuArgs.packageLocation}" -Parent
//             """, returnStdout: true
//             echo "INFO Deploy Dir Path:: ${deployDirPath}"
//             dir("${deployDirPath.trim()}") {
//                 archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
//             }
//             // ----------------------------- Generate files extra from WFClient n WFEngine local package
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
//                 /t:GenerateGXDeployDescriptor
//             """
//             // ----------------------------- Zip package
//             envArgs.packageName = "Platform.${envArgs.generatedLanguage}${envArgs.dataSource}.zip"
//             powershell script: """
//                 & 7z a -tzip "${envArgs.deployTarget}\\${envArgs.packageName}" "${envArgs.deployTarget}\\Packages"
//             """
//             // ----------------------------- Create NuGet package
//             envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
//             envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
//             envArgs.packageVersion = envArgs.componentVersion
//             envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

//             // ----------------------------- Publish NuGet package
//             envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
//             gxLibDeployEngine.publishNuGetPackage(envArgs)
//         }
//         def ret = "${envArgs.componentId}.${envArgs.packageName}"
//         return ret

//     } catch (error) {
// //         currentBuild.result = 'FAILURE'
// //         throw error
//     }
// }

// /**
//  *
//  */
// String updatePlatformNet(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
//     try{
//         def kbLibHelper = new PropertiesHelper()
//         def gxLibDeployEngine = new GXDeployEngineHelper()
//         // ----------------------------- Print Debug vars
//         echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
//         echo "INFO dataSource:: ${envArgs.dataSource}"
//         echo "INFO targetPath:: ${envArgs.targetPath}"
        
//         stage("Prepare ENV:${envArgs.targetPath}") {
//             kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            
//             powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}" -Recurse -Force }
//                 \$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -ItemType Directory

//                 # --------------------- TODO: Update reference to circle dependency
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${WORKSPACE}\\Libs\\NetCore\\*")
//                 Copy-Item -Path "${WORKSPACE}\\Libs\\NetCore\\*.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\" -Force -Recurse
                
//                 # --------------------- Sync files from GeneXus Installation
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\dotNetCore\\GXflow.Programs.Rules.dll")
//                 Copy-Item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\RuleEvaluator\\dotNetCore\\GXflow.Programs.Rules.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\GXflow.Programs.Rules.dll" -Force -Recurse
//                 Write-Output((Get-Date -Format G) + " [INFO] Sync ${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\dotNetCore\\wfcache.dll")
//                 Copy-Item -Path "${envArgs.gxBasePath}\\Packages\\Gxpm\\WFCache\\dotNetCore\\wfcache.dll" -Destination "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin\\wfcache.dll" -Force -Recurse
//                 Write-Output((Get-Date -Format G) + " [INFO] Extract NetProtCore.zip")
//                 Expand-Archive -Path "${envArgs.gxBasePath}\\Packages\\GXPM\\Protection\\NetProtCore.zip" -DestinationPath "${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\bin" -Force                        
                
//                 # --------------------- Create VirtualDirCreationDisabled file
//                 Write-Output((Get-Date -Format G) + " [INFO] Create VirtualDirCreationDisabled file in modelDir")
//                 if(-not (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled")) {\$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled"}
//             """
//         }
//         stage("Build ENV:${envArgs.targetPath}") {
//             kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
//             kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "Do not specify")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema -donotexecute")
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", ".net_application_namespace", "GXflow.Programs")                    
//             kbLibHelper.setGeneratorProperty(envArgs, "Default", "isolation_level", "Read Uncommitted")
//             if(envArgs.dataSource == 'db2common' || envArgs.dataSource == 'Informix' || envArgs.dataSource == 'Oracle' || envArgs.dataSource == 'Oracle9to11g' || envArgs.dataSource == 'PostgreSQL' || envArgs.dataSource == 'Sql2012' ) {
//                 kbLibHelper.setDataStoreProperty(envArgs, "Default", "database schema", "GXFLOW_SCHEMA")
//             }
            
//             buildConfigurationEnvironment(envArgs)

//             // //------------------ W.A for connection.gam
//             bat label: 'Create connection.gam',
//                 script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
//         }
//         stage("Make DB Schema Dynamic") {
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:KBEnvironment="${envArgs.environmentName}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:TargetDbms="${envArgs.dataSource}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /t:MakeDBSchemaDynamic
//             """
//         }
//         stage("Package ENV:${envArgs.targetPath} Platform") {
//             // ----------------------------- Package Platform resources
//             envArgs.deployTarget = "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration"
//             powershell script: """
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\Integration" -Recurse -Force }
//                 if (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline") { Remove-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\IntegrationPipeline" -Recurse -Force }
//             """
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:KBEnvironment="${envArgs.environmentName}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:TargetDbms="${envArgs.dataSource}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
//                 /t:PackageWorkflow
//             """
//             // ----------------------------- Create Package for DU:Client
//             clientDuArgs.packageLocation = packageLocalDU(clientDuArgs)
//             echo "INFO DU Package Location:: ${clientDuArgs.packageLocation}"
//             clientDuArgs.packageName = "WF${clientDuArgs.duName}.zip"
//             echo "INFO Package Name:: ${clientDuArgs.packageName}"
//             deployDirPath = powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 Copy-Item -Path "${clientDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${clientDuArgs.targetPath}\\${clientDuArgs.packageName}"
//                 Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
//                 Split-Path "${clientDuArgs.packageLocation}" -Parent
//             """, returnStdout: true
//             echo "INFO Deploy Dir Path:: ${deployDirPath}"
//             dir("${deployDirPath.trim()}") {
//                 archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
//             }
//             // ----------------------------- Create Package for DU:Engine
//             engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
//             echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
//             engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
//             echo "INFO Package Name:: ${engineDuArgs.packageName}"
//             deployDirPath = powershell script: """
//                 \$ErrorActionPreference = 'Stop'
//                 Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
//                 Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}" -Force
//                 Split-Path "${engineDuArgs.packageLocation}" -Parent
//             """, returnStdout: true
//             echo "INFO Deploy Dir Path:: ${deployDirPath}"
//             dir("${deployDirPath.trim()}") {
//                 archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}_${env.BUILD_NUMBER}", followSymlinks: false
//             }
//             // ----------------------------- Generate files extra from WFClient n WFEngine local package
//             bat script: """
//                 "${envArgs.msbuildExePath}" "${envArgs.localKBPath}\\${envArgs.targetPath}\\Web\\${envArgs.msbuildDeployFile}" \
//                 /p:GXInstall="${envArgs.gxBasePath}" \
//                 /p:KBFolder="${envArgs.localKBPath}" \
//                 /p:TargetLanguage="${envArgs.generatedLanguage}" \
//                 /p:TargetDbms="${envArgs.dataSource}" \
//                 /p:KBEnvironmentPath="${envArgs.targetPath}" \
//                 /p:OutputPath="${envArgs.deployTarget}\\Packages\\GXPM" \
//                 /t:GenerateGXDeployDescriptor
//             """
//             // ----------------------------- Zip package
//             envArgs.packageName = "Platform.${envArgs.generatedLanguage}${envArgs.dataSource}.zip"
//             powershell script: """
//                 & 7z a -tzip "${envArgs.deployTarget}\\${envArgs.packageName}" "${envArgs.deployTarget}\\Packages"
//             """
//             // ----------------------------- Create NuGet package
//             envArgs.packageLocation = "${envArgs.deployTarget}\\${envArgs.packageName}"
//             envArgs.packageName = envArgs.packageName.replace(".zip", "").trim()
//             envArgs.packageVersion = envArgs.componentVersion
//             envArgs.nupkgPath = gxLibDeployEngine.createNuGetPackageFromZip(envArgs)

//             // ----------------------------- Publish NuGet package
//             envArgs.moduleServerSource = "${envArgs.moduleServerSourceBase}${envArgs.artifactsServerId}"
//             gxLibDeployEngine.publishNuGetPackage(envArgs)
//         }
//         def ret = "${envArgs.componentId}.${envArgs.packageName}"
//         return ret

//     } catch (error) {
// //         currentBuild.result = 'FAILURE'
// //         throw error
//     }
// }




return this
