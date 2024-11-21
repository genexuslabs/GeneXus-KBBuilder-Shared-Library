package com.genexus
import groovy.text.StreamingTemplateEngine
import com.genexus.GeneXusHelper

/**
 * This method
 * @param 
 */
String createTemplate(String templateName, def params) {
    def fileContents = libraryResource(templateName)
    def engine = new StreamingTemplateEngine()
    return engine.createTemplate(fileContents).make(params).toString()
}

@NonCPS
def getRepositoryChanges() {
    List<Map<String, Object>> changes = []

    try {
        def changeLogSets = currentBuild.changeSets
        if (changeLogSets.size() != 0) { 
            for (def entry in changeLogSets) {
                if (entry instanceof hudson.plugins.git.GitChangeSetList) {
                    for (def revision in entry.items) {
                        def date = new Date(revision.timestamp).toString()
                        def filesCount = revision.affectedFiles.size()

                        List<String> modifiedFiles = []
                        for (file2 in revision.affectedFiles) {
                            modifiedFiles << file2.path
                        }

                        changes << [
                            commitId     : revision.commitId.toString(),
                            date         : date,
                            author       : revision.author.toString(),
                            message      : revision.msg.toString(),
                            filesCount   : filesCount,
                            modifiedFiles: modifiedFiles
                        ]
                    }
                }
            }
        } else {
            echo " [DEBUG] no commits"
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }

    return changes 
}

def getChanges(int source) {
    // 0: All
    // 1: Only GxServer Knowledge Base
    // 2: Only Git
    List<Map<String, Object>> changes = []

    try {
        def changeLogSets = currentBuild.changeSets
        if (changeLogSets.size() != 0) {
            for (def entry in changeLogSets) {
                if ((entry instanceof org.jenkinsci.plugins.genexus.server.GXSChangeLogSet && (source == 1 || source == 0)) || 
                    (entry instanceof hudson.plugins.git.GitChangeSetList && (source == 2 || source == 0))) {
                    processRevisions(entry.items, changes)
                }
            }
        } else {
            echo " [DEBUG] no commits"
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }

    return changes
}

def processRevisions(items, changes) {
    for (def revision in items) {
        def date = new Date(revision.timestamp).toString()
        def filesCount = revision.affectedFiles.size()

        List<String> modifiedFiles = []
        for (file2 in revision.affectedFiles) {
            modifiedFiles << file2.path
        }

        changes << [
            commitId     : revision.commitId.toString(),
            date         : date,
            author       : revision.author.toString(),
            message      : revision.msg.toString(),
            filesCount   : filesCount,
            modifiedFiles: modifiedFiles
        ]
    }
}

def getAllKnowledgeBaseMessages() {
    def commitMessages = []
    def changeLogSets = currentBuild.changeSets
    if (changeLogSets.size() != 0) { 
        for (def entry in changeLogSets) {
            if (entry instanceof org.jenkinsci.plugins.genexus.server.GXSChangeLogSet) {
                for (def revision in entry.items) {
                    commitMessages << revision.msg.toString()
                }
            }
        }
    } else {
        echo " [DEBUG] no commits"
    }
    return commitMessages
}

void printCommit() {
    try{
        getChangeLogSet()
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * This methods 
 * @param 
 */
@NonCPS 
String getChangeLogSet() {
    String revisions = ""
    def changeLogSets = currentBuild.changeSets
    if(changeLogSets.size() != 0) {
        Boolean isEven = false
        for (def entry in changeLogSets) {
            for (def revision in entry.items) {
                def date = new Date(revision.timestamp)
                if(isEven) { revisions += "<tr class=\"revisions-even\">" }
                else { revisions += "<tr class=\"revisions-odd\">" }
                isEven = !(isEven)
                revisions +="<th class=\"revision-item\">${revision.commitId.toString()}</th>"
                revisions +="<th class=\"revision-item\" style=\"width:240px;\">${date.toString()}</th>"
                revisions +="<th class=\"revision-item\" style=\"text-align:left;padding-left:5px;\">${revision.author.toString()}</th>"
                revisions +="<th class=\"revision-item\" style=\"text-align:left;padding-left:5px;\">${revision.msg.toString()}</th>"
                def count = 0
                def files = new ArrayList(revision.affectedFiles)
                for (def file in files) {
                //    echo " ${file.editType.name} ${file.path}"
                    count += 1
                }
                revisions +="<th class=\"revision-item\" style=\"text-align:center;padding-left:5px;width:60px;\">${count.toString()} Files</th>"
                revisions += "</tr>"
            }
        }
    }
    else {
        revisions += "<tr class=\"revisions-even\">"
        revisions +="<th class=\"revision-item\">No changes</th>"
        revisions += "</tr>"
    }
    return revisions
}

return this

// package com.genexus
// import groovy.text.StreamingTemplateEngine

// /**
//  * This method
//  * @param 
//  */
// String createTemplate(String templateName, def params) {
//     def fileContents = libraryResource(templateName)
//     def engine = new StreamingTemplateEngine()
//     return engine.createTemplate(fileContents).make(params).toString()
// }

// /**
//  * This methods 
//  * @param 
//  */
// @NonCPS 
// String getChangeLogSet() {
//     String revisions = ""
//     def changeLogSets = currentBuild.changeSets
//     if(changeLogSets.size() != 0) {
//         Boolean isEven = false
//         for (def entry in changeLogSets) {
//             for (def revision in entry.items) {
//                 def date = new Date(revision.timestamp)
//                 if(isEven) { revisions += "<tr class=\"revisions-even\">" }
//                 else { revisions += "<tr class=\"revisions-odd\">" }
//                 isEven = !(isEven)
//                 revisions +="<th class=\"revision-item\">${revision.commitId.toString()}</th>"
//                 revisions +="<th class=\"revision-item\" style=\"width:240px;\">${date.toString()}</th>"
//                 revisions +="<th class=\"revision-item\" style=\"text-align:left;padding-left:5px;\">${revision.author.toString()}</th>"
//                 revisions +="<th class=\"revision-item\" style=\"text-align:left;padding-left:5px;\">${revision.msg.toString()}</th>"
//                 def count = 0
//                 def files = new ArrayList(revision.affectedFiles)
//                 for (def file in files) {
//                 //    echo " ${file.editType.name} ${file.path}"
//                     count += 1
//                 }
//                 revisions +="<th class=\"revision-item\" style=\"text-align:center;padding-left:5px;width:60px;\">${count.toString()} Files</th>"
//                 revisions += "</tr>"
//             }
//         }
//     }
//     else {
//         revisions += "<tr class=\"revisions-even\">"
//         revisions +="<th class=\"revision-item\">No changes</th>"
//         revisions += "</tr>"
//     }
//     return revisions
// }

// Map getBuildInfo() {
//     Map ret = [:]
//     switch (currentBuild.currentResult) {
//         case 'SUCCESS':
//             ret.icon = "✅"
//             ret.buildColor = "green"
//             ret.buildResult = "Success"
//         break
//         case 'UNSTABLE':
//             ret.icon = "⚠️"
//             ret.buildColor = "#FFC300"
//             ret.buildResult = "Unstable"
//         break
//         case 'FAILURE':
//             ret.icon = "❌"
//             ret.buildColor = "red"
//             ret.buildResult = "Failure"
//         break
//         case 'NOT_BUILT':
//             ret.icon = "🔳"
//             ret.buildColor = "black"
//             ret.buildResult = "Not build"
//         break
//         case 'ABORTED':
//             ret.icon = "⛔"
//             ret.buildColor = "orange"
//             ret.buildResult = "Aborted"
//         break
//         default:
//             ret.icon = "🚨"
//             ret.buildColor = "red"
//             ret.buildResult = "Unknown"
//         break
//     }
//     return ret
// }


// void sendEmail(Map args = [:]) {
//     String flag = "[${args.notificationWhen}]"
//     if(flag.contains(currentBuild.currentResult)) {
//         def gxHelper = new GeneXusHelper()
//         String gxVersion = gxHelper.getGeneXusInstallationVersion(args.gxBasePath)
//         def changeLogSet = getChangeLogSet()
//         Map emailConst = getBuildInfo()

//         String templateName = "com/genexus/notificationTemplates/emailBuildResult.html.groovy"
//         def template = createTemplate(templateName, [
//             "jenkinsUrl"        :   env.BUILD_URL,
//             "jenkinsTimestamp"  :   env.BUILD_TIMESTAMP,
//             "buildNumber"       :   env.BUILD_NUMBER,
//             "buildColor"        :   emailConst.buildColor,
//             "buildResult"       :   emailConst.buildResult,
//             "jenkinsDuration"   :   currentBuild.durationString.replaceAll(' and counting', ''),
//             "changeLogSet"      :   changeLogSet,
//             "cause"             :   currentBuild.buildCauses[0].shortDescription.replaceAll('\\[',' '),
//             "gxversion"         :   gxVersion
//         ]);

//         emailext body: template,
//             mimeType: 'text/html',
//             subject: "${emailConst.icon} ${currentBuild.fullDisplayName}",
//             to: "${args.notificationRecipient}, cc:${args.notificationBaseList.replace(', ', ', cc:')}",
//             attachLog: true
//     }
// }
// return this