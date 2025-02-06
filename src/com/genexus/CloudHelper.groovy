package com.genexus

/**
 * Configure Windows AWS CLI
 *
 * @param args A map containing the following parameters:
 *   - awsRegion: Amazon region (e.g., us-east-1)
 *   - awsOutput: Amazon output format (e.g., json)
 *   - awsCredentialsId: AWS AccessKey and SecretAccessKey stored as a Jenkins credential of type username/password
 */
void awsConfigure(Map args = [:]) {
    try{
        echo "[INFO] Configure AWS CLI profile: ${args.awsCredentialsId}"
        withCredentials([
            usernamePassword(
                credentialsId: args.awsCredentialsId,
                passwordVariable: 'awsPWD',
                usernameVariable: 'awsUSER')
        ]) {
            powershell script: "aws configure set aws_access_key_id ${awsUSER} --profile ${args.awsCredentialsId} "
            powershell script: "aws configure set aws_secret_access_key \$ENV:awsPWD --profile ${args.awsCredentialsId} " 
            powershell script: "aws configure set region ${args.awsRegion} --profile ${args.awsCredentialsId} "
            powershell script: "aws configure set output ${args.awsOutput} --profile ${args.awsCredentialsId}"
        }
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

/**
 * Uploads an artifact to an AWS S3 bucket
 *
 * @param args A map containing the following parameters:
 *   - artifactFullPath: The full path to the artifact to be uploaded
 *   - awsS3Bucket: The name of the AWS S3 bucket
 */
void awsUploadToS3Bucket(Map args = [:]) {
    try{
        echo "[INFO] Starting upload artifact to S3"
        echo "[DEBUG] Artifact full path:\"${args.artifactFullPath}\""
        echo "[DEBUG] AWS Bucket full destination s3://${args.awsS3Bucket}"
        powershell script: """
            if(-not ${args.awsS3Bucket}.EndsWith("/")) {
                ${args.awsS3Bucket} += "/"
            }
            \$fileNameExt = [System.IO.Path]::GetFileName("${args.artifactFullPath}")
            Write-Output "$(Get-Date -Format G) [INFO] Uploading package: \$fileNameExt to ${args.awsS3Bucket}"
            & aws s3 cp "${args.artifactFullPath}" s3://${args.awsS3Bucket}\$fileNameExt
        """
    } catch (error) {
        currentBuild.result = 'FAILURE'
        throw error
    }
}

return this
