/*
 * Job: runUnitTestSuite
 *
 * Description:
 * This job reads properties from the environment and runs a unit test suite using the 'TestObjTestSuite' 
 * MSBuild task. It syncs the `cdxci.msbuild` template, executes the tests, and stores the results in 
 * the specified path.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment.
 *   - testObjects: The list of test objects to include in the test suite.
 *   - testBrowser: The browser to use for testing.
 *   - testArguments: Additional arguments for the test.
 *   - gxserverUSR: The username for GeneXus Server authentication.
 *   - gxserverPWD: The password for GeneXus Server authentication.
 *   - localUnitTestPath: The local path where unit test results are stored.
 *   - msbuildExePath: The path to the MSBuild executable.
 *
 * Workflow Steps:
 * 1. Sync the `cdxci.msbuild` template from the library resources.
 * 2. Run the unit tests using MSBuild with the specified parameters.
 * 3. Store the unit test results in the specified path.
 * 4. Publish the unit test results using JUnit.
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    // -- TODO -- Tener credentialsId para gxserver
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