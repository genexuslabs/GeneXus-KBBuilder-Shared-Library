param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $localGXPath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $localAndroidSDKPath
)
$ErrorActionPreference="Stop"
#
$gxExeConfigPath = "$localGXPath\GeneXus.exe.config"
if(Test-Path -Path $gxExeConfigPath) {
    [xml]$gxExeConfig = Get-Content $gxExeConfigPath 
    #$gxExeConfig
	$keyValueTags = $gxExeConfig.configuration["appSettings"]
    foreach($tag in $keyValueTags) {
        if($tag.key -eq "ProgramDataPath") {
            $programDataPath = $tag.value
            if(Test-Path -Path $programDataPath) {
                Write-Output((Get-Date -Format G) + " INFO remove programDataPath: $programDataPath") 
                Remove-Item -Path "$programDataPath" -Recurse -ErrorAction Stop
            }
        }
        if($tag.key -eq "UserAppDataPath") {
            $userDataPath = $tag.value
            if(Test-Path -Path $userDataPath) {
                Write-Output((Get-Date -Format G) + " INFO remove userDataPath: $userDataPath") 
                Remove-Item -Path "$userDataPath" -Recurse -ErrorAction Stop
            }
        }
    }
}
if(Test-Path -Path $localGXPath) {
    Write-Output((Get-Date -Format G) + " INFO remove localGXPath: $localGXPath") 
    Remove-Item -Path "$localGXPath" -Recurse -ErrorAction Stop
}
if(Test-Path -Path $localAndroidSDKPath) {
    Write-Output((Get-Date -Format G) + " INFO remove localAndroidSDKPath: $localAndroidSDKPath") 
    Remove-Item -Path "$localAndroidSDKPath" -Recurse -ErrorAction Stop
}
#