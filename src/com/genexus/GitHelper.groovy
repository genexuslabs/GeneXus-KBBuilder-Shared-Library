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

String getAppToken(String githubAppCredentialsId) {
    try {
        withCredentials([
            usernamePassword(credentialsId: "${githubAppCredentialsId}",
                usernameVariable: 'githubClientId',
                passwordVariable: 'githubPrivateKey')
        ]) {
            def jwt = powershell(script: """
                \$ErrorActionPreference = "Stop"

                \$header = @{ alg = 'RS256'; typ = 'JWT' } | ConvertTo-Json -Compress
                \$iat = [int][double]::Parse((Get-Date -UFormat %s))
                \$exp = \$iat + 600
                \$payload = @{ iat = \$iat; exp = \$exp; iss = "\$env:githubClientId" } | ConvertTo-Json -Compress

                \$header64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes(\$header)) -replace '=', '' -replace '\\+', '-' -replace '/', '_'
                \$payload64 = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes(\$payload)) -replace '=', '' -replace '\\+', '-' -replace '/', '_'
                \$jwt = "\$header64.\$payload64"

                \$signedJwt = & openssl dgst -sha256 -sign <(echo "\$env:githubPrivateKey") <<< \$jwt | openssl base64 | tr -d '=' | tr '+/' '-_' | tr -d '\\n'
                return "\$jwt.\$signedJwt"
            """, returnStdout: true).trim()

            // Obtener el token de la aplicación GitHub
            githubAppToken = powershell(script: """
                \$url = 'https://api.github.com/app/installations/:installation_id/access_tokens' # Reemplaza con el ID real
                \$response = Invoke-RestMethod -Uri \$url -Method Post -Headers @{
                    'Authorization' = "Bearer \$jwt"
                    'Accept' = 'application/vnd.github.v3+json'
                }
                return \$response.token
            """, returnStdout: true).trim()
        }

        return githubAppToken
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

void dispatchBuiltinModuleToGeneXusDependencySync(Map args = [:]) {
    try {
        // Validación de parámetros
        if (!args.githubAppToken) {
            throw new IllegalArgumentException("Missing 'githubAppToken'")
        }
        if (!args.packageModuleName) {
            throw new IllegalArgumentException("Missing 'packageModuleName'")
        }
        if (!args.moduleVersion) {
            throw new IllegalArgumentException("Missing 'moduleVersion'")
        }
        if (!args.dispathBranch) {
            throw new IllegalArgumentException("Missing 'dispathBranch'")
        }

        echo " [DEBUG] args.githubAppToken::${args.githubAppToken}"
        echo " [DEBUG] args.packageModuleName::${args.packageModuleName}"
        echo " [DEBUG] args.moduleVersion::${args.moduleVersion}"
        echo " [DEBUG] args.dispathBranch::${args.dispathBranch}"

        // Ejecutar el script de PowerShell
        powershell """
            \$ErrorActionPreference = "Stop"

            \$curlCommand = @"
            curl -X POST -H "Accept: application/vnd.github.v3+json" -H "Authorization: token ${args.githubAppToken}" `
            https://api.github.com/repos/VY-GEN032-KG/genexus-dependency-sync/actions/workflows/commit-to-git-manual.yml/dispatches `
            -d "{\\"ref\\":\\"master\\",\\"inputs\\":{\\"COMPONENT_NAME\\":\\"${args.packageModuleName}\\",\\"VERSION\\":\\"${args.moduleVersion}\\",\\"BRANCH\\":\\"${args.dispathBranch}\\",\\"COMMITTER\\":\\"Jenkins Builder\\",\\"COMMIT_MESSAGE\\":\\"Bump '${args.packageModuleName}' version to ${args.moduleVersion} #JenkinsIntegration\\",\\"PACKAGE_NAMES\\":\\"${args.packageModuleName}\\"}}"
            "@

            \$curlResponseOutput = Invoke-Expression \$curlCommand

            \$curlResponseStatusRegex = '(?<=\\"status\\":\\s*)\\"\\d+\\"'
            if (\$curlResponseOutput -match \$curlResponseStatusRegex) {
                \$curlResponseStatusCode = \$matches[0]
            }

            if (\$curlResponseStatusCode -ne '') {
                throw "Error dispatching to GeneXus Dependency Sync: \$curlResponseStatusCode"
            }
        """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this