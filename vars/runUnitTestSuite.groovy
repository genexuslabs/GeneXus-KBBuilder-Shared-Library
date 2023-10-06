/*
 * Job runUnitTestSuite >> Read properties from environment
 *
 * @Param args = [:]
 * +- localGXPath
 * +- localKBPath
 * +- environmentName
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:]) {
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    Boolean foundReorganization = false
    String target = " /t:TestObjTestSuite"
    String msbuildGenArgs = ''
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "GX_PROGRAM_DIR", args.localGXPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "localKbPath", args.localKBPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "EnvironmentName", args.environmentName)
    withCredentials([ usernamePassword(credentialsId: args.gxserverCredentials, passwordVariable: 'gxserverPWD', usernameVariable: 'gxserverUSR')]) {
        msbuildGenArgs = concatArgs(msbuildGenArgs, "testObjectsList", args.testObjects)
        msbuildGenArgs = concatArgs(msbuildGenArgs, "testBrowser", test.browser)
        msbuildGenArgs = concatArgs(msbuildGenArgs, "testArgs", test.arguments)
        msbuildGenArgs = concatArgs(msbuildGenArgs, "gxsUsername", gxserverUSR)
        msbuildGenArgs = concatArgs(msbuildGenArgs, "gxsPassword", gxserverPWD)
    }
    String localUnitTestingPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\tests\\unit\")", returnStdout: true
    msbuildGenArgs = concatArgs(msbuildGenArgs, "fullTestResultsFile", "${localUnitTestingPath.trim()}\\UnitTestResults.xml")
    bat label: "Running Tests:${args.testObjects}", 
        script: "\"${args.msbuildExePath}\" .\\cdxci.msbuild ${target} ${msbuildGenArgs} /nologo "
    dir(localTestingPath) {
        junit "UnitTestResults.xml"
    }

    return foundReorganization
}