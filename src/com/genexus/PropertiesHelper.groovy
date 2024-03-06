package com.genexus

/**
 * This method retrieves an environment property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath, localKBPath,
 *             and environmentName, to customize the MSBuild execution.
 * @param envPropName The name of the environment property to retrieve.
 * @return The value of the specified environment property.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to obtain the requested environment property value. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
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
 * This method sets an environment property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath, localKBPath,
 *             and environmentName, to customize the MSBuild execution.
 * @param envPropName The name of the environment property to set.
 * @param envPropValue The value to assign to the specified environment property.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to set the value of the specified environment property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
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
                /p:environmentPropName="${envPropName}" \
                /p:environmentPropValue="${envPropValue}" \
                /t:SetEnvironmentProperty
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method retrieves a generator property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath, localKBPath,
 *             and environmentName, to customize the MSBuild execution.
 * @param generatorName The name of the generator for which to retrieve the property.
 * @param genPropName The name of the generator property to retrieve.
 * @return The value of the specified generator property.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to obtain the requested generator property value. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
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
 * This method sets a generator property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath, localKBPath,
 *             and environmentName, to customize the MSBuild execution.
 * @param genName The name of the generator for which to set the property.
 * @param genPropName The name of the generator property to set.
 * @param genPropValue The value to assign to the specified generator property.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to set the value of the specified generator property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
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