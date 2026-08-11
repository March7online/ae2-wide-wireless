param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$ClassesDirectory = (Join-Path (Split-Path -Parent (Split-Path -Parent $ProjectRoot)) 'tempclasses-2.1.10b'),
    [string]$BaseJar = (Join-Path $ProjectRoot 'build/libs/ae2_wide_wireless-neoforge-1.21.1-2.1.9-final.jar')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$releaseVersion = '9.8.7-test'
$temporaryRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("ae2-wide-release-test-" + [guid]::NewGuid().ToString('N'))
$stageDirectory = Join-Path $temporaryRoot 'stage'
$outputJar = Join-Path $temporaryRoot 'release.jar'

try {
    New-Item -ItemType Directory -Path $temporaryRoot | Out-Null

    & (Join-Path $PSScriptRoot 'Build-ManualRelease.ps1') `
        -ProjectRoot $ProjectRoot `
        -ClassesDirectory $ClassesDirectory `
        -BaseJar $BaseJar `
        -Version $releaseVersion `
        -StageDirectory $stageDirectory `
        -OutputJar $outputJar | Out-Null

    $archive = [System.IO.Compression.ZipFile]::OpenRead($outputJar)
    try {
        $metadataEntry = $archive.GetEntry('META-INF/neoforge.mods.toml')
        if ($null -eq $metadataEntry) {
            throw 'Release JAR is missing META-INF/neoforge.mods.toml'
        }

        $reader = [System.IO.StreamReader]::new($metadataEntry.Open())
        try {
            $metadata = $reader.ReadToEnd()
        }
        finally {
            $reader.Dispose()
        }
    }
    finally {
        $archive.Dispose()
    }

    if ($metadata -notmatch '(?m)^modLoader="javafml"\r?$') {
        throw 'Release metadata must retain modLoader="javafml"'
    }
    if ($metadata -notmatch '(?m)^loaderVersion="\[4,\)"\r?$') {
        throw 'Release metadata must retain loaderVersion="[4,)"'
    }
    if ([regex]::IsMatch($metadata, '(?m)^loaderversion=')) {
        throw 'Release metadata must not contain the invalid lowercase loaderversion key'
    }

    $modsSection = [regex]::Match(
        $metadata,
        '(?ms)^\[\[mods\]\]\r?$.*?(?=^\[\[|\z)')
    if (-not $modsSection.Success) {
        throw 'Release metadata is missing the [[mods]] section'
    }
    $releaseVersionPattern = '(?m)^version="' + [regex]::Escape($releaseVersion) + '"\r?$'
    if ($modsSection.Value -notmatch $releaseVersionPattern) {
        throw "The [[mods]] version must be updated to $releaseVersion"
    }

    Write-Host 'Manual release metadata regression checks passed.'
}
finally {
    if (Test-Path -LiteralPath $temporaryRoot) {
        Remove-Item -Recurse -Force -LiteralPath $temporaryRoot
    }
}
