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

/**
 * Commits changes to a specified Git repository using provided credentials and commit details.
 *
 * This method allows you to commit and push changes to a GitHub repository using an authenticated
 * URL with a GitHub App token. It configures Git with the specified user email and name, stages 
 * all changes, commits them with the provided commit message, and pushes the changes to the specified 
 * branch of the repository.
 *
 * @param args A map containing the following keys:
 *             - gitRepositoryUrl: The URL of the Git repository.
 *             - gitEmail: The email address to use for the Git commit.
 *             - gitAppUserId: The user ID of the GitHub App.
 *             - gitAppNameSlug: The slug of the GitHub App name.
 *             - gitCommitMessage: The commit message to use.
 *             - gitBranch: The branch to which the changes should be pushed.
 *             - gitCredentialsId: The credentials ID stored in Jenkins for authentication.
 *
 * The method uses credentials stored in Jenkins to authenticate the push operation.
 *
 * @throws Exception if any error occurs during the execution of the Git commands.
 */
void commitUsingGitHubAppBuilderToken(Map args = [:]) {
    try {
        withCredentials([usernamePassword(credentialsId: "${args.gitCredentialsId}", usernameVariable: 'GITHUB_APP', passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
            def authenticatedUrl = args.gitRepositoryUrl.replace("https://", "https://x-access-token:${GITHUB_ACCESS_TOKEN}@")
            powershell script: """
                git config --global credential.helper ""
                git config user.email \"${args.gitEmail}\"
                git config user.name \"${args.gitAppUserId}+${args.gitAppNameSlug}[bot]\"
                git remote set-url origin '${authenticatedUrl}'
                git pull origin ${args.gitBranch}
                git add .
                git commit -m "${args.gitCommitMessage}"
                git push origin ${args.gitBranch}
                git remote set-url origin '${args.gitRepositoryUrl}'
            """
        }
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
            """
                //git push --set-upstream origin ${reorgPublishTypeDefinition.branch}
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void dispatchToGeneXusDependencySync(Map args = [:]) {
    try {
        withCredentials([
            usernamePassword(credentialsId: "${args.dispatchGitHubAppCredentialsId}",
                usernameVariable: 'githubClientId',
                passwordVariable: 'githubAccessToken'
        )]) {
            bat """
                curl -X POST ^
                -H \"Accept: application/vnd.github+json\" ^
                -H \"Authorization: Bearer ${githubAccessToken}\" ^
                https://api.github.com/repos/${args.dispatchRepoOrganization}/${args.dispatchRepoName}/actions/workflows/${args.dispatchWorkflowName}/dispatches ^
                -d "{ \\"ref\\": \\"${args.dispatchRepoBranch}\\", \\"inputs\\": { \\"COMPONENT_NAME\\": \\"${args.componentName}\\", \\"BRANCH\\": \\"${args.componentBranch}\\", \\"PACKAGE_NAMES\\":\\"${args.packageNames}\\", \\"VERSION\\": \\"${args.componentVersion}\\", \\"COMMIT_MESSAGE\\": \\"${args.commitMessage}\\", \\"COMMITTER\\": \\"${args.committer}\\" } }"
            """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void dispatchToReusableUpdateImageNumber(Map args = [:]) {
    try {
        withCredentials([
            usernamePassword(credentialsId: "${args.dispatchGitHubAppCredentialsId}",
                usernameVariable: 'githubClientId',
                passwordVariable: 'githubAccessToken'
        )]) {
            bat """
                    curl -X POST ^
                    -H "Accept: application/vnd.github+json" ^
                    -H "Authorization: Bearer ${githubAccessToken}" ^
                    https://api.github.com/repos/${args.dispatchRepoOrganization}/${args.dispatchRepoName}/actions/workflows/${args.dispatchWorkflowName}/dispatches ^
                    -d \"{ \\"ref\\": \\"${args.dispatchRepoBranch}\\", \\"inputs\\": { \\"DEPLOY_REPOSITORY\\": \\"${args.deployRepository}\\", \\"DEPLOY_ENV\\": \\"${args.deployEnv}\\", \\"BRANCH\\": \\"${args.branch}\\", \\"COMMITTER\\": \\"${args.committer}\\", \\"SERVICES\\": \\"${args.services}\\", \\"IMAGE_VERSION\\": \\"${args.imageVersion}\\" } }\"
                """
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
