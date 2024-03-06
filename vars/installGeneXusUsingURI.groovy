/*
 * Job installGeneXusUsingURI:
 * -- >> IF [forceUpdateGX] >> DELETE GX INSTALLATION
 * -- >> COMPARE [genexusURI] WITH [gxBasePath]\\LastURI.txt
 * -- -- >> UPDATE [gxBasePath] FROM [genexusURI]
 * -- -- >> CONFIG ProgramData and UserData DIRECTORIES
 * -- -- >> RUN [gxBasePath]\\install
 * -- -- >> IF [localAndroidSDKPath] DOWNLOAD USING [gxBasePath]\Android\Setup\AndroidRequirements.exe
 * -- >> CONFIGURE [protServerType], [protServerName], [protServerCredentialsId]
 * -- >> PRINT GENEXUS VERSION
 *
 * @Param args = [:]
 * +- forceUpdateGX
 * +- gxBasePath
 * +- localAndroidSDKPath
 * +- genexusURI
 * +- protServerType, protServerName, protServerCredentialsId
 */

import com.genexus.GeneXusHelper

def call(Map args = [:]) {
    def gxHelper = new GeneXusHelper()
    if(Boolean.valueOf(args.forceUpdateGX)) {
        gxHelper.deleteGeneXusInstallation(args.gxBasePath, args.localAndroidSDKPath)
    }
    gxHelper.updateGeneXusInstallationByURI(args.gxBasePath, args.genexusURI, args.localAndroidSDKPath)

    gxHelper.configureProtectionServer(args.gxBasePath, args.protServerType, args.protServerName, args.protServerCredentialsId)

    def gxVersion = gxHelper.getGeneXusInstallationVersion(args.gxBasePath)
    echo "Using GeneXus Installation version::${gxVersion}"
}