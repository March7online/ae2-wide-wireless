param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

function Assert-Equal {
    param($Actual, $Expected, [string]$Because)
    if ($Actual -ne $Expected) {
        throw "$Because (expected '$Expected', got '$Actual')"
    }
}

function Read-Json {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Missing JSON resource: $Path"
    }
    Get-Content -Raw -Encoding UTF8 -LiteralPath $Path | ConvertFrom-Json
}

function Get-OpaqueBoundsBelowStorageGrid {
    param([string]$Path)
    $bitmap = [System.Drawing.Bitmap]::new($Path)
    try {
        $minX = $bitmap.Width
        $maxX = -1
        for ($y = 71; $y -lt [Math]::Min(256, $bitmap.Height); $y++) {
            for ($x = 0; $x -lt [Math]::Min(357, $bitmap.Width); $x++) {
                if ($bitmap.GetPixel($x, $y).A -ne 0) {
                    $minX = [Math]::Min($minX, $x)
                    $maxX = [Math]::Max($maxX, $x)
                }
            }
        }
        [pscustomobject]@{ MinX = $minX; MaxX = $maxX }
    }
    finally {
        $bitmap.Dispose()
    }
}

$resourceRoot = Join-Path $ProjectRoot 'src/main/resources'
$textureRoot = Join-Path $resourceRoot 'assets/ae2_wide_wireless/textures/gui'
foreach ($textureName in @('wide_crafting.png', 'wide_pattern.png')) {
    $bounds = Get-OpaqueBoundsBelowStorageGrid (Join-Path $textureRoot $textureName)
    Assert-Equal $bounds.MinX 81 "$textureName lower module must be centered at x=81"
    Assert-Equal $bounds.MaxX 275 "$textureName lower module must keep its 195-pixel width"
}

$baseStyle = Read-Json (Join-Path $resourceRoot 'assets/ae2/screens/terminals/base_terminal.json')
Assert-Equal $baseStyle.terminalStyle.slotsPerRow 18 'Wide terminal must expose 18 storage columns'
Assert-Equal $baseStyle.terminalStyle.header.srcRect[2] 357 'Wide header must cover the full 357-pixel screen'
Assert-Equal $baseStyle.widgets.search.left 241 'Search field must remain on the right side of the header'
Assert-Equal $baseStyle.widgets.wideItemFilter.left 79 'Item filter position changed unexpectedly'
Assert-Equal $baseStyle.widgets.wideFluidFilter.left 104 'Fluid filter position changed unexpectedly'

$narrowStyle = Read-Json (Join-Path $resourceRoot 'assets/ae2/screens/ae2_wide_wireless/narrow/base_terminal.json')
Assert-Equal $narrowStyle.terminalStyle.slotsPerRow 9 'Narrow terminal must expose nine storage columns'

$wirelessPatternPath = Join-Path $resourceRoot 'assets/ae2/screens/wtlib/wireless_pattern_encoding_terminal.json'
$wirelessPatternStyle = Read-Json $wirelessPatternPath
if ($wirelessPatternStyle.includes -notcontains '../terminals/pattern_encoding_terminal.json') {
    throw 'Wireless pattern-encoding style must explicitly inherit the mod wide pattern-encoding style.'
}
if ($wirelessPatternStyle.includes -notcontains 'universal_terminal.json') {
    throw 'Wireless pattern-encoding style must retain AE2WTLib universal-terminal widgets.'
}

$sourceRoot = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless'
$screenMixin = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $sourceRoot 'mixin/MEStorageScreenMixin.java')
foreach ($requiredToken in @('wideItemFilter', 'wideFluidFilter', 'TerminalWidthState.toggle()', 'isWUT')) {
    if ($screenMixin -notmatch [regex]::Escape($requiredToken)) {
        throw "MEStorageScreenMixin is missing retained client feature: $requiredToken"
    }
}
if ($screenMixin -match 'WideUniversalScreen|WideUniversalMenu') {
    throw 'Storage screen mixin must not reference the removed dual-layout implementation.'
}

$mixinConfig = Read-Json (Join-Path $resourceRoot 'ae2_wide_wireless.mixins.json')
if (@($mixinConfig.mixins).Count -ne 0) {
    throw 'Client-only mixin configuration must not contain common mixins.'
}
foreach ($requiredMixin in @(
        'MEStorageScreenMixin',
        'PatternEncodingPanelMixin',
        'EaepPatternScreenMixin',
        'TianshuEncodingPanelMixin',
        'TianshuInitialStyleMixin',
        'TianshuPatternEncodingScreenMixin')) {
    if ($mixinConfig.client -notcontains $requiredMixin) {
        throw "Client mixin configuration is missing: $requiredMixin"
    }
}

$modsToml = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $resourceRoot 'META-INF/neoforge.mods.toml')
if ($modsToml -notmatch '(?ms)^\[\[mixins\]\]\r?\nconfig="ae2_wide_wireless\.mixins\.json"') {
    throw 'NeoForge metadata must register the client mixin configuration.'
}

$properties = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $ProjectRoot 'gradle.properties')
if ($properties -notmatch '(?m)^ae2_version_range=\[19\.2\.17\]\r?$') {
    throw 'AE2 dependency range must pin 19.2.17 and accept CRLF checkouts.'
}
if ($properties -notmatch '(?m)^mod_version=2\.1\.22-client\.1\r?$') {
    throw 'Client-only source version is incorrect.'
}

Write-Host 'Layout regression checks passed.'
