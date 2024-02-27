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

/*
 * Job readPipelineProperties >> Read properties from build.properties
 *
 * @Param args = [:]
 * +- gitUrl
 * +- gitBranch
 * +- gitCredentialsId
 * +- propertiesFilePath
 * +- machineFilePath
 */

def call(Map args = [:]) {
    try {
        def changeLogSet = getChangeLogSet(currentBuild)
        def icon
        String template
        String templateName
        String buildColor
        String buildResult
        String jobName = (env.JOB_NAME).replace(env.JOB_BASE_NAME, '')
        String jobFullDisplayName = currentBuild.fullDisplayName
        def splitJobDisplayName = currentBuild.fullDisplayName.split(' » ')
        def jobDisplayName = "${jobFullDisplayName.replace(splitJobDisplayName[splitJobDisplayName.length - 1], '')}"
        switch (currentBuild.currentResult) {
            case 'SUCCESS':
                icon = "✅"
                buildColor = "green"
                buildResult = "Success"
            break
            case 'UNSTABLE':
                icon = "⚠️"
                buildColor = "#FFC300"
                buildResult = "Unstable"
            break
            case 'FAILURE':
                icon = "❌"
                buildColor = "red"
                buildResult = "Failure"
            break
            case 'NOT_BUILT':
                icon = "🔳"
                buildColor = "black"
                buildResult = "Not build"
            break
            case 'ABORTED':
                icon = "⛔"
                buildColor = "orange"
                buildResult = "Aborted"
            break
            default:
                icon = "🚨"
                buildColor = "red"
                buildResult = "Unknown"
            break
        }

        templateName = "notificationTemplates/emailBuildResult.html.groovy"
        template = createTemplate(templateName, [
            "jenkinsJobName"    :   jobName,
            "jenkinsUrl"        :   env.BUILD_URL,
            "jenkinsTimestamp"  :   env.BUILD_TIMESTAMP,
            "buildNumber"       :   env.BUILD_NUMBER,
            "buildColor"        :   buildColor,
            "buildResult"       :   buildResult,
            "jenkinsDuration"   :   currentBuild.durationString.replaceAll(' and counting', ''),
            "changeLogSet"      :   changeLogSet,
            "cause"             :   currentBuild.buildCauses[0].shortDescription.replaceAll('\\[',' '),
            "gxversion"         :   gxVersion
        ]);

        emailext body: template,
            mimeType: 'text/html',
            subject: "${icon} ${jobDisplayName.toString()} Build #${env.BUILD_NUMBER} » ${currentBuild.currentResult}",
            to: "${notificationAction.to}",
            replyTo: "${notificationAction.to}",
            attachLog: true
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}