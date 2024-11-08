package com.kbbuilder
import com.genexus.PropertiesHelper


void buildPlatform(Map args = [:]) {
    try{
        def kbLibHelper = new PropertiesHelper()
        // ----------------------------- Print Debug vars
        echo "INFO GeneratedLanguage:: ${args.generatedLanguage}"
        echo "INFO DataSource:: ${args.dataSource}"
        args.targetPath = "${args.generatedLanguage}${args.dataSource}"
        echo "INFO TargetPath:: ${args.targetPath}"
        kbLibHelper.setEnvironmentProperty(args, "TargetPath", args.targetPath)
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

void packagePlatform(Map args = [:]) {
    try{


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
