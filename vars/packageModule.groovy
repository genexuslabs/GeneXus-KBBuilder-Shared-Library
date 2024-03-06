/*
 * Job packageModule >> This method executes the task 'PackageModule' to publishes a module from the working model to a local path 
 * -- >> Task documentation:: https://wiki.genexus.com/commwiki/wiki?55011,Modules%20MsBuild%20Tasks%20%28GeneXus%2018%20Upgrade%203%20or%20prior%29#PackageModule+Task
 *
 * @Param args = [:]
 * +- gxBasePath
 * +- localKBPath
 * +- environmentName
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:]) {
    // Sync cdxci.msbuild
    def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
    writeFile file: 'cdxci.msbuild', text: fileContents
    
    def moduleTargetPath = "${args.localKBPath}\\CDCI_Modules\\${args.packageModuleName}\\${env.BUILD_NUMBER}"

    bat label: "Package Module::${args.packageModuleName}",
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:packageModuleName="${args.packageModuleName}" \
            /p:pipelineBuildNumber="${env.BUILD_NUMBER}" \
            /p:csharpEnvName="${args.csharpEnvName}" \
            /p:javaEnvName="${args.javaEnvName}" \
            /p:netCoreEnvName="${args.netCoreEnvName}" \
            /p:destinationPath="${moduleTargetPath}" \
            /t:PackageGXModule
        """
    return moduleTargetPath
}