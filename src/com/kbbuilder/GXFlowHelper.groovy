package com.kbbuilder
import com.genexus.PropertiesHelper

/**
 * Return packageName for packageList in dispatch
 */
String updatePlatformNetFW(Map envArgs = [:], Map clientDuArgs = [:], Map engineDuArgs = [:]) {
    try{
        def kbLibHelper = new PropertiesHelper()
        // ----------------------------- Print Debug vars
        echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
        echo "INFO dataSource:: ${envArgs.dataSource}"
        envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"

        stage("Prepare ENV:${envArgs.targetPath}") {
            steps {
                script {
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
            }
        }
        stage("Build ENV:${envArgs.targetPath}") {
            steps{
                script {
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
            }
        }
        stage("Package ENV:${envArgs.targetPath} Platform"){
            steps {
                script {

                    clientDuArgs.packageLocation = packageLocalDU(clientDuArgs)
                    echo "INFO DU Package Location:: ${clientDuArgs.packageLocation}"
                    deployDirPath = powershell script: """Split-Path "${clientDuArgs.packageLocation}" -Parent""", returnStdout: true
                    echo "INFO Deploy Dir Path:: ${deployDirPath}"
                    packageName = powershell script: """Split-Path "${clientDuArgs.packageLocation}" -Leaf""", returnStdout: true
                    echo "INFO Package Name:: ${packageName}"
                    // dir("${deployDirPath.trim()}") {
                    //     TODO: Rename file to NetFW_client/Engine_BUILDNUM.zip
                    //     archiveArtifacts artifacts: "${packageName.trim()}", followSymlinks: false
                    // }
                    engineDuArgs.packageLocation = packageLocalDU(engineDuArgs)
                    echo "INFO DU Package Location:: ${engineDuArgs.packageLocation}"
                    deployDirPath = powershell script: """Split-Path "${engineDuArgs.packageLocation}" -Parent""", returnStdout: true
                    echo "INFO Deploy Dir Path:: ${deployDirPath}"
                    packageName = powershell script: """Split-Path "${engineDuArgs.packageLocation}" -Leaf""", returnStdout: true
                    echo "INFO Package Name:: ${packageName}"
                    // dir("${deployDirPath.trim()}") {
                    //     TODO: Rename file to NetFW_client/Engine_BUILDNUM.zip
                    //     archiveArtifacts artifacts: "${packageName.trim()}", followSymlinks: false
                    // }
                    
                }
            }
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
        // ----------------------------- Print Debug vars
        echo "INFO generatedLanguage:: ${envArgs.generatedLanguage}"
        echo "INFO dataSource:: ${envArgs.dataSource}"
        envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"

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
        envArgs.targetPath = "${envArgs.generatedLanguage}${envArgs.dataSource}"

    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}


return this
