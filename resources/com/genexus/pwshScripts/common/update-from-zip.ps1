<#
.SYNOPSIS
Updates files at the target path by downloading and extracting a ZIP archive from a source URI.

.DESCRIPTION
This script is used for updating files at the target path by fetching a ZIP archive from the source URI, extracting its contents, and copying them to the target path. Optionally, you can specify a source folder within the ZIP archive to copy.

.PARAMETER SourceUri
Specifies the URI of the ZIP archive to download and extract. This parameter is mandatory.

.PARAMETER SourceFolder
Specifies the name of the folder within the ZIP archive to copy. Optional; if not provided, the entire contents of the ZIP archive will be copied.

.PARAMETER TargetPath
Specifies the path where the extracted files will be copied. This parameter is mandatory.

.EXAMPLE
# Update files in the target path from a ZIP archive at the source URI.
.\Update-From-Zip.ps1 -SourceUri "https://example.com/archive.zip" -TargetPath "C:\Program Files\MyApp"

.EXAMPLE
# Update files from a specific folder within the ZIP archive.
.\Update-From-Zip.ps1 -SourceUri "https://example.com/archive.zip" -SourceFolder "FolderName" -TargetPath "C:\Program Files\MyApp"

#>

[CmdletBinding()]
param (
	[ValidateNotNullOrEmpty()]
	[string] $SourceUri,
	[ValidateNotNullOrEmpty()]
	[string] $TargetPath,
	[string] $SourceFolder
)

$ErrorActionPreference = "Stop"

function Get-ZipFileName-FromUri {
	param (
		[string] $Url
	)

	if ($Url.IndexOf('?') -lt 0) {
		if ($Url.EndsWith(".zip")) { 
			return [System.IO.Path]::GetFileName($SourceUri)
		} else {
			return [System.IO.Path]::GetFileName($SourceUri) + ".zip"
		}
	}

	# detect variable names and their values in the query string
	# and look for the 'id' parameter
	$query = $Url.Substring($Url.IndexOf('?') + 1)
	foreach ($q in ($query -split '&')) {
		$kv = $($q + '=') -split '='
		$name = [uri]::UnescapeDataString($kv[0]).Trim()
		if ($name -eq "id") {
			return [uri]::UnescapeDataString($kv[1]) + ".zip"
		}
	}

	return [System.IO.Path]::GetFileNameWithoutExtension([System.IO.Path]::GetTempFileName()) + ".zip"
}

function Get-ZipFile {
	param (
		[string] $SourceUri
	)

	$zipFileName = Get-ZipFileName-FromUri($SourceUri)
	$zipFilePath = Join-Path -Path $env:Temp -ChildPath $zipFileName
	if (Test-Path -Path $zipFilePath -PathType leaf) {
		Write-Host("Taking zip file already present on ${zipFilePath}")
		return $zipFilePath
	}

	Write-Host("Downloading: ${SourceUri} to ${zipFilePath}")
	Invoke-WebRequest -Uri $SourceUri -OutFile $zipFilePath
	return $zipFilePath
}

function Expand-Zip {
	param (
		[string] $ZipFilePath
	)

	$expandedZipFolderName = [System.IO.Path]::GetFileNameWithoutExtension($ZipFilePath)
	$expandedZipPath = Join-Path $env:Temp $expandedZipFolderName

	if (!(Test-Path $expandedZipPath)) {
		Expand-Archive -Path $ZipFilePath -DestinationPath $expandedZipPath -Force
	}
	else {
		Write-Host("Taking expanded content already present on ${expandedZipPath}")
	}

	return $expandedZipPath
}

function Copy-Files {
	param (
		[string] $source,
		[string] $target
	)

	robocopy $source $Target /E /MT /NJH /NJS /NFL /NDL
	if ($LASTEXITCODE -gt 8) {
		Write-Error "robocopy encountered an error. Exit code: $LASTEXITCODE"
	}
	else {
		$LASTEXITCODE = 0
	}
}

try {
	Write-Output "Updating ${TargetPath} from content at ${SourceUri}"
	$zipFilePath = Get-ZipFile -SourceUri $SourceUri
	$zipContent = Expand-Zip -ZipFilePath $zipFilePath
	$sourcePath = Join-Path $zipContent $SourceFolder
	Copy-Files -source $sourcePath -target $TargetPath
	Write-Output " [INFO] delete:${zipFilePath}"
	Remove-Item -Path $zipFilePath -Force -Recurse
	Remove-Item -Path $zipFilePath.Replace('.zip', '') -Force -Recurse
}
catch {
	Write-Output "Error Updating from '${SourceUri}':"
	throw $_
}
