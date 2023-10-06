/*
 * Job installGeneXusUsingURI >> Download and install GeneXus Installation from a public URI
 *
 * @Param args = [:]
 * +- forceUpdateGX
 * +- localGXPath
 * +- localAndroidSDKPath
 * +- genexusURI
 * +- protServerType, protServerName, protServerCredentialsId
 */

def call(Map args = [:]) {
    def fileContents
    if(Boolean.valueOf(args.forceUpdateGX)) {
        fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/deleteGeneXusInstallation.ps1'
        writeFile file: 'deleteGeneXusInstallation.ps1', text: fileContents
        powershell script: ".\\deleteGeneXusInstallation.ps1 -localAndroidSDKPath:'${args.localAndroidSDKPath}' -localGXPath:'${args.localGXPath}'"
    }
    fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/updateGeneXusInstallationByURI.ps1'
    writeFile file: 'updateGeneXusInstallationByURI.ps1', text: fileContents
    powershell script: ".\\updateGeneXusInstallationByURI.ps1 -genexusURI:'${args.genexusURI}' -localAndroidSDKPath:'${args.localAndroidSDKPath}' -localGXPath:'${args.localGXPath}'"

    withCredentials([
        usernamePassword(credentialsId: "${args.protServerCredentialsId}", passwordVariable: 'protectionServerPass', usernameVariable: 'protectionServerUser')
    ]) {
        fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/configProtectionServer.ps1'
        writeFile file: 'configProtectionServer.ps1', text: fileContents
        powershell script: ".\\configProtectionServer.ps1 -protectionServerType:'${args.protServerType}' -protectionServerName:'${args.protServerName}' -protectionServerUser:'${protectionServerUser}' -localGXPath:'${args.localGXPath}'"
    }
    
    fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/getGeneXusInstallationVersion.ps1'
    writeFile file: 'getGeneXusInstallationVersion.ps1', text: fileContents
    powershell script: ".\\getGeneXusInstallationVersion.ps1 -localGXPath:'${args.localGXPath}'"
}