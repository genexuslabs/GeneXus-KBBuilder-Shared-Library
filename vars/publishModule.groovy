/*
 * Job: publishModule
 *
 * Description:
 * This job publishes a GeneXus module using the PublishGXModule MSBuild task. For detailed information 
 * on the task, refer to the documentation at 
 * https://wiki.genexus.com/commwiki/wiki?55011,Modules%20MsBuild%20Tasks%20%28GeneXus%2018%20Upgrade%203%20or%20prior%29#PublishModule+Task
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - msbuildExePath: The path to the MSBuild executable.
 *   - gxBasePath: The path of the GeneXus installation.
 *   - localKBPath: The local path of the Knowledge Base.
 *   - localModulePackage: The local path to the module package.
 *   - moduleServerId: The ID of the module server.
 *   - moduleServerCredentialsId: The credentials ID containing the username and password for the module server.
 *
 * Workflow Steps:
 * 1. Synchronize the properties file (properties.msbuild).
 * 2. Retrieve the module server credentials.
 * 3. Execute the PublishGXModule MSBuild task to publish the module.
 */

def call(Map args = [:]) {
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents

    def serverUsername = ''
    def serverPassword = ''

    if(args.moduleServerCredentialsId) {
        withCredentials([ usernamePassword( credentialsId: args.moduleServerCredentialsId,
            usernameVariable: 'username',
            passwordVariable: 'password')
        ]) {
            serverUsername = username
            serverPassword = password
        }
    }
    bat label: "Package Module::${args.packageModuleName}",
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:opcPath="${args.localModulePackage}" \
            /p:serverId="${args.moduleServerId}" \
            /p:serverUsername="${serverUsername}" \
            /p:serverPassword="${serverPassword}" \
            /t:PublishGXModule
        """
}