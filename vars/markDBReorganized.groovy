/**
 * This method marks the database as reorganized using the provided arguments.
 * 
 * @param args a map containing the following keys:
 *   - customMSBuildScript: (String) Path to the custom MSBuild script.
 *   - localGXPath: (String) Path to the local GX directory.
 *   - localKBPath: (String) Path to the local knowledge base.
 *   - environmentName: (String) Name of the environment.
 *   - generator: (String) The generator to be used (e.g., "Java").
 *   - dataSource: (String) The data source to be used.
 *   - jdkInstallationId: (String) The ID of the JDK installation (required if generator is "Java").
 *   - tomcatVersion: (String, optional) The version of Tomcat to be used, if applicable.
 *   - msbuildExePath: (String) Path to the MSBuild executable.
 */
def call(Map args = [:]) {
    try {
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents

        bat label: "MarkDBReorganized",
            script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:EnvironmentName="${args.environmentName}" \
                /t:UpdateInstallationModel
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo " ERROR ${error.getMessage()}"
        throw error
    }
}