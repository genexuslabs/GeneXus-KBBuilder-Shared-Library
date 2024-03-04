/*
 * Job closeKnowledgeBase >> Close KB method, commonly used to force detachment from the local database
 *
 * @Param args = [:]
 * +- gxBasePath
 * +- localKBPath
 */

def call(Map args = [:]) {
    if(args.localKBPath != null) {
        // Sync cdxci.msbuild
        def fileContents = libraryResource 'com/genexus/templates/cdxci.msbuild'
        writeFile file: 'cdxci.msbuild', text: fileContents
        
        bat label: "Close Knowledge Base",
            script: """
                "${args.msbuildExePath}" "${WORKSPACE}\\cdxci.msbuild" \
                /p:GX_PROGRAM_DIR="${args.gxBasePath}" \
                /p:localKbPath="${args.localKBPath}" \
                /t:CloseKB
            """
    }
}