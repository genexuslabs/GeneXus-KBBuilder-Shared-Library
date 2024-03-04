/*
 * Job runUnitTestSuite >> Read properties from environment
 *
 * @Param args = [:]
 * +- gxBasePath
 * +- localKBPath
 * +- environmentName
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:]) {
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    bat label: "Running Tests:${args.testObjects}", 
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /p:testObjectsList="${args.testObjects}" \
            /p:testBrowser="${args.testBrowser}" \
            /p:testArgs="${args.testArguments}" \
            /p:gxsUsername="${args.gxserverUSR}" \
            /p:gxsPassword="${args.gxserverPWD}" \
            /p:fullTestResultsFile="${args.localUnitTestPath}\\UnitTestResults.xml" \
            /t:TestObjTestSuite
        """
    dir(localUnitTestingPath.trim()) {
        junit "UnitTestResults.xml"
    }
}