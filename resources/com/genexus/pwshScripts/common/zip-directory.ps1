param 
(
	[ValidateNotNullOrEmpty()]
    [string] $gxPIABaseLocation,
	[ValidateNotNullOrEmpty()]
    [string] $ZipLocation,
	[ValidateNotNullOrEmpty()]
    [string] $Dir,
	[ValidateNotNullOrEmpty()]
    [string] $7zipPath = "C:\Program Files\7-Zip\7z.exe"
)
$ErrorActionPreference = "Stop"
$ProgressPreference = 'SilentlyContinue'

#
try {
    Write-Output "$(Get-Date -Format G) [INFO] Starting $PSCommandPath"
    $7zipPath = $path.RemoteMachinePaths.zipper
    Write-Output((Get-Date -Format G) + " READ $7zipPath")
    Write-Output((Get-Date -Format G) + " INFO zipping... (to: " + $ZipLocation + " )")
    Invoke-Command -ScriptBlock {& $7zipPath a $ZipLocation $Dir}
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