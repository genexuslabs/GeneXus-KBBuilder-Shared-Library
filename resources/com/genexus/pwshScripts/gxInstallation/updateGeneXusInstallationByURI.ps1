param (
    [Parameter(Mandatory=$True)]
    [ValidateNotNullOrEmpty()]
    [string] $genexusURI,
    [Parameter(Mandatory=$True)]
    [ValidateNotNullOrEmpty()]
    [string] $gxBasePath,
    [string] $localAndroidSDKPath,
    [boolean] $runGXInstall,
    [boolean] $cleanCustomSpecialFolders = $false
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
    Write-Output((Get-Date -Format G) + " [INFO] Downloading '$genexusURI' to '$blZip'")
    if($genexusURI.Contains("s3://")) {
        & aws s3 cp $genexusURI $blZip
    } else {
        $clnt = new-object System.Net.WebClient
        $clnt.DownloadFile("$genexusURI", $blZip)
    }

    Write-Output((Get-Date -Format G) + " [INFO] Unziping '$blZip' to '$gxBasePath'")
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
    Write-Output((Get-Date -Format G) + " [INFO] Remove genexus.zip ($blZip)")
    Remove-Item -Path $blZip -Recurse

    $gxExeConfigPath = "$gxBasePath\GeneXus.exe.config"
    $relativeProgramDataPath = "$gxBasePath\..\ProgramData"
    if ($cleanCustomSpecialFolders -and (Test-Path -Path $relativeProgramDataPath)) {
        Write-Output((Get-Date -Format G) + " [INFO] Cleaning '$relativeProgramDataPath' directory...")
        Remove-Item -Path $relativeProgramDataPath -Recurse -Force
    }   
    if(!(Test-Path -Path $relativeProgramDataPath)) {$null = New-Item -Path $relativeProgramDataPath -ItemType Directory}
    Set-Location -Path $relativeProgramDataPath
    $programDataPath = (Get-Location).Path
    $relativeUserDataPath = "$gxBasePath\..\UserData"
    if ($cleanCustomSpecialFolders -and (Test-Path -Path $relativeUserDataPath)) {
        Write-Output((Get-Date -Format G) + " [INFO] Cleaning '$relativeUserDataPath' directory...")
        Remove-Item -Path $relativeUserDataPath -Recurse -Force
    }   
    if(!(Test-Path -Path $relativeUserDataPath)) {$null = New-Item -Path $relativeUserDataPath -ItemType Directory}
    Set-Location -Path $relativeUserDataPath
    $userDataPath = (Get-Location).Path
    [xml]$xml = Get-Content $gxExeConfigPath
    Write-Output((Get-Date -Format G) + " [INFO] Set ProgramDataPath=$relativeProgramDataPath in genexus.exe.config")
    $newEl_a = $xml.CreateElement("add");                               # Create a new Element 
    $nameAtt1_a = $xml.CreateAttribute("key");                         # Create a new attribute “key” 
    $nameAtt1_a.psbase.value = "ProgramDataPath";                    # Set the value of “key” attribute 
    $newEl_a.SetAttributeNode($nameAtt1_a);                              # Attach the “key” attribute 
    $nameAtt2_a = $xml.CreateAttribute("value");                       # Create “value” attribute  
    $nameAtt2_a.psbase.value = $programDataPath;                       # Set the value of “value” attribute 
    $newEl_a.SetAttributeNode($nameAtt2_a);                               # Attach the “value” attribute 
    $xml.configuration["appSettings"].AppendChild($newEl_a);    # Add the newly created element to the right position

    Write-Output((Get-Date -Format G) + " [INFO] Set UserDataPath=$relativeUserDataPath in genexus.exe.config")
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
        Write-Output((Get-Date -Format G) + " [INFO] Executing genexus.com /install")
        powershell "$gxInstallationPth /install"
    }
    if(Test-Path -Path $lastURIFilePath) { Remove-Item -Path $lastURIFilePath }
    $null = New-Item -Path $lastURIFilePath
    Set-Content -Path $lastURIFilePath -Value "$genexusURI"

    if([string]::IsNullOrEmpty($localAndroidSDKPath)) {
        Write-Output((Get-Date -Format G) + " [INFO] AndroidSDK installation not configured") 
    } else {
        $androidRequirementsExe = "$gxBasePath\Android\Setup\AndroidRequirements.exe"
        if(-not(Test-Path -Path $androidRequirementsExe)) {
            if(-not(Test-Path -Path "$gxBasePath\Android\Setup")) {
                $null = New-Item -Path "$gxBasePath\Android\Setup" -ItemType Directory
            }
            $androidReqURI = "https://files.genexus.com/runtimesxev2u1/AndroidSDK18.exe"
            Write-Output((Get-Date -Format G) + " [INFO] Downloading androidSDK from $androidReqURI...")
            Invoke-WebRequest -Uri $androidReqURI -OutFile "$gxBasePath\Android\Setup\AndroidRequirements.exe"
        }
        Write-Output((Get-Date -Format G) + " [INFO] Installing $androidRequirementsExe to $localAndroidSDKPath...")
        #AndroidRequirements.exe /s GXPATH=<path GX> ANDROIDSDKPATH=<path Android SDK> LOG=<path log>
        Start-Process -FilePath $androidRequirementsExe -ArgumentList "/s GXPATH=`"$gxBasePath`" ANDROIDSDKPATH=`"$localAndroidSDKPath`" LOG=`"$localAndroidSDKPath\androidsdk.log`"" -NoNewWindow -Wait
        Write-Output((Get-Date -Format G) + " [INFO] Android SDK installation complete!") 
    }
}
