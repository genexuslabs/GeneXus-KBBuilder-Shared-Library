param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $msbuildExePath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $msbuildScript,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $localGXPath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $localKBPath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $environmentName,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $generator,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $dataSource,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $dbName,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $dbServer,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $dbPort,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $dbServerUsername
)
$ErrorActionPreference="Stop"
Write-Output((Get-Date -Format G) + " INFO input msbuildExePath::$msbuildExePath")
Write-Output((Get-Date -Format G) + " INFO input msbuildScript::$msbuildScript")
Write-Output((Get-Date -Format G) + " INFO input localGXPath::$localGXPath")
Write-Output((Get-Date -Format G) + " INFO input localKBPath::$localKBPath")
Write-Output((Get-Date -Format G) + " INFO input environmentName::$environmentName")
Write-Output((Get-Date -Format G) + " INFO input generator::$generator")
Write-Output((Get-Date -Format G) + " INFO input dataSource::$dataSource")
Write-Output((Get-Date -Format G) + " INFO input dbName::$dbName")
Write-Output((Get-Date -Format G) + " INFO input dbServer::$dbServer")
Write-Output((Get-Date -Format G) + " INFO input dbPort::$dbPort")
Write-Output((Get-Date -Format G) + " INFO input dbServerUsername::$dbServerUsername"
#
$target = " /t:ConfigureDataStore"
$msbuildGenArgs = " /p:GX_PROGRAM_DIR=`"$localGXPath`""
$msbuildGenArgs += " /p:localKbPath=`"$localKBPath`""
$msbuildGenArgs += " /p:EnvironmentName=`"$environmentName`""
$msbuildGenArgs += " /p:Generator=`"$generator`""
$msbuildGenArgs += " /p:DataSource=`"$dataSource`""
$msbuildGenArgs += " /p:DB_Name=`"$dbName`""
$msbuildGenArgs += " /p:DB_Server=`"$dbServer`""
$msbuildGenArgs += " /p:DB_Port=`"$dbPort`""
$msbuildGenArgs += " /p:DB_User=`"$dbServerUsername`""
if ($null -eq $ENV:dbServerPassword) {
    Write-Error "Environment Variable 'dbServerPassword' is required"
}
$msbuildGenArgs += " /p:DB_Pwd=`"$ENV:dbServerPassword`""
Invoke-Command -ScriptBlock {$msbuildExePath $msbuildScript $target $msbuildGenArgs}
#