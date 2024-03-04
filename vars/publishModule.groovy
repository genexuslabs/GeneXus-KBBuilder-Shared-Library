/*
 * Job reorganizeDatabase >> Read properties from environment
 *
 * @Param args = [:]
 * +- localGXPath
 * +- localKBPath
 * +- environmentName
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:]) {
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents
    
    def moduleTargetPath = "${args.localKBPath}\\IntegrationModule\\${args.packageModuleName}\\${args.buildJobNumber}"

    bat script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.localGXPath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:packageModuleName="${args.packageModuleName}" \
            /p:pipelineBuildNumber="${args.buildJobNumber}" \
            /p:csharpEnvName="${args.csharpEnvName}" \
            /p:javaEnvName="${args.javaEnvName}" \
            /p:netCoreEnvName="${args.netCoreEnvName}" \
            /p:destinationPath="${moduleTargetPath}" \
            /t:PackageGXModule
        """
}