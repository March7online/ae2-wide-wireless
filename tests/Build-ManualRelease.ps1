param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [Parameter(Mandatory = $true)]
    [string]$ClassesDirectory,
    [Parameter(Mandatory = $true)]
    [string]$BaseJar,
    [Parameter(Mandatory = $true)]
    [string]$Version,
    [Parameter(Mandatory = $true)]
    [string]$StageDirectory,
    [Parameter(Mandatory = $true)]
    [string]$OutputJar
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

foreach ($requiredPath in @($ProjectRoot, $ClassesDirectory, $BaseJar)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required path does not exist: $requiredPath"
    }
}
if (Test-Path -LiteralPath $StageDirectory) {
    throw "Stage directory already exists: $StageDirectory"
}
if (Test-Path -LiteralPath $OutputJar) {
    throw "Output JAR already exists: $OutputJar"
}

[System.IO.Compression.ZipFile]::ExtractToDirectory($BaseJar, $StageDirectory)
Copy-Item -Recurse -Force -Path (Join-Path $ClassesDirectory '*') -Destination $StageDirectory
Copy-Item -Recurse -Force -LiteralPath (Join-Path $ProjectRoot 'src/main/resources/assets') -Destination $StageDirectory
Copy-Item -Force -LiteralPath (Join-Path $ProjectRoot 'src/main/resources/ae2_wide_wireless.mixins.json') -Destination $StageDirectory

$obsoleteMixin = Join-Path $StageDirectory 'dev/codex/ae2widewireless/mixin/ItemWTMenuHostMixin.class'
if (Test-Path -LiteralPath $obsoleteMixin) {
    Remove-Item -Force -LiteralPath $obsoleteMixin
}

$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$tomlPath = Join-Path $StageDirectory 'META-INF/neoforge.mods.toml'
$toml = Get-Content -Raw -LiteralPath $tomlPath
$modVersionRegex = [regex]::new(
    '(?m)^version="[^"]+"\r?$',
    [System.Text.RegularExpressions.RegexOptions]::None)
$toml = $modVersionRegex.Replace($toml, "version=`"$Version`"", 1)
[System.IO.File]::WriteAllText($tomlPath, $toml, $utf8NoBom)

$manifestPath = Join-Path $StageDirectory 'META-INF/MANIFEST.MF'
$manifest = Get-Content -Raw -LiteralPath $manifestPath
$manifest = [regex]::Replace($manifest, 'Specification-Version: .+', "Specification-Version: $Version")
$manifest = [regex]::Replace($manifest, 'Implementation-Version: .+', "Implementation-Version: $Version")
[System.IO.File]::WriteAllText($manifestPath, $manifest, $utf8NoBom)

$stageRoot = [System.IO.Path]::GetFullPath($StageDirectory).TrimEnd('\', '/')
$archive = [System.IO.Compression.ZipFile]::Open(
    $OutputJar,
    [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in Get-ChildItem -LiteralPath $StageDirectory -Recurse -File) {
        $entryName = $file.FullName.Substring($stageRoot.Length).TrimStart('\', '/').Replace('\', '/')
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
            $archive,
            $file.FullName,
            $entryName,
            [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally {
    $archive.Dispose()
}

$archive = [System.IO.Compression.ZipFile]::OpenRead($OutputJar)
try {
    $badEntries = @($archive.Entries | Where-Object { $_.FullName.Contains('\') })
    if ($badEntries.Count -ne 0) {
        throw "JAR contains entries with Windows path separators: $($badEntries[0].FullName)"
    }
} finally {
    $archive.Dispose()
}

Get-Item -LiteralPath $OutputJar | Select-Object FullName, Length, LastWriteTime
