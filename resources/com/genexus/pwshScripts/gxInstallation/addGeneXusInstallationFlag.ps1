param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $gxBasePath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $flagName
)
$ErrorActionPreference="Stop"
#
$flagPath = "$gxBasePath\$flagName.flag"
if(Test-Path -Path $flagPath) {Remove-Item -Path $flagPath}
Write-Output((Get-Date -Format G) + " [INFO] Success create file $flagName.flag in $gxBasePath")
$null = New-Item -Path $flagPath
#