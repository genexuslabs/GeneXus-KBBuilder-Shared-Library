/**
 * Job: sendNotifications >> This method triggers notifications based on the specified notification type.
 *
 * Parameters:
 * - args: A map containing the following parameters:
 *   - notificationType: The type of notification to be triggered (email, googleChatRoom).
 *   - TODO
 *

 */

import com.genexus.NotificationHelper

def call(Map args = [:]) {
    try {
        def nHelper = new NotificationHelper()
        switch(args.notificationType) {
            case 'email':
                nHelper.sendEmail(args)
            break
            case 'googleChatRoom':
                nHelper.sendGoogleChatNotification(args)
            break
            default:
                currentBuild.result = 'FAILURE'
                error "[ERROR] sendNotifications.groovy - notification type with name ${args.notificationType} not implemented yet"
            break
        }
    } catch (def error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}