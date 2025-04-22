package com.genexus

/**
 * Synchronizes a file to a remote server using SCP.
 *
 * @param args A map containing the following parameters:
 *   - sshKeyPath: The path to the SSH key used for authentication.
 *   - fileFullPath: The full path to the file that needs to be synchronized.
 *   - dbnUsername: The username for the remote server.
 *   - dbnIP: The IP address of the remote server.
 *   - dbnSyncPath: The destination path on the remote server where the file will be synchronized.
 */
void scpSyncToDBN(Map args = [:]) {
    try {
        powershell(
            label: "Sync DU package to DBN",
            script: """
                try {
                    \$ErrorActionPreference = 'Stop'
                    Write-Output "\$(Get-Date -Format G) [INFO] Start sync file"
                    scp -i "${args.sshKeyPath}" "${args.fileFullPath}" "${args.dbnUsername}@${args.dbnIP}:${args.dbnSyncPath}"
                    Write-Output "\$(Get-Date -Format G) [INFO] Finish sync file"
                } catch {
                    if (\$Error) {
                        Write-Host "Error(s) encontrados:"
                        \$Error | ForEach-Object {
                            Write-Host \$_.Exception
                        }
                    }
                    throw
                }
            """
        )
    } catch (Exception err) {
        echo "[ERROR] Sync process failed: ${err.getMessage()}"
        throw err
    }
}

return this
