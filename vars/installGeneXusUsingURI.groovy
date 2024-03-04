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
def genexus = new GeneXusHelper()

def call(Map args = [:]) {
    def fileContents
    if(Boolean.valueOf(args.forceUpdateGX)) {
        genexus.deleteGeneXusInstallation(args.gxBasePath, args.localAndroidSDKPath)
    }
    genexus.updateGeneXusInstallationByURI(args.gxBasePath, args.genexusURI, args.localAndroidSDKPath)

    genexus.configureProtectionServer(args.gxBasePath, args.protServerType, args.protServerName, args.protServerCredentialsId)
    
    genexus.getGeneXusInstallationVersion(args.gxBasePath)
}