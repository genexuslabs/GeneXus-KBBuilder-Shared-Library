param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $gxBasePath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $protectionServerType,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $protectionServerName,
    [Parameter(Mandatory=$True)]
    [string] $protectionServerUser
)
$ErrorActionPreference="Stop"
#
$protectionIniFilePath = "$gxBasePath\Protect.ini"
if(Test-Path -Path $protectionIniFilePath) {
    Remove-Item -Path $protectionIniFilePath
}
$null = New-Item -Path $protectionIniFilePath
Add-Content -Path $protectionIniFilePath -Value "[Settings]"
Add-Content -Path $protectionIniFilePath -Value "ProtType=$protectionServerType"
Add-Content -Path $protectionIniFilePath -Value "ServerName=$protectionServerName"
if($protectionServerUser) 
{	if ($null -eq $ENV:protectionServerPass) {
		Write-Error "Environment Variable 'protectionServerPass' is required"
	}
    Add-Content -Path $protectionIniFilePath -Value "ProtUser=$protectionServerUser"
    Add-Content -Path $protectionIniFilePath -Value "ProtPassword=$ENV:protectionServerPass"
}
#