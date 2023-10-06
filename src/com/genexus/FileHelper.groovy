package com.genexus

/**
 * This method return path combine function
 * @param 
 */
String joinPath(String a, String b) {
    try {
        def joinResult = powershell script: """
            Join-Path \"${a}\" \"${b}\"
            #if(!(Test-Path -Path \$ret)) { Write-Error -Message \"joinPath fail when Test-Path\"}
            #Write-Host \$ret
        """, returnStdout: true
        return "${joinResult.trim()}"
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method return absolute path from a relativa path
 * @param relativePath is relative path
 */
String getAbsolutePathFromWS(String relativePath) {
    try {
        String auxPath = env.WORKSPACE + relativePath
        def absolutePath = powershell script: """
            if(!(Test-Path -Path \"${auxPath}\")) {\$null = New-Item -Path \"${auxPath}\" -ItemType Directory}
            Set-Location -Path \"${auxPath}\"
            (Get-Location).Path
        """, returnStdout: true
        return absolutePath.trim()
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this