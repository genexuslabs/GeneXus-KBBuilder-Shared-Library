/*
 * Job buildConfigurationEnvironment >> This method executes the 'BuildAll' task after configuring the following properties:
 * -- >> "Keep GAM database updated" = false
 * -- >> "Deploy business processes on build" = No
 * -- >> "Populate Data" = false
 * -- >> "Reorganize server tables" = No
 * -- >> "deploy to cloud" = No
 *
 * @Param args = [:]
 * +- gxBasePath
 * +- localKBPath
 * +- environmentName
 * +- forceRebuild
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents
    // Sync properties.msbuild
    fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
    writeFile file: 'properties.msbuild', text: fileContents

    bat label: 'Avoid Datastore connections', 
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
            /p:GX_PROGRAM_DIR="${args.localGXPath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /t:AvoidDatastoreConnections
        """

    bat label: 'Build all', 
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.localGXPath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /p:rebuild="${args.forceRebuild}" \
            /t:BuildConfigurationEnv
        """
}