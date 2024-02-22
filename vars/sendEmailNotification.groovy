/*
 * Job readPipelineProperties >> Read properties from build.properties
 *
 * @Param args = [:]
 * +- gitUrl
 * +- gitBranch
 * +- gitCredentialsId
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:]) {
    git url: args.gitUrl, 
        branch: args.gitBranch, 
        credentialsId: args.gitCredentialsId, 
        changelog: false, 
        poll: false

    props = readProperties file: args.propertiesFilePath;
    props = readProperties defaults: props, file: args.machineFilePath;
    String msbuildExePath = powershell script: "Join-Path \"${props.msbuildInstallationPath}\" \"msbuild.exe\"", returnStdout: true
    props.msbuildExePath = msbuildExePath.trim()
    echo "INFO MSBuild:: ${props.msbuildExePath}"

    String localGXPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\genexus\")", returnStdout: true
    props.localGXPath = localGXPath.trim()
    echo "INFO GeneXus Installation:: ${props.localGXPath}"
    if(props.genexusNeedAndroidSDK) {
        String localAndroidSDKPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\androidSDK\")", returnStdout: true
        props.localAndroidSDKPath = localAndroidSDKPath.trim()
        echo "INFO AndroidSDK:: ${props.localAndroidSDKPath}"
    }
    String localKBPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\kb\\${props.gxserverKB}\")", returnStdout: true
    props.localKBPath = localKBPath.trim()
    echo "INFO KnowledgeBase:: ${props.localKBPath}"

    String localUnitTestingPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\tests\\unit\")", returnStdout: true
    props.localUnitTestPath = localUnitTestingPath.trim()
    echo "INFO localUnitTestPath:: ${props.localUnitTestPath}"

    return props
}