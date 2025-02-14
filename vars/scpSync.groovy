import com.genexus.FileHelper

// # Job: SCPSync

// ## Descripción

// El método `SCPSyncFiles` es un trabajo que comprime un directorio especificado en un archivo ZIP y sincroniza ese archivo con un servidor remoto usando SCP. 
// Este proceso es útil para empaquetar y transferir archivos de manera segura durante los despliegues o migraciones.

// ## Parámetros

// - `args`: Un mapa que contiene los siguientes parámetros:
//   - `sourceFolder`: La carpeta que se comprimirá en un archivo ZIP.
//   - `targetPath`: La ruta de destino donde se guardará el archivo ZIP localmente.
//   - `sshKeyPath`: La ruta a la clave SSH utilizada para autenticarse en el servidor remoto.
//   - `dbnUsername`: El nombre de usuario para autenticarse en el servidor remoto.
//   - `dbnIP`: La dirección IP del servidor remoto.
//   - `dbnSyncPath`: La ruta de destino en el servidor remoto donde se sincronizará el archivo ZIP.

// ## Pasos del Flujo de Trabajo

// 1. Cargar y escribir el script de PowerShell `zip-directory.ps1` desde los recursos de la biblioteca.
// 2. Ejecutar el script de PowerShell para comprimir el directorio especificado.
// 3. Verificar el éxito de la operación de compresión.
// 4. Sincronizar el archivo ZIP con el servidor remoto utilizando SCP.
// 5. Manejar errores durante el proceso de sincronización y lanzar excepciones si es necesario.

// ## Notas

// - Asegúrate de que las herramientas necesarias (PowerShell, SCP) estén instaladas y configuradas correctamente en el entorno de ejecución de Jenkins.
// - Verifica que las credenciales y rutas proporcionadas sean correctas y estén protegidas adecuadamente.

def call(Map args = [:]) {
    def sysLibHelper = new FileHelper()

    sysLibHelper.winCompressDirectory(args.sourceDir, args.targetPath)
    try {
        powershell(
            label: "Sync DU package to DBN",
            script: """
                try {
                    \$ErrorActionPreference = 'Stop'
                    Write-Host "INFO: Files zipped successfully"
                    
                    Write-Host "INFO: Start sync zip file"
                    scp -i "${args.sshKeyPath}" "${args.targetPath}" "${args.dbnUsername}@${args.dbnIP}:${args.dbnSyncPath}"
                    Write-Host "INFO: Finish sync zip file"
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
        echo "[INFO] FINISH SYNC FILES"
    } catch (Exception err) {
        echo "[ERROR] Sync process failed: ${err.getMessage()}"
        throw err
    }
}

