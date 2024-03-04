package com.genexus
import groovy.text.StreamingTemplateEngine

/**
 * This method
 * @param 
 */
String createTemplate(String templateName, def params) {
    def fileContents = libraryResource(templateName)
    def engine = new StreamingTemplateEngine()
    return engine.createTemplate(fileContents).make(params).toString()
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