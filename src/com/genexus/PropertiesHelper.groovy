package com.genexus

/**
 * This method set generator property using msbuild command
 * @param 
 */
void setGeneratorProperty(Map args = [:]) { // String msbuildExePath, String gxBasePath, String localKBPath, String environmentName, String generatorName, String propName, String propValue) {
    try {
        // Sync properties.msbuild -- TODO no sync if exists
        fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
        writeFile file: 'properties.msbuild', text: fileContents

        bat script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:environmentName="${args.environmentName}" \
                /p:generatorName="${args.generatorName}" \
                /p:propertyName="${args.propName}" \
                /p:propertyValue="${args.propValue}" \
                /t:SetGeneratorProperty
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this