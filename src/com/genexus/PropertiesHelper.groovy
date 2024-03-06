package com.genexus

/**
 * This method get envronment property using msbuild command
 * @param 
 */
String getEnvironmentProperty(Map args = [:], String envPropName) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        def propsFile = "${WORKSPACE}\\CommProperty.json"
        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /p:environmentPropName="${envPropName}" \
            /p:propFileAbsolutePath="${propsFile}" \
            /p:helperName="aux" \
            /t:GetEnvironmentProperty
        """
        def commiteableEnvPropValue = readJSON file: propsFile
        echo "[READ] Environment property `${envPropName}` = ${commiteableEnvPropValue.aux}"
        return commiteableEnvPropValue.aux
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method set environment property using msbuild command
 * @param 
 */
void setEnvironmentProperty(Map args = [:], String envPropName, String envPropValue) {
    try {
        
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:environmentName="${args.environmentName}" \
                /p:environmentPropName="${environmentPropName}" \
                /p:environmentPropValue="${environmentPropValue}" \
                /t:SetEnvironmentProperty
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method get generator property using msbuild command
 * @param 
 */
String getGeneratorProperty(Map args = [:], String generatorName, String genPropName) {
    try {
        
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        def propsFile = "${WORKSPACE}\\CommProperty.json"
        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /p:generatorName="${genName}" \
            /p:generatorPropName="${genPropName}" \
            /p:propFileAbsolutePath="${propsFile}" \
            /p:helperName="aux" \
            /t:GetGeneratorProperty
        """
        def commiteableGenPropValue = readJSON file: propsFile
        echo "[READ] Generator property `${genPropName}` = ${commiteableGenPropValue.aux}"
        return commiteableGenPropValue.aux
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method set generator property using msbuild command
 * @param 
 */
void setGeneratorProperty(Map args = [:], String genName, String genPropName, String genPropValue) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:environmentName="${args.environmentName}" \
                /p:generatorName="${genName}" \
                /p:generatorPropName="${genPropName}" \
                /p:generatorPropValue="${genPropValue}" \
                /t:SetGeneratorProperty
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this