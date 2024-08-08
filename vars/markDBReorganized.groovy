/**
 * This method marks the database as reorganized using the provided arguments.
 * 
 * @param args a map containing the following keys:
 *   - customMSBuildScript: (String) Path to the custom MSBuild script.
 *   - localGXPath: (String) Path to the local GX directory.
 *   - localKBPath: (String) Path to the local knowledge base.
 *   - environmentName: (String) Name of the environment.
 *   - generator: (String) The generator to be used (e.g., "Java").
 *   - dataSource: (String) The data source to be used.
 *   - jdkInstallationId: (String) The ID of the JDK installation (required if generator is "Java").
 *   - tomcatVersion: (String, optional) The version of Tomcat to be used, if applicable.
 *   - msbuildExePath: (String) Path to the MSBuild executable.
 */
void markDBReorganized(Map args = [:]) {
    try {
        String target = "${args.customMSBuildScript} /t:UpdateInstallationModel"
        String msbuildGenArgs = ''
        msbuildGenArgs = concatArgs(msbuildGenArgs, "GX_PROGRAM_DIR", "${args.gxBasePath}")
        msbuildGenArgs = concatArgs(msbuildGenArgs, "localKbPath", "${args.localKBPath}")
        msbuildGenArgs = concatArgs(msbuildGenArgs, "EnvironmentName", args.environmentName)
        msbuildGenArgs = concatArgs(msbuildGenArgs, "Generator", args.generator)
        msbuildGenArgs = concatArgs(msbuildGenArgs, "DataSource", args.dataSource)
        if(args.generator == "Java") {
            String jdkToolPath = tool name: args.jdkInstallationId, type: 'jdk'
            msbuildGenArgs = concatArgs(msbuildGenArgs, "JavaPath", jdkToolPath)
            if(args.tomcatVersion) {
                msbuildGenArgs = concatArgs(msbuildGenArgs, "TomcatVersionName", parseTomcatVersion(args.tomcatVersion))
            }
        } 
        bat label: "Apply reorganization", 
            script: "\"${args.msbuildExePath}\" ${target} ${msbuildGenArgs} /nologo "

    } catch (error) {
        currentBuild.result = 'FAILURE'
        echo " ERROR ${error.getMessage()}"
        throw error
    }
}