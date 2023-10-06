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
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "testObjectsList", args.testObjects)
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "testBrowser", args.testBrowser)
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "testArgs", args.testArguments)
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "gxsUsername", gxserverUSR)
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "gxsPassword", gxserverPWD)
    }
    String localUnitTestingPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\tests\\unit\")", returnStdout: true
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "fullTestResultsFile", "${localUnitTestingPath.trim()}\\UnitTestResults.xml")
    bat label: "Running Tests:${args.testObjects}", 
        script: "\"${args.msbuildExePath}\" .\\cdxci.msbuild ${target} ${msbuildGenArgs} /nologo "
    dir(localUnitTestingPath) {
        junit "UnitTestResults.xml"
    }

    return foundReorganization
}