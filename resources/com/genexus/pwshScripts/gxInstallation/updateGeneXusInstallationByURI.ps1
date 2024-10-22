param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $gxBasePath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $genexusURI,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $localAndroidSDKPath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [boolean] $runGXInstall
)
$ErrorActionPreference="Stop"
#
$flag = $true
$lastURIFilePath = "$gxBasePath\lastURI.txt"
if(Test-Path -Path "$gxBasePath") {
    if(Test-Path -Path "$lastURIFilePath") {
        $lastURI = Get-Content $lastURIFilePath
        if ($lastURI -eq $genexusURI) {
            $flag = $false
        }
    }
}
if ($flag) {
	Invoke-Command -ScriptBlock {& "$PSScriptRoot\deleteGeneXusInstallation.ps1" $gxBasePath $localAndroidSDKPath}
    $null = New-Item -Path "$gxBasePath" -ItemType directory
    $tempDir = [System.IO.Path]::GetTempPath()
    [string] $guid = [System.Guid]::NewGuid()
    $blZip = Join-Path -Path $tempDir -ChildPath $guid".zip"
    Write-Output((Get-Date -Format G) + " INFO downloading: $genexusURI to: $blZip")
    if($genexusURI.Contains("s3://")) {
        & aws s3 cp $genexusURI $blZip
    } else {
        $clnt = new-object System.Net.WebClient
        $clnt.DownloadFile("$genexusURI", $blZip)
    }

    Write-Output((Get-Date -Format G) + " INFO unziping gx-bl")
    Expand-Archive -LiteralPath $blZip -DestinationPath "$gxBasePath" -Force
    #Mover sub carpeta al raiz si existe serchgxfile
    $serchGxFile="Artech.Common.dll"
    $auxDirs = Get-ChildItem -Path "$gxBasePath"  | Where-Object { $_.Name.StartsWith($serchGxFile)}
    if ($auxDirs -eq $null) {
        $dirs = Get-ChildItem -Directory -Path "$gxBasePath" 
        foreach($i in $dirs)
        {
            $auxDirs = Get-ChildItem -Path "$gxBasePath\$i" | Where-Object { $_.Name.StartsWith($serchGxFile)}
            #MOVER  Todo de $i a current 
            if ($auxDirs -ne $null) {
                Get-Item -Path "$gxBasePath\$i\*" | Move-Item -destination "$gxBasePath"
                Remove-Item -Path "$gxBasePath\$i" 
                break;
            }
        }
    }
    Write-Output((Get-Date -Format G) + " Remove genexus.zip")
    Remove-Item -Path $blZip -Recurse

    $gxExeConfigPath = "$gxBasePath\GeneXus.exe.config"
    $relativeProgramDataPath = "$gxBasePath\..\ProgramData"
    if(!(Test-Path -Path $relativeProgramDataPath)) {$null = New-Item -Path $relativeProgramDataPath -ItemType Directory}
    Set-Location -Path $relativeProgramDataPath
    $programDataPath = (Get-Location).Path
    $relativeUserDataPath = "$gxBasePath\..\UserData"
    if(!(Test-Path -Path $relativeUserDataPath)) {$null = New-Item -Path $relativeUserDataPath -ItemType Directory}
    Set-Location -Path $relativeUserDataPath
    $userDataPath = (Get-Location).Path
    [xml]$xml = Get-Content $gxExeConfigPath
    Write-Output((Get-Date -Format G) + " INFO set ProgramDataPath in genexus.exe.config")
    $newEl_a = $xml.CreateElement("add");                               # Create a new Element 
    $nameAtt1_a = $xml.CreateAttribute("key");                         # Create a new attribute “key” 
    $nameAtt1_a.psbase.value = "ProgramDataPath";                    # Set the value of “key” attribute 
    $newEl_a.SetAttributeNode($nameAtt1_a);                              # Attach the “key” attribute 
    $nameAtt2_a = $xml.CreateAttribute("value");                       # Create “value” attribute  
    $nameAtt2_a.psbase.value = $programDataPath;                       # Set the value of “value” attribute 
    $newEl_a.SetAttributeNode($nameAtt2_a);                               # Attach the “value” attribute 
    $xml.configuration["appSettings"].AppendChild($newEl_a);    # Add the newly created element to the right position

    Write-Output((Get-Date -Format G) + " INFO set UserDataPath in genexus.exe.config")
    $newEl_b = $xml.CreateElement("add");                               # Create a new Element 
    $nameAtt1_b = $xml.CreateAttribute("key");                         # Create a new attribute “key” 
    $nameAtt1_b.psbase.value = "UserAppDataPath";                    # Set the value of “key” attribute 
    $newEl_b.SetAttributeNode($nameAtt1_b);                              # Attach the “key” attribute 
    $nameAtt2_b = $xml.CreateAttribute("value");                       # Create “value” attribute  
    $nameAtt2_b.psbase.value = $userDataPath;                       # Set the value of “value” attribute 
    $newEl_b.SetAttributeNode($nameAtt2_b);                               # Attach the “value” attribute 
    $xml.configuration["appSettings"].AppendChild($newEl_b);    # Add the newly created element to the right position

    $xml.Save($gxExeConfigPath)

    Write-Output((Get-Date -Format G) + " [DEBUG] runGXInstall=$runGXInstall")
    if($runGXInstall -eq $False) {
        Write-Output((Get-Date -Format G) + " [INFO] Avoid genexus.com /install execution")
    } else {
        $gxInstallationPth = "$gxBasePath\GeneXus.com"
        Write-Output((Get-Date -Format G) + " INFO executing genexus.com /install")
        powershell "$gxInstallationPth /install"
    }
    if(Test-Path -Path $lastURIFilePath) { Remove-Item -Path $lastURIFilePath }
    $null = New-Item -Path $lastURIFilePath
    Set-Content -Path $lastURIFilePath -Value "$genexusURI"
    if(![string]::IsNullOrEmpty($localAndroidSDKPath)) {
        $androidRequirementsExe = "$gxBasePath\Android\Setup\AndroidRequirements.exe"
        Write-Output((Get-Date -Format G) + " INFO downloading androidSDK") 
        #AndroidRequirements.exe /s GXPATH=<path GX> ANDROIDSDKPATH=<path Android SDK> LOG=<path log>
        Start-Process -FilePath $androidRequirementsExe -ArgumentList "/s GXPATH=`"$gxBasePath`" ANDROIDSDKPATH=`"$localAndroidSDKPath`" LOG=`"$localAndroidSDKPath\androidsdk.log`"" -NoNewWindow -Wait
        Write-Output((Get-Date -Format G) + " INFO finish downloading androidSDK") 
    }
}