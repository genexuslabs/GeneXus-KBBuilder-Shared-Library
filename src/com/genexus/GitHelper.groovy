package com.genexus

/**
 * This methods clone git repo to workspace
 * @param gitUrl, gitBranch, gitCredentialsId
 */
void cloneRepository(String gitUrl, String gitBranch, String gitCredentialsId) {
    try {
        git url: gitUrl, 
            branch: gitBranch, 
            credentialsId: gitCredentialsId, 
            changelog: false, 
            poll: false
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void publishReorganizationScript(LinkedHashMap reorgPublishTypeDefinition, String reorgFullPath) {
    try {
        dir("PublishReorgRepo") {
            git url: reorgPublishTypeDefinition.repository, 
                branch: reorgPublishTypeDefinition.branch, 
                credentialsId: reorgPublishTypeDefinition.credentialsId, 
                changelog: false, 
                poll: false
                
            powershell script: """
                \$fileName = (Split-Path -Path \"${reorgFullPath}\" -Leaf)
                Write-Output(\" INFO file::\$fileName\")
                Write-Output(\" INFO reorgFullPath::${reorgFullPath}\")
                Write-Output(\" INFO destination::${WORKSPACE}\\PublishReorgRepo\\${reorgPublishTypeDefinition.directory}\\\$fileName\")
                Copy-Item -Path \"${reorgFullPath}\" \"${WORKSPACE}\\PublishReorgRepo\\${reorgPublishTypeDefinition.directory}\\\$fileName\"

                git add .
                git status
                git commit -m \"Jenkins pipeline push ReorganizationScript ${env.BUILD_NUMBER}\"
                git push --set-upstream origin ${reorgPublishTypeDefinition.branch}
            """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void dispatchBuiltinModuleToGeneXusDependencySync(Map args = [:]) {
    try {
        withCredentials([
            usernamePassword(credentialsId: "${args.githubAppCredentialsId}",
                usernameVariable: 'githubClientId',
                passwordVariable: 'githubAccessToken'
        )]) {
            bat """
                curl -X POST ^
                -H \"Accept: application/vnd.github+json\" ^
                -H \"Authorization: Bearer ${githubAccessToken}\" ^
                https://api.github.com/repos/${args.gitOrganization}/${args.gitRepoName}/actions/workflows/${args.gitWorkflowName}/dispatches ^
                -d "{ \\"ref\\": \\"${args.gitBranch}\\", \\"inputs\\": { \\"COMPONENT_NAME\\": \\"${args.packageModuleName}\\", \\"BRANCH\\": \\"${args.dispathBranch}\\", \\"VERSION\\": \\"${args.moduleVersion}\\", \\"COMMIT_MESSAGE\\": \\"${args.commitMessage}\\", \\"COMMITTER\\": \\"${args.committer}\\" } }"
            """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this