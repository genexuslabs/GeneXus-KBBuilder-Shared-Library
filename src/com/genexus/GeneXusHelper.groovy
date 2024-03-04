package com.genexus

/**
 * This methods remove GeneXus Installation, ProgramData/UserData path and Android SDK Path
 * @param gxBasePath
 */
void deleteGeneXusInstallation(String gxBasePath, String localAndroidSDKPath) {
    try{
        fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/deleteGeneXusInstallation.ps1'
        writeFile file: 'deleteGeneXusInstallation.ps1', text: fileContents
        powershell script: ".\\deleteGeneXusInstallation.ps1 -gxBasePath:'${gxBasePath}' -localAndroidSDKPath:'${localAndroidSDKPath}'"
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This methods update GeneXus Installation from public URI
 * @param gxBasePath
 */
void updateGeneXusInstallationByURI(String gxBasePath, String genexusURI, String localAndroidSDKPath) {
    try{
        fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/updateGeneXusInstallationByURI.ps1'
        writeFile file: 'updateGeneXusInstallationByURI.ps1', text: fileContents
        powershell script: ".\\updateGeneXusInstallationByURI.ps1 -gxBasePath:'${gxBasePath}' -genexusURI:'${genexusURI}' -localAndroidSDKPath:'${localAndroidSDKPath}'"
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This methods configure Protection Server in GeneXus Installation
 * @param gxBasePath
 */
void configureProtectionServer(String gxBasePath, String protServerType, String protServerName, String protServerCredentialsId) {
    try{
        withCredentials([
            usernamePassword(credentialsId: "${protServerCredentialsId}", passwordVariable: 'protectionServerPass', usernameVariable: 'protectionServerUser')
        ]) {
            fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/configureProtectionServer.ps1'
            writeFile file: 'configureProtectionServer.ps1', text: fileContents
            powershell script: ".\\configureProtectionServer.ps1 -gxBasePath:'${gxBasePath}' -protectionServerType:'${protServerType}' -protectionServerName:'${protServerName}' -protectionServerUser:'${protectionServerUser}'"
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This methods return GeneXus version
 * @param gxBasePath
 */
String getGeneXusInstallationVersion(String gxBasePath) {
    try{
        String dllPath = "${gxBasePath}" + "\\Artech.Common.Controls.dll"
        String gxversion = powershell label: "Using GeneXus version",
            script: """
                If( Test-Path -Path \"${dllPath}\"){
                    (Get-Item \"${dllPath}\").VersionInfo.ProductVersion
                }
                else
                {
                    Write-Output "--------"
                }
                
            """, returnStdout: true        
        return gxversion
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
