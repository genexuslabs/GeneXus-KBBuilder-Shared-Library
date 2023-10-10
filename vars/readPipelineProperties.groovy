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
    echo "INFO MSBuild:: ${msbuildExePath.trim()}"

    String localGXPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\genexus\")", returnStdout: true
    props.localGXPath = localGXPath.trim()
    echo "INFO GeneXus Installation:: ${localGXPath.trim()}"
    if(props.genexusNeedAndroidSDK) {
        String localAndroidSDKPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\androidSDK\")", returnStdout: true
        props.localAndroidSDKPath = localAndroidSDKPath.trim()
        echo "INFO AndroidSDK:: ${localAndroidSDKPath.trim()}"
    }
    String localKBPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\kb\\${props.gxserverKB}\")", returnStdout: true
    props.localKBPath = localKBPath.trim()
    echo "INFO KnowledgeBase:: ${localKBPath.trim()}"

    String localUnitTestingPath = powershell script: "[System.IO.Path]::GetFullPath(\"${WORKSPACE}\\..\\tests\\unit\")", returnStdout: true
    props.localUnitTestPath = localUnitTestingPath.trim()
    echo "INFO localUnitTestPath:: ${localUnitTestPath.trim()}"

    return props
}