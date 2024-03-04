/*
 * Job installGeneXusUsingURI >> 
 *                              1. IF [forceUpdateGX] >> DELETE GX INSTALLATION
 *                              2. COMPARE [genexusURI] WITH [gxBasePath]\\LastURI.txt
 *                              -- 2.1. UPDATE [gxBasePath] FROM [genexusURI]
 *                              -- 2.2. CONFIG ProgramData and UserData DIRECTORIES
 *                              -- 2.3. RUN [gxBasePath]\\install
 *                              -- 2.4. IF [localAndroidSDKPath] DOWNLOAD USING [gxBasePath]\Android\Setup\AndroidRequirements.exe
 *                              3. CONFIGURE [protServerType], [protServerName], [protServerCredentialsId]
 *                              4. PRINT GENEXUS VERSION
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
    def helper = new GeneXusHelper()
    if(Boolean.valueOf(args.forceUpdateGX)) {
        helper.deleteGeneXusInstallation(args.gxBasePath, args.localAndroidSDKPath)
    }
    helper.updateGeneXusInstallationByURI(args.gxBasePath, args.genexusURI, args.localAndroidSDKPath)

    helper.configureProtectionServer(args.gxBasePath, args.protServerType, args.protServerName, args.protServerCredentialsId)

    def gxVersion = helper.getGeneXusInstallationVersion(args.gxBasePath)
    echo "Using GeneXus Installation version::${gxVersion}"
}