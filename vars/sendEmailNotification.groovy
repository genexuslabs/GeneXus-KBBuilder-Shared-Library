import groovy.text.StreamingTemplateEngine
import com.genexus.NotificationHelper

/**
 * This method
 * @param 
 */
String createTemplate(String templateName, def params) {
    def fileContents = libraryResource(templateName)
    def engine = new StreamingTemplateEngine()
    return engine.createTemplate(fileContents).make(params).toString()
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
        def engine2 = new NotificationHelper()
        def changeLogSet = engine2.getChangeLogSet()
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
            "gxversion"         :   "18.0.0.0"//gxVersion
        ]);

        emailext body: template,
            mimeType: 'text/html',
            subject: "${icon} ${jobDisplayName.toString()} Build #${env.BUILD_NUMBER} » ${currentBuild.currentResult}",
            to: "jalbarellos@genexus.com",
            replyTo: "jalbarellos@genexus.com",
            attachLog: true
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}