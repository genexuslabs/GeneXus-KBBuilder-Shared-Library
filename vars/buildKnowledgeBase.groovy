/*
 * Job buildKnowledgeBase >> Read properties from environment
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

    bat label: 'Build all', 
        script: """
            "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
            /p:GX_PROGRAM_DIR="${args.localGXPath}" \
            /p:localKbPath="${args.localKBPath}" \
            /p:environmentName="${args.environmentName}" \

            /p:Generator="${args.generator}" \
            /p:DataSource="${args.dataSource}" \
            /p:rebuild="${args.forceRebuild)}" \
            /p:JavaPath="${args.javaPath}" \
            /p:TomcatVersionName="${args.tomcatVersionName}" \
            /p:ServletDir="${args.localKBPath}\\${args.targetPath}\\web\\Deploy\\servlet" \
            /p:StaticDir="${args.localKBPath}\\${args.targetPath}\\web\\Deploy\\static" \
            /p:JDBClogFile="${args.jdbcLogPath}" \
            /p:androidSDKpath="${args.genexusNeedAndroidSDK}" \
            /p:fullTestResultsFile= \
            /t:BuildKnowledgeBase
        """
}