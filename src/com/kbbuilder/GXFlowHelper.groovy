package com.kbbuilder
import com.genexus.PropertiesHelper
import com.genexus.GXDeployEngineHelper

/**
 * Return packageName for packageList in dispatch
 */
String updatePlatformNetFW(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try{
        def kbLibHelper = new PropertiesHelper()
        // ----------------------------- Print Debug vars
        echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
        echo "INFO dataSource:: ${envArgs.dataSource}"
        echo "INFO targetPath:: ${envArgs.targetPath}"

        stage("Prepare ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
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
        stage("Build ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
            kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "HTML5")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "isolation_level", "Read Uncommitted")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", ".Net Application Namespace", "QueryViewer.Services")                    
            
            buildConfigurationEnvironment(envArgs)

            // //------------------ W.A for connection.gam
            bat label: 'Create connection.gam',
                script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
        }
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
                Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}" -Force
                Split-Path "${clientDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}", followSymlinks: false
            }
            // ----------------------------- Create Package for DU:Engine
            engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
            echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
            engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
            echo "INFO Package Name:: ${engineDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
                Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}" -Force
                Split-Path "${engineDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}", followSymlinks: false
            }
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
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 *
 */
void updatePlatformJava(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try{
        def kbLibHelper = new PropertiesHelper()
        def gxLibDeployEngine = new GXDeployEngineHelper()
        // ----------------------------- Print Debug vars
        echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
        echo "INFO dataSource:: ${envArgs.dataSource}"
        echo "INFO targetPath:: ${envArgs.targetPath}"
        
        stage("Prepare ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath) 
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
        stage("Build ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
            kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "HTML5")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "java package name", "com.gxflow")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "generate prompt programs", "No")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "compiler_options", "-J-Xms1024m -J-Xmx2048m -O -source 1.8 -target 1.8")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "java package name", "com.gxflow")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema -donotexecute")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "Java platform support", "Both Platforms")
            
            buildConfigurationEnvironment(envArgs)

            // //------------------ W.A for connection.gam
            bat label: 'Create connection.gam',
                script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
        }
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
                Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}" -Force
                Split-Path "${clientDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}", followSymlinks: false
            }
            // ----------------------------- Create Package for DU:Engine
            engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
            echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
            engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
            echo "INFO Package Name:: ${engineDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
                Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}" -Force
                Split-Path "${engineDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}", followSymlinks: false
            }
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

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 *
 */
void updatePlatformNet(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try{
        def kbLibHelper = new PropertiesHelper()
        // ----------------------------- Print Debug vars
        echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
        echo "INFO dataSource:: ${envArgs.dataSource}"
        echo "INFO targetPath:: ${envArgs.targetPath}"
        
        stage("Prepare ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "TargetPath", envArgs.targetPath)
            
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
                Expand-Archive -Path "${envJavaDefinition.gxBasePath}\\Packages\\GXPM\\Protection\\NetProtCore.zip" -DestinationPath "${envJavaDefinition.localKBPath}\\${envJavaDefinition.targetPath}\\web\\bin" -Force                        
                
                # --------------------- Create VirtualDirCreationDisabled file
                Write-Output((Get-Date -Format G) + " [INFO] Create VirtualDirCreationDisabled file in modelDir")
                if(-not (Test-Path -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled")) {\$null = New-Item -Path "${envArgs.localKBPath}\\${envArgs.targetPath}\\VirtualDirCreationDisabled"}
            """
        }
        stage("Build ENV:${envArgs.targetPath}") {
            kbLibHelper.setEnvironmentProperty(envArgs, "translation_type", "Run-time")
            kbLibHelper.setEnvironmentProperty(envArgs, "html_document_type", "Do not specify")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "reorganization_options", "-nogui -noverifydatabaseschema -donotexecute")
            kbLibHelper.setGeneratorProperty(envArgs, "Default", ".net_application_namespace", "GXflow.Programs")                    
            kbLibHelper.setGeneratorProperty(envArgs, "Default", "isolation_level", "Read Uncommitted")
            
            buildConfigurationEnvironment(envArgs)

            // //------------------ W.A for connection.gam
            bat label: 'Create connection.gam',
                script: "echo > \"${envArgs.localKBPath}\\${envArgs.targetPath}\\web\\connection.gam\""
        }
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
                Rename-Item -Path "${clientDuArgs.packageLocation}" -NewName "${clientDuArgs.targetPath}_${clientDuArgs.packageName}" -Force
                Split-Path "${clientDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${clientDuArgs.targetPath}_${clientDuArgs.packageName}", followSymlinks: false
            }
            // ----------------------------- Create Package for DU:Engine
            engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
            echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
            engineDuArgs.packageName = "WF${engineDuArgs.duName}.zip"
            echo "INFO Package Name:: ${engineDuArgs.packageName}"
            deployDirPath = powershell script: """
                \$ErrorActionPreference = 'Stop'
                Copy-Item -Path "${engineDuArgs.packageLocation}" "${envArgs.deployTarget}\\Packages\\GXPM\\Platforms\\${engineDuArgs.targetPath}\\${engineDuArgs.packageName}"
                Rename-Item -Path "${engineDuArgs.packageLocation}" -NewName "${engineDuArgs.targetPath}_${engineDuArgs.packageName}" -Force
                Split-Path "${engineDuArgs.packageLocation}" -Parent
            """, returnStdout: true
            echo "INFO Deploy Dir Path:: ${deployDirPath}"
            dir("${deployDirPath.trim()}") {
                archiveArtifacts artifacts: "${engineDuArgs.targetPath}_${engineDuArgs.packageName}", followSymlinks: false
            }
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

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}


return this
