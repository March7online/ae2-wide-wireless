param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$JarPath = (Join-Path (Split-Path -Parent $PSScriptRoot) 'build/libs/ae2_wide_wireless-neoforge-1.21.1-2.1.22-client.1.jar')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

if (-not (Test-Path -LiteralPath $JarPath)) {
    throw "Built client-only JAR is missing: $JarPath"
}
if ([IO.Path]::GetFileName($JarPath) -match 'final') {
    throw 'Release filename must not contain final.'
}

$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $JarPath))
try {
    $metadataEntry = $archive.GetEntry('META-INF/neoforge.mods.toml')
    $manifestEntry = $archive.GetEntry('META-INF/MANIFEST.MF')
    if ($null -eq $metadataEntry -or $null -eq $manifestEntry) {
        throw 'Built JAR is missing NeoForge metadata or manifest.'
    }

    $reader = [IO.StreamReader]::new($metadataEntry.Open())
    try { $metadata = $reader.ReadToEnd() } finally { $reader.Dispose() }
    $reader = [IO.StreamReader]::new($manifestEntry.Open())
    try { $manifest = $reader.ReadToEnd() } finally { $reader.Dispose() }

    foreach ($check in @(
            @{ Pattern = '(?m)^modLoader="javafml"\r?$'; Message = 'modLoader is incorrect.' },
            @{ Pattern = '(?m)^loaderVersion="\[4,\)"\r?$'; Message = 'loaderVersion is incorrect.' },
            @{ Pattern = '(?m)^version="2\.1\.22-client\.1"\r?$'; Message = 'Packaged mod version is incorrect.' },
            @{ Pattern = '(?m)^displayName="AE2 Wide Wireless Terminal Client"\r?$'; Message = 'Packaged display name is incorrect.' },
            @{ Pattern = 'client-only'; Message = 'Packaged description does not identify the client-only edition.' })) {
        if ($metadata -notmatch $check.Pattern) {
            throw $check.Message
        }
    }
    if ([regex]::Matches($metadata, '(?m)^side="CLIENT"\r?$').Count -ne 5) {
        throw 'All four required dependencies and optional JEI dependency must be client-sided.'
    }
    if ($metadata -match '\$\{') {
        throw 'Packaged metadata still contains an unexpanded Gradle placeholder.'
    }
    if ($manifest -notmatch '(?m)^Implementation-Version: 2\.1\.22-client\.1\r?$') {
        throw 'Manifest implementation version is incorrect.'
    }
}
finally {
    $archive.Dispose()
}

Write-Host 'Release metadata checks passed.'
