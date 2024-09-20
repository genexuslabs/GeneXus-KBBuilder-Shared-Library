import groovy.text.StreamingTemplateEngine
import com.genexus.NotificationHelper
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



/*
 * Job: sendEmailNotification
 *
 * Description:
 * This job sends an email notification with the build result of a Jenkins job. It uses a template to format 
 * the email content and includes various details about the build, such as job name, URL, timestamp, duration, 
 * and change log.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - notifyTo: The email address to send the notification to.
 *
 * Workflow Steps:
 * 1. Initialize the NotificationHelper and obtain the change log set.
 * 2. Determine the build result and set the corresponding icon, color, and result string.
 * 3. Define the email template and populate it with build and job details.
 * 4. Send the email using the `emailext` plugin, attaching the build log.
 * 5. Handle any exceptions by setting the build result to 'FAILURE' and rethrowing the error.
 *
 */
def call(Map args = [:]) {
    try {
        def engine2 = new NotificationHelper()
        def file = new GeneXusHelper()
        def changeLogSet = engine2.getChangeLogSet()
        def gxVersion = file.getGeneXusInstallationVersion(args.gxBasePath)
        def icon
        String template
        String templateName
        String buildColor
        String buildResult
        String jobName = (env.JOB_NAME).replace(env.JOB_BASE_NAME, '')
        String jobFullDisplayName = currentBuild.fullDisplayName
        def splitJobDisplayName = currentBuild.fullDisplayName.split(' » ')
        def jobDisplayName = ''
        if (splitJobDisplayName[-1].toLowerCase().contains('build')) {
            jobDisplayName = "${jobFullDisplayName.replace(splitJobDisplayName[splitJobDisplayName.length - 1], '')}"
        }else{
            //Replaces anything that matches the pattern /#\d+/ with an empty string and trims the string
            //For example in this string: BETA » Modules » super_apps_module #20 the result is: BETA » Modules » super_apps_module
            jobDisplayName = jobFullDisplayName.replaceAll('/#\\d+/', '').trim()
        }
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

        templateName = "com/genexus/notificationTemplates/emailBuildResult.html.groovy"
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
            to: "${args.notifyTo} , jalbarellos@genexus.com",
            attachLog: true

    } catch (def error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}