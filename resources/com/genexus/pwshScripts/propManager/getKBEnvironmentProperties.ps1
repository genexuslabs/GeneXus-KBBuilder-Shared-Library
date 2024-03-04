param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $msbuildExePath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $msbuildScript,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $gxBasePath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $localKBPath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $environmentName,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $propertiesFilePath
)
$ErrorActionPreference="Stop"
Write-Output((Get-Date -Format G) + " INFO input msbuildExePath::$msbuildExePath")
Write-Output((Get-Date -Format G) + " INFO input msbuildScript::$msbuildScript")
Write-Output((Get-Date -Format G) + " INFO input gxBasePath::$gxBasePath")
Write-Output((Get-Date -Format G) + " INFO input localKBPath::$localKBPath")
Write-Output((Get-Date -Format G) + " INFO input environmentName::$environmentName")
Write-Output((Get-Date -Format G) + " INFO input propertiesFilePath::$propertiesFilePath")
#
$target = " /t:ReadCommiteableProperties"
$msbuildGenArgs = " /p:GX_PROGRAM_DIR=`"$gxBasePath`""
$msbuildGenArgs += " /p:localKbPath=`"$localKBPath`""
$msbuildGenArgs += " /p:EnvironmentName=`"$environmentName`""
$msbuildGenArgs += " /p:PropFileAbsolutePath=`"$propertiesFilePath`""
Invoke-Command -ScriptBlock {$msbuildExePath $msbuildScript $target $msbuildGenArgs}
#