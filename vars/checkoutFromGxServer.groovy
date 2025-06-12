/*
 * Job: checkoutFromGxServer
 * * Description:
 * This job connects to a GeneXus Server to check out or update a specific Knowledge Base (KB) 
 * into a local workspace. It utilizes the 'gxserver' plugin to handle the communication and
 * synchronization of the KB. The behavior of polling for changes and generating a changelog
 * can be controlled via parameters, both defaulting to true.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 * - changelog: (Optional) Boolean. If true, generates a changelog from the SCM. Defaults to true.
 * - poll: (Optional) Boolean. If true, polls the SCM for changes. Defaults to true.
 * - gxBasePath: The base path of the GeneXus installation.
 * - msbuildExePath: The path to the MSBuild executable.
 * - gxserverURL: The URL of the target GeneXus Server.
 * - gxserverCredentials: The Jenkins credentialsId for authenticating with GeneXus Server.
 * - gxserverKB: The name of the Knowledge Base on the server.
 * - gxserverVersion: The name of the KB Version on the server (e.g., "Trunk").
 * - kbDbServerInstance: The database server instance for the local Knowledge Base.
 * - localKBPath: The local directory path where the Knowledge Base will be checked out or updated.
 * - localKbVersion: The version name to be used for the local KB (in this script, it's set from gxserverKB).
 *
 * Return Type:
 * - void: This method does not return a specific value. It will throw an exception if the underlying plugin fails.
 *
 * Workflow Steps:
 * 1. Determine the values for 'changelog' and 'poll', using 'true' as a default if they are not provided.
 * 2. Execute the 'gxserver' plugin, passing all the provided parameters.
 * 3. The plugin connects to the specified GeneXus Server and brings the changes from the remote KB version
 * to the local KB path.
 */
def call(Map args = [:]) {
    gxserver changelog: (args.changelog ?? true), 
             poll: (args.poll ?? true),
             gxCustomPath: "${args.gxBasePath}",
             msbuildCustomPath: "${args.msbuildExePath}",
             serverURL: args.gxserverURL,
             credentialsId: args.gxserverCredentials,
             kbName: args.gxserverKB,
             kbVersion: args.gxserverVersion,
             kbDbServerInstance: "${args.kbDbServerInstance}",
             localKbPath: "${args.localKBPath}",
             localKbVersion: args.gxserverKB
}