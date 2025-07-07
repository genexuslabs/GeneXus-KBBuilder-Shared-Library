/*
 * Job: installGeneXusUsingURI
 *
 * Description:
 * This job performs a series of tasks to manage and update a GeneXus installation. It includes options
 * to force update the GeneXus installation, compare and update the GeneXus URI, configure ProgramData
 * and UserData directories, run the GeneXus installation, download Android SDK if necessary, configure
 * Protection Server settings, and print the GeneXus version.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - forceUpdateGX: Boolean flag indicating whether to force the update of the GeneXus installation.
 *   - gxBasePath: The base path of the GeneXus installation.
 *   - localAndroidSDKPath: The local path of the Android SDK.
 *   - genexusURI: The GeneXus URI for updating the installation.
 *   - protServerType: Type of Protection Server.
 *   - protServerName: Name or address of the Protection Server.
 *   - protServerCredentialsId: Credentials ID containing username and password for the Protection Server.
 *   - runGXInstall: Boolean flag indicating whether to run the GeneXus installation command. Defaults to true if not provided.
 *
 * Workflow Steps:
 * 1. If ${forceUpdateGX} is true, delete the existing GeneXus installation.
 * 2. Compare genexusURI with ${gxBasePath}\LastURI.txt and update the installation accordingly.
 * 3. Configure ProgramData and UserData directories.
 * 4. Execute ${gxBasePath}\genexus.com /install if ${runGXInstall} is true.
 * 5. If ${localAndroidSDKPath} is provided, download Android SDK using ${gxBasePath}\AndroidRequirements.exe.
 * 6. Configure Protection Server.
 * 7. Print the GeneXus installation version.
 *
 */
import com.genexus.GeneXusHelper

def call(Map args = [:]) {
    def gxHelper = new GeneXusHelper()
    if(Boolean.valueOf(args.forceUpdateGX)) {
        gxHelper.deleteGeneXusInstallation(args.gxBasePath, args.localAndroidSDKPath)
    }
    if (args.runGXInstall == null) { args.runGXInstall = true }
    gxHelper.updateGeneXusInstallationByURI(args.gxBasePath, args.genexusURI, args.localAndroidSDKPath, args.runGXInstall, Boolean.valueOf(args.cleanCustomSpecialFolders))

    gxHelper.configureProtectionServer(args.gxBasePath, args.protServerType, args.protServerName, args.protServerCredentialsId)

    def gxVersion = gxHelper.getGeneXusInstallationVersion(args.gxBasePath)
    echo "[INFO] Using GeneXus Installation version::${gxVersion}"
}

