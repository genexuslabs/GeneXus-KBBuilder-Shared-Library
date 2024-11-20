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
 * Resets an environment property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath, localKBPath,
 *             and environmentName, to customize the MSBuild execution.
 * @param envPropName The name of the environment property to set.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to reset the value of the specified environment property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 */
void resetEnvironmentProperty(Map args = [:], String envPropName) {
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
                /t:ResetEnvironmentProperty
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
String getGeneratorProperty(Map args = [:], String genName, String genPropName) {
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

/**
 * Resets a generator property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath, localKBPath,
 *             and environmentName, to customize the MSBuild execution.
 * @param genName The name of the generator for which to set the property.
 * @param genPropName The name of the generator property to set.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to reset the value of the specified generator property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 */
void resetGeneratorProperty(Map args = [:], String genName, String genPropName) {
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
                /t:ResetGeneratorProperty
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Retrieves an object property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath, 
 *             and localKBPath, to customize the MSBuild execution.
 * @param objName The name of the object (or category) for which to retrieve the property.
 * @param objPropName The name of the object property to retrieve.
 * @return The value of the specified object property.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to obtain the requested object property value. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
String getObjectProperty(Map args = [:], String objName, String objPropName) {
    try {
        
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        def propsFile = "${WORKSPACE}\\ObjProperty.json"
        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:objectName="${objName}" \
            /p:objectPropName="${objPropName}" \
            /p:propFileAbsolutePath="${propsFile}" \
            /p:helperName="aux" \
            /t:GetObjectProperty
        """
        def commiteableGenPropValue = readJSON file: propsFile
        echo "[READ] Object property `${objPropName}` = ${commiteableGenPropValue.aux}"
        return commiteableGenPropValue.aux
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Sets an object property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath,
 *             and localKBPath, to customize the MSBuild execution.
 * @param objName The name of the object (or category) for which to set the property.
 * @param objPropName The name of the object property to set.
 * @param objPropValue The value to assign to the specified object property.
 *
 * This method generates a properties file using a provided MSBuild template. It then
 * executes MSBuild to set the value of the specified object property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
void setObjectProperty(Map args = [:], String objName, String objPropName, String objPropValue) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:objectName="${objName}" \
                /p:objectPropName="${objPropName}" \
                /p:objectPropValue="${objPropValue}" \
                /t:SetObjectProperty
            """
    } catch (error) { 
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Resets an object property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath,
 *             and localKBPath, to customize the MSBuild execution.
 * @param objName The name of the object (or category) for which to set the property.
 * @param objPropName The name of the object property to set.
 *
 * This method generates a properties file using a provided MSBuild template. It then
 * executes MSBuild to reset the value of the specified object property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
void resetObjectProperty(Map args = [:], String objName, String objPropName) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:objectName="${objName}" \
                /p:objectPropName="${objPropName}" \
                /t:ResetObjectProperty
            """
    } catch (error) { 
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Retrieves a version property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath, 
 *             and localKBPath, to customize the MSBuild execution.
 * @param verPropName The name of the version property to retrieve.
 * @return The value of the specified version property.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to obtain the requested object property value. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
String getVersionProperty(Map args = [:], String verPropName) {
    try {
        
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        def propsFile = "${WORKSPACE}\\VerProperty.json"
        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:verPropName="${verPropName}" \
            /p:propFileAbsolutePath="${propsFile}" \
            /p:helperName="aux" \
            /t:GetVersionProperty
        """
        def commiteableGenPropValue = readJSON file: propsFile
        echo "[READ] Version property `${verPropName}` = ${commiteableGenPropValue.aux}"
        return commiteableGenPropValue.aux
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Sets a version property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath,
 *             and localKBPath, to customize the MSBuild execution.
 * @param verPropName The name of the version property to set.
 * @param verPropValue The value to assign to the specified version property.
 *
 * This method generates a properties file using a provided MSBuild template. It then
 * executes MSBuild to set the value of the specified version property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
void setVersionProperty(Map args = [:], String verPropName, String verPropValue) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:verPropName="${verPropName}" \
                /p:versionPropValue="${verPropValue}" \
                /t:SetVersionProperty
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Resets a version property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath,
 *             and localKBPath, to customize the MSBuild execution.
 * @param verPropName The name of the version property to set.
 *
 * This method generates a properties file using a provided MSBuild template. It then
 * executes MSBuild to reset the value of the specified version property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
void resetVersionProperty(Map args = [:], String verPropName) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:verPropName="${verPropName}" \
                /t:ResetVersionProperty
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}
/**
 * Retrieves a datastore property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath,
 *             and localKBPath, to customize the MSBuild execution.
 * @param propertyName The name of the datastore property to retrieve.
 * @param dataStoreName The name of the datastore.
 * @return The value of the specified datastore property.
 *
 * This method generates a properties file using a provided MSBuild template and then
 * executes MSBuild to obtain the requested datastore property value. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
String getDataStoreProperty(Map args = [:], String dataStoreName, String propertyName) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        def propsFile = "${WORKSPACE}\\DStoreProperty.json"
        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:DataStoreName="${dataStoreName}" \
            /p:PropertyName="${propertyName}" \
            /p:propFileAbsolutePath="${propsFile}" \
            /p:helperName="aux" \
            /t:GetDataStoreProperty
        """
        def commiteableGenPropValue = readJSON file: propsFile
        echo "[READ] DataStore property `${propertyName}` = ${commiteableGenPropValue.aux}"
        return commiteableGenPropValue.aux
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Sets a datastore property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath,
 *             and localKBPath, to customize the MSBuild execution.
 * @param propertyName The name of the datastore property to set.
 * @param propertyValue The value to assign to the specified datastore property.
 * @param dataStoreName The name of the datastore.
 *
 * This method generates a properties file using a provided MSBuild template. It then
 * executes MSBuild to set the value of the specified datastore property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
void setDataStoreProperty(Map args = [:], String dataStoreName, String propertyName, String propertyValue) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:DataStoreName="${dataStoreName}" \
            /p:PropertyName="${propertyName}" \
            /p:PropertyValue="${propertyValue}" \
            /t:SetDataStoreProperty
        """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Resets a datastore property using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath,
 *             and localKBPath, to customize the MSBuild execution.
 * @param propertyName The name of the datastore property to reset.
 * @param dataStoreName The name of the datastore.
 *
 * This method generates a properties file using a provided MSBuild template. It then
 * executes MSBuild to reset the value of the specified datastore property. The properties file
 * is used to store temporary values during the execution of the MSBuild command.
 *
 */
void resetDataStoreProperty(Map args = [:], String dataStoreName, String propertyName) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:DataStoreName="${dataStoreName}" \
            /p:PropertyName="${propertyName}" \
            /t:ResetDataStoreProperty
        """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Sets GAM properties using the MSBuild command.
 *
 * @param args A map containing optional parameters, such as msbuildExePath, gxBasePath,
 *             and localKBPath, to customize the MSBuild execution.
 * @param includeFrontEnd A flag indicating whether to include frontend objects.
 * @param includeSDSamples A flag indicating whether to include SDSamples.
 * @param updateMode The update mode to be used in the GAM properties.
 *
 * This method generates a properties file using a provided MSBuild template. It then
 * executes MSBuild to set the GAM properties as specified. The properties file is
 * used to store temporary values during the execution of the MSBuild command.
 *
 */
void setGAMProperties(Map args = [:], boolean includeFrontEnd, boolean includeSDSamples, String updateMode) {
    try {
        if (!fileExists("${WORKSPACE}\\properties.msbuild")) {
            def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
            writeFile file: 'properties.msbuild', text: fileContents
        }
        bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:includeFrontEnd="${includeFrontEnd}" \
                /p:includeSDSamples="${includeSDSamples}" \
                /p:updateMode="${updateMode}" \
                /t:SetGAMProperties
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
