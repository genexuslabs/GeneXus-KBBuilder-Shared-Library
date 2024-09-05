/*
 * Job: packageModule
 *
 * Description:
 * This job executes the 'PackageGXModule' task to publish a module from the working model to a local path.
 * For detailed information on the task, refer to the documentation:
 * https://wiki.genexus.com/commwiki/wiki?55011,Modules%20MsBuild%20Tasks%20%28GeneXus%2018%20Upgrade%203%20or%20prior%29#PackageModule+Task
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - packageModuleName: The name of the module to be packaged.
 *   - csharpEnvName: The C# environment name.
 *   - javaEnvName: The Java environment name.
 *   - netCoreEnvName: The .NET Core environment name.
 *
 * Additional Information:
 * - propsFile: An auxiliary file where the 'GetObjectProperty' task writes the module version.
 * - moduleTargetPath: The local path inside the Knowledge Base where the packaged module is intended to be stored.
 * - packageModuleName: The module version is read from the 'propsFile' file during the execution.
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents
    
    def propsFile = "${WORKSPACE}\\modVer.json"
    def moduleTargetPath = "${args.localKBPath}\\CDCI_Modules\\${args.packageModuleName}\\${env.BUILD_NUMBER}"

    bat script: """
        "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
        /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
        /p:localKbPath="${args.localKBPath}" \
        /p:packageModuleName="${args.packageModuleName}" \
        /p:pipelineBuildNumber="${env.BUILD_NUMBER}" \
        /p:csharpEnvName="${args.csharpEnvName}" \
        /p:javaEnvName="${args.javaEnvName}" \
        /p:netCoreEnvName="${args.netCoreEnvName}" \
        /p:destinationPath="${moduleTargetPath}" \
        /p:propFileAbsolutePath="${propsFile}" \
        /p:helperName="aux" \
        /t:PackageGXModule
    """
    def packageModuleName = readJSON file: propsFile
    return "${moduleTargetPath}\\${args.packageModuleName}_${packageModuleName.aux}.opc"
}