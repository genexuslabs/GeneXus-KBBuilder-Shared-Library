param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $S3BucketName,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $FileSource
)
$ErrorActionPreference = "Stop"
$ProgressPreference = 'SilentlyContinue'

try {
    Write-Output "$(Get-Date -Format G) [INFO] Starting $PSCommandPath"
    Invoke-Command -ScriptBlock {& aws s3 cp $FileSource s3://$S3BucketName}
    #    
    Write-Output "$(Get-Date -Format G) [INFO] -----End $PSCommandPath"
	exit 0
#
} catch {
	Write-Output "$(Get-Date -Format G) [ERROR] $PSCommandPath Fail"
	Write-Output "$(Get-Date -Format G) [ERROR] $PSCommandPath failed with Error:" + $_
	exit 1
}
#