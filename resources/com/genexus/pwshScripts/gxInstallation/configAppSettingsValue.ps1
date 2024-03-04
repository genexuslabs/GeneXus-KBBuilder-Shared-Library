param (
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $gxBasePath,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $addKey,
    [Parameter(Mandatory=$True)]
	[ValidateNotNullOrEmpty()]
    [string] $addValue
)
$ErrorActionPreference="Stop"
#
$webConfigPath = "$gxBasePath\genexus.exe.config"
[xml]$xml = Get-Content $webConfigPath 
$nodeElem = $xml.CreateElement("add");
##
$nodeAtt = $xml.CreateAttribute("key");
$nodeAtt.psbase.value = $addKey;
$nodeElem.SetAttributeNode($nodeAtt);
$nodeAttValue = $xml.CreateAttribute("value");
$nodeAttValue.psbase.value = $addValue;
$nodeElem.SetAttributeNode($nodeAttValue);
$xml.configuration["appSettings"].AppendChild($nodeElem);

$xml.Save($webConfigPath)
Write-Output((Get-Date -Format G) + " [INFO] Success write add tag in $webConfigPath ")
#