/*
 * Job configureDataStore >> config datastore default
 *
 * @Param args = [:]
 * +- localGXPath
 * +- localKBPath
 * +- environmentName
 * +- generator
 * +- dataSource
 * +- storageDBName, storageDBServer, storageDBServerPort, storageDBCredentialsId
 * +- generator
 */

def call(Map args = [:]) {
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
    writeFile file: 'properties.msbuild', text: fileContents

    withCredentials([usernamePassword(credentialsId: args.storageDBCredentialsId, usernameVariable: 'username', passwordVariable: 'password')]) {
        bat label: "Configure Storage Datastore", 
            script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\properties.msbuild" \
                /p:GX_PROGRAM_DIR="${args.localGXPath}" \
                /p:localKbPath="${args.localKBPath}" \
                /p:environmentName="${args.environmentName}" \
                /p:generator="${args.generator}" \
                /p:dataSource="${args.dataSource}" \
                /p:dataStoreName="Default" \
                /p:dbName="${args.storageDBName}" \
                /p:dbServerName="${args.storageDBServer}" \
                /p:dbServerPort="${args.storageDBServerPort}" \
                /p:dbServerUser="${username}" \
                /p:dbServerPass="${password}" \
                /t:ConfigureDataStore
            """
    }
}