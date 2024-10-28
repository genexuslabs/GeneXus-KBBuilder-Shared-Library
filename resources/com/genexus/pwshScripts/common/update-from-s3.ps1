<#
.SYNOPSIS
Updates files at the target path by copying and extracting a ZIP archive from an AWS S3 bucket.

.DESCRIPTION
This script updates files at the target path by fetching a ZIP archive from an AWS S3 bucket, extracting its contents, and copying them to the target path. Optionally, you can specify a source folder within the ZIP archive to copy.

.PARAMETER SourceUri
Specifies the S3 URI of the ZIP archive to download and extract. This parameter is mandatory (e.g., s3://bucket-name/archive.zip).

.PARAMETER SourceFolder
Specifies the name of the folder within the ZIP archive to copy. Optional; if not provided, the entire contents of the ZIP archive will be copied.

.PARAMETER TargetPath
Specifies the path where the extracted files will be copied. This parameter is mandatory.

.EXAMPLE
# Update files in the target path from a ZIP archive in an S3 bucket.
.\Update-From-S3-Zip.ps1 -SourceUri "s3://example-bucket/archive.zip" -TargetPath "C:\Program Files\MyApp"

.EXAMPLE
# Update files from a specific folder within the ZIP archive.
.\Update-From-S3-Zip.ps1 -SourceUri "s3://example-bucket/archive.zip" -SourceFolder "FolderName" -TargetPath "C:\Program Files\MyApp"
#>

[CmdletBinding()]
param (
    [Parameter(Mandatory=$True)]
    [ValidateNotNullOrEmpty()]
    [string] $SourceUri,
    [Parameter(Mandatory=$True)]
    [ValidateNotNullOrEmpty()]
    [string] $TargetPath,
    [string] $SourceFolder
)

$ErrorActionPreference = "Stop"

function Get-ZipFile {
    param (
        [string] $SourceUri
    )

    # Define the temporary ZIP file path
    $zipFilePath = Join-Path -Path $env:Temp -ChildPath ([System.IO.Path]::GetFileName($SourceUri))

    # Check if the ZIP file already exists
    if (Test-Path -Path $zipFilePath -PathType leaf) {
        Write-Host("Taking zip file already present on ${zipFilePath}")
        return $zipFilePath
    }

    # Use AWS CLI to copy the ZIP file from S3
    Write-Host("Copying from: ${SourceUri} to ${zipFilePath}")
    aws s3 cp $SourceUri $zipFilePath --quiet
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

    robocopy $source $target /E /MT /NJH /NJS /NFL /NDL
    if ($LASTEXITCODE -gt 8) {
        Write-Error "robocopy encountered an error. Exit code: $LASTEXITCODE"
    }
}

try {
    Write-Output "Updating ${TargetPath} from content at ${SourceUri}"
    $zipFilePath = Get-ZipFile -SourceUri $SourceUri
    $zipContent = Expand-Zip -ZipFilePath $zipFilePath
    $sourcePath = Join-Path $zipContent $SourceFolder

    # If SourceFolder is not provided, set sourcePath to the expanded zip path
    if (-not [string]::IsNullOrEmpty($SourceFolder)) {
        $sourcePath = Join-Path $zipContent $SourceFolder
    } else {
        $sourcePath = $zipContent
    }

    Copy-Files -source $sourcePath -target $TargetPath
    Write-Output " [INFO] delete:${zipFilePath}"
    Remove-Item -Path $zipFilePath -Force -Recurse
    Remove-Item -Path $zipFilePath.Replace('.zip', '') -Force -Recurse
}
catch {
    Write-Output "Error Updating from '${SourceUri}':"
    throw $_
}