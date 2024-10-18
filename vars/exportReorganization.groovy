/*
 * Job: exportReorganization
 *
 * Description:
 * This job exports the reorganization script for a GeneXus Knowledge Base to a specified path. It determines
 * the type of data source (MySQL or SQLServer) and configures the reorganization export accordingly. The exported
 * reorganization script is then copied to the specified export path.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - dataSource: The type of data source (e.g., "MySQL" or "SQLServer").
 *   - reorgExportPath: The path where the reorganization script will be exported.
 *   - environmentName: The name of the environment to be used.
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - targetPath: The target path within the Knowledge Base.
 *   - BUILD_NUMBER: The build number to be used in naming the exported files.
 *   - generator: The generator to be used for the reorganization.
 *
 * Workflow Steps:
 * 1. Determine if the data source is MySQL by checking the value of ${args.dataSource}.
 * 2. Log the start of the reorganization export process.
 * 3. Execute the MSBuild command to export the reorganization script using the provided parameters.
 * 4. Copy the exported reorganization script to the specified export path.
 *
 */
def call(Map args = [:]) {
    boolean isMySQL = false;
    boolean isSQLServer = false;

    switch (args.dataSource) {
        case "MySQL":
            isMySQL = true;
            break;
        case "SQL Server":
            isSQLServer = true;
            break;
        default:
            throw new IllegalArgumentException("Unsupported DataStore type: ${args.dataSource}");
    }
    
    try {
        bat label: "Export reorganization::${args.environmentName}", 
        script: """
            "${args.msbuildExePath}" "${args.gxBasePath}\\deploy.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /p:SourcePath="${args.localKBPath}\\${args.targetPath}" \
            /p:ReorgDestination="${args.reorgExportPath}" \
            /p:FileName="${env.BUILD_NUMBER}_reorg.jar" \
            /p:Generator="${args.generator}" \
            /p:SourcePath="${args.localKBPath}\\${args.targetPath}" \
            /p:MySQL="${isMySQL}" \
            /p:SQLServer="${isSQLServer}" \
            /t:ExportReorganization 
        """
        
        powershell script: "Copy-Item \"${args.localKBPath}\\${args.targetPath}\\Web\\ReorganizationScript.txt\" \"${args.reorgExportPath}\\${env.BUILD_NUMBER}_ReorganizationScript.txt\""
        echo "[INFO] Export reorganization to ${args.reorgExportPath}"
    } catch (error) {
        echo "[ERROR] An error occurred during the reorganization export process: ${error}"
        throw error
    }
}