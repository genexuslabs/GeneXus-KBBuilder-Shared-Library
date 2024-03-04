/*
 * Job installGeneXusUsingURI >> Download and install GeneXus Installation from a public URI
 *
 * @Param args = [:]
 * +- forceUpdateGX
 * +- gxBasePath
 * +- localAndroidSDKPath
 * +- genexusURI
 * +- protServerType, protServerName, protServerCredentialsId
 */

import com.genexus.GeneXusHelper
def helper = new GeneXusHelper()

def call(Map args = [:]) {
    def fileContents
    if(Boolean.valueOf(args.forceUpdateGX)) {
        helper.deleteGeneXusInstallation(args.gxBasePath, args.localAndroidSDKPath)
    }
    helper.updateGeneXusInstallationByURI(args.gxBasePath, args.genexusURI, args.localAndroidSDKPath)

    helper.configureProtectionServer(args.gxBasePath, args.protServerType, args.protServerName, args.protServerCredentialsId)

    helper.getGeneXusInstallationVersion(args.gxBasePath)
}