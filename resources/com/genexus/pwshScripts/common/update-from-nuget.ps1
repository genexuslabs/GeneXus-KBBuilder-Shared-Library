param (
    [Parameter(Mandatory=$true)]
    [string]$REMOTE_NUGET_HOST,
    [Parameter(Mandatory=$true)]
    [string]$PackageId,
    [Parameter(Mandatory=$true)]
    [string]$PackageVersion,
    [Parameter(Mandatory=$true)]
    [string]$DeployTarget
)
# Define constants
$DepsTmpDir = Join-Path -Path $PSScriptRoot -ChildPath "\.tmp\"
$DepsProjectName = "DepsProject"
$DepsProjectDir = Join-Path -Path $DepsTmpDir -ChildPath $DepsProjectName
$DepsProjectPath = Join-Path -Path $DepsProjectDir -ChildPath "$DepsProjectName.csproj"
$LOCAL_NUGET_CACHE = Join-Path -Path $DepsTmpDir -ChildPath "DEPS_NUGET_CACHE"
$DepsProjectNuGetConfig = Join-Path -Path $DepsTmpDir -ChildPath "NuGet.Config"

# Create project
if (Test-Path -Path $DepsTmpDir) { Remove-Item -Path $DepsTmpDir -Recurse -Force }
New-Item -ItemType Directory -Path $DepsProjectDir -Force | Out-Null
dotnet new classlib -n $DepsProjectName -o $DepsProjectDir
if (Test-Path -Path $LOCAL_NUGET_CACHE) { Remove-Item -Path $LOCAL_NUGET_CACHE -Recurse -Force }
New-Item -ItemType Directory -Path $LOCAL_NUGET_CACHE -Force | Out-Null

# Write NuGet.Config
$nugetConfigContent = @"
<?xml version="1.0" encoding="utf-8"?>
<configuration>
    <packageSources>
        <add key="nexus_proxy_from_azure" value="$REMOTE_NUGET_HOST" protocolVersion="3" />
    </packageSources>
    <config>
        <add key="globalPackagesFolder" value="$LOCAL_NUGET_CACHE" />
    </config>
</configuration>
"@

Set-Content -Path $DepsProjectNuGetConfig -Value $nugetConfigContent

try {
    dotnet add $DepsProjectPath package $PackageId --no-restore --version $PackageVersion
} catch {
    Write-Host "Failed to add package $PackageId version $PackageVersion"
}
# Restore the project
dotnet restore $DepsProjectPath
$localPackagesPath = Join-Path -Path $LOCAL_NUGET_CACHE -ChildPath "$($PackageId.ToLower())\$PackageVersion\"
$Package = Get-ChildItem -Path "$localPackagesPath\" -Filter ".zip"
Write-Output "$(Get-Date -Format G) [DEBUG] Read downloaded package zip: $Package.Name"
Invoke-Command -ScriptBlock {& "$PSScriptRoot\update-from-zip.ps1" (Join-Path -Path $localPackagesPath -ChildPath $Package.Name) $DeployTarget}