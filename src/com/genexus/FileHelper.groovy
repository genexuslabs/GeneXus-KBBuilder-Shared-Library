package com.genexus

/**
 * This method combines two path components to create a joined path.
 *
 * @param a The first path component.
 * @param b The second path component.
 * @return The combined path resulting from joining the two input path components.
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
 * This method returns the absolute path from a given relative or absolute path.
 *
 * @param auxPath The path, either relative or absolute, for which to obtain the absolute path.
 * @return The absolute path corresponding to the provided path.
 */
String getFullPath(String auxPath) {
    try {
        def absolutePath = powershell script: "[System.IO.Path]::GetFullPath(\"${auxPath}\")", returnStdout: true
        return absolutePath.trim()
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This method returns the absolute path from a relative path within the Jenkins workspace.
 *
 * @param relativePath The relative path within the Jenkins workspace.
 *                    If the path does not exist, the method creates the necessary directories.
 * @return The absolute path corresponding to the provided relative path within the Jenkins workspace.
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


/**
 * This method forcefully removes a directory and its contents.
 *
 * @param dirPath The relative path of the directory to be removed.
 *                If the directory exists, it will be deleted along with all its contents.
 *                If the directory does not exist, no action will be taken.
 */
void removeDirectoryPath(String dirPath) {
    try {
        powershell label: "Remove path: ${dirPath}",
            script: """
                if(Test-Path -Path '${dirPath}') {
                    Remove-Item -Path '${dirPath}' -Recurse -Force 
                }
            """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this