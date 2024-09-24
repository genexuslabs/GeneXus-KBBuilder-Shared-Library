package com.genexus

/**
 * This method removes the GeneXus installation, ProgramData/UserData paths, and Android SDK path.
 *
 * @param gxBasePath The base path of the GeneXus installation to be removed.
 * @param localAndroidSDKPath The local path of the Android SDK to be removed.
 *
 * This method utilizes a PowerShell script to perform the removal of the specified GeneXus installation,
 * including associated ProgramData/UserData paths, and the provided Android SDK path.
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
 * Updates the GeneXus installation from a public URI.
 *
 * @param gxBasePath The base path of the existing GeneXus installation to be updated.
 * @param genexusURI The public URI from which to update the GeneXus installation.
 * @param localAndroidSDKPath The local path of the Android SDK associated with the GeneXus installation.
 * @param runGXInstall Boolean flag indicating whether to execute the GeneXus installation command after the update.
 *
 * This method ensures the existence of a supporting PowerShell script for uninstallation,
 * downloads an update script, and executes it to update the specified GeneXus installation
 * using the provided public URI. It also considers the associated Android SDK path during the update.
 * 
 * Note: The boolean parameter 'runGXInstall' is passed as a string with a double dollar sign ($$) 
 * to translate it correctly into the PowerShell variable context.
 */
void updateGeneXusInstallationByURI(String gxBasePath, String genexusURI, String localAndroidSDKPath, Boolean runGXInstall) {
    try{
        if (!fileExists("${WORKSPACE}\\deleteGeneXusInstallation.ps1")) {
            fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/deleteGeneXusInstallation.ps1'
            writeFile file: 'deleteGeneXusInstallation.ps1', text: fileContents
        }
        fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/updateGeneXusInstallationByURI.ps1'
        writeFile file: 'updateGeneXusInstallationByURI.ps1', text: fileContents
        powershell script: ".\\updateGeneXusInstallationByURI.ps1 -gxBasePath:'${gxBasePath}' -genexusURI:'${genexusURI}' -localAndroidSDKPath:'${localAndroidSDKPath}' -runGXInstall:\$${runGXInstall}"
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method configures the Protection Server in a GeneXus Installation.
 *
 * @param gxBasePath The base path of the GeneXus installation where the Protection Server will be configured.
 * @param protServerType The type of Protection Server to be configured (e.g., "Basic" or "Advanced").
 * @param protServerName The name or address of the Protection Server.
 * @param protServerCredentialsId The credentials ID containing the username and password for the Protection Server.
 *
 * This method utilizes secure credentials to access the Protection Server and executes a PowerShell script
 * to configure the specified GeneXus Installation with the provided Protection Server details.
 */
void configureProtectionServer(String gxBasePath, String protServerType, String protServerName, String protServerCredentialsId) {
    try{
        fileContents = libraryResource 'com/genexus/pwshScripts/gxInstallation/configureProtectionServer.ps1'
        writeFile file: 'configureProtectionServer.ps1', text: fileContents
        echo "[DEBUG] protServerCredentialsId::${protServerCredentialsId}"
        echo "[DEBUG] protServerName::${protServerName}"
        if(protServerCredentialsId != null) {
            withCredentials([
                usernamePassword(credentialsId: "${protServerCredentialsId}", passwordVariable: 'protectionServerPass', usernameVariable: 'protectionServerUser')
            ]) {
                powershell script: ".\\configureProtectionServer.ps1 -gxBasePath:'${gxBasePath}' -protectionServerType:'${protServerType}' -protectionServerName:'${protServerName}' -protectionServerUser:'${protectionServerUser}'"
            }
        }
        else {
            powershell script: ".\\configureProtectionServer.ps1 -gxBasePath:'${gxBasePath}' -protectionServerType:'${protServerType}' -protectionServerName:'${protServerName}'"
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method retrieves the version of the GeneXus installation.
 *
 * @param gxBasePath The base path of the GeneXus installation for which to obtain the version.
 * @return A string representing the version of the GeneXus installation, or "--------" if the version cannot be determined.
 *
 * This method checks for the presence of the 'Artech.Common.Controls.dll' file in the specified GeneXus installation
 * and retrieves the product version information. If the file is not found, it returns "--------" as a placeholder.
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
