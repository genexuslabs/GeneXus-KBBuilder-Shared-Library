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

def call(Map args = [:], String dataStoreName) {
    def databaseName
    def datastoreServer
    def datastorePort
    def datastoreCredentialsId
    switch (dataStoreName) {
        case 'GAM':
            databaseName = args.gamDBName
            datastoreServer = args.gamDBServer
            datastorePort = args.gamDBServerPort
            datastoreCredentialsId = args.gamDBCredentialsId
        break
        case 'GXFlow':
            databaseName = args.gxflowDBName
            datastoreServer = args.gxflowDBServer
            datastorePort = args.gxflowDBServerPort
            datastoreCredentialsId = args.gxflowDBCredentialsId
        break
        default:
            databaseName = args.storageDBName
            datastoreServer = args.storageDBServer
            datastorePort = args.storageDBServerPort
            datastoreCredentialsId = args.storageDBCredentialsId
        break
    }
    // Sync properties.msbuild
    def fileContents = libraryResource 'com/genexus/templates/properties.msbuild'
    writeFile file: 'properties.msbuild', text: fileContents

    String target = ' /t:ConfigureDataStore'

    String msbuildGenArgs = ''
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "GX_PROGRAM_DIR", args.localGXPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "localKbPath", args.localKBPath)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "environmentName", args.environmentName)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "generator", args.generator)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "dataSource", args.dataSource)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "dataStoreName", dataStoreName)

    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "dbName", databaseName)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "dbServerName", datastoreServer)
    msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "dbServerPort", datastorePort)
    withCredentials([usernamePassword(credentialsId: datastoreCredentialsId, usernameVariable: 'username', passwordVariable: 'password')]) {
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "dbServerUser", username)
        msbuildGenArgs = concatMSBuildArgs(msbuildGenArgs, "dbServerPass", password)
    }
    bat label: 'Writing commiteable properties',
        script: "\"${args.msbuildExePath}\" .\\properties.msbuild ${target} ${msbuildGenArgs} /nologo "
}