/*
 * Job: createEnvironment
 * 
 * Description:
 * This job executes the 'CreateEnvironment' task with the specified parameters. It creates a new 
 * environment in the GeneXus Knowledge Base (KB).
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - environmentName: The name of the environment to create.
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - kbTemplate: The template to use for creating the environment. ["netcore", "java", "csharp"]
 *
 * Return Type:
 * - Boolean: Returns true if the environment creation is successful, false otherwise.
 *
 * Workflow Steps:
 * 1. Sync cdxci.msbuild template from the library resource.
 * 2. Execute the 'CreateEnvironment' task using MSBuild with the specified parameters.
 * 3. Catch any errors to determine if the environment creation was successful.
 * 4. Return true if the environment was created successfully, false otherwise.
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    bat label: "Create Environment", 
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \
            /p:localKBTemplate="${args.localKBTemplate}" \
            /t:CreateLocalEnvironment
        """
    } catch (error) {
        echo "[ERROR] Failed to create environment '${args.environmentName}'"
    }
    return environmentCreated
}