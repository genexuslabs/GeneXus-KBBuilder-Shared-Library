/*
 * Job publishModule >> This job publishes a GeneXus module using the PublishGXModule MSBuild task. 
 * -- >> For detailed information on the task refer to the documentation https://wiki.genexus.com/commwiki/wiki?55011,Modules%20MsBuild%20Tasks%20%28GeneXus%2018%20Upgrade%203%20or%20prior%29#PublishModule+Task
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - gxBasePath: The path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - moduleServerCredentialsId: The credentials ID containing the username and password for the module server.
 *   - localModulePackage: The local path to the module package.
 *   - moduleServerId: The ID of the module server.
 *
 */

def call(Map args = [:]) {
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    withCredentials([ usernamePassword( credentialsId: args.moduleServerCredentialsId,
        usernameVariable: 'username',
        passwordVariable: 'password')
    ]) {
        bat label: "Package Module::${args.packageModuleName}",
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:opcPath="${args.localModulePackage}" \
            /p:serverId="${args.moduleServerId}" \
            /p:serverUsername="${username}" \
            /p:serverPassword="${password}" \
            /t:PublishGXModule
        """
    }
}