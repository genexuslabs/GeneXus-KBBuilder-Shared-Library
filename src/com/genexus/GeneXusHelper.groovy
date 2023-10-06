package com.genexus

/**
 * This methods
 * @param localGXPath
 */
String getCurrentVersion(String localGXPath) {
    try{        
        String dllPath = "${localGXPath}" + "\\Artech.Common.Controls.dll"
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
