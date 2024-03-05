package com.genexus

/**
 * This method set generator property using msbuild command
 * @param 
 */
void setGeneratorProperty(String msbuildExePath, String gxBasePath, String localKBPath, String environmentName, String generatorName, String propName, String propValue) {
    try {
        // Sync properties.msbuild -- TODO no sync if exists
        fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
        writeFile file: 'properties.msbuild', text: fileContents

        bat script: """
                "${msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${gxBasePath}" \
                /p:localKbPath="${localKBPath}" \
                /p:environmentName="${environmentName}" \
                /p:generatorName="${generatorName}" \
                /p:propertyName="${propName}" \
                /p:propertyValue="${propValue}" \
                /t:SetGeneratorProperty
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this