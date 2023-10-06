param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $localGXPath
)
$ErrorActionPreference="Stop"
#
$dllPath = "$localGXPath\Artech.Common.Controls.dll"
if(Test-Path -Path $dllPath) {
    Write-Output("Using GeneXus Installation version " + (Get-Item "$dllPath").VersionInfo.ProductVersion)
}
else {
    Write-Output("--------")
}
#