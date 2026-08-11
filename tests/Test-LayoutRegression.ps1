param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

function Assert-Equal {
    param(
        [Parameter(Mandatory)]$Actual,
        [Parameter(Mandatory)]$Expected,
        [Parameter(Mandatory)][string]$Because
    )

    if ($Actual -ne $Expected) {
        throw "$Because (expected '$Expected', got '$Actual')"
    }
}

function Get-OpaqueBoundsBelowStorageGrid {
    param([Parameter(Mandatory)][string]$Path)

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
        return [pscustomobject]@{ MinX = $minX; MaxX = $maxX }
    }
    finally {
        $bitmap.Dispose()
    }
}

$resourceRoot = Join-Path $ProjectRoot 'src/main/resources'
if (-not (Test-Path -LiteralPath $resourceRoot)) {
    $resourceRoot = $ProjectRoot
}

$textureRoot = Join-Path $resourceRoot 'assets/ae2_wide_wireless/textures/gui'
foreach ($textureName in @('wide_crafting.png', 'wide_pattern.png')) {
    $bounds = Get-OpaqueBoundsBelowStorageGrid (Join-Path $textureRoot $textureName)
    Assert-Equal $bounds.MinX 81 "$textureName lower module must be centered at x=81"
    Assert-Equal $bounds.MaxX 275 "$textureName lower module must retain its 195-pixel width"
}

$stylePath = Join-Path $resourceRoot 'assets/ae2/screens/ae2_wide_wireless/wide_universal_terminal.json'
$style = Get-Content -Raw -LiteralPath $stylePath | ConvertFrom-Json

$controlNames = @('wirelessTerminalSettingsButton', 'jeiTransferTarget', 'trashButton')
$controlBottoms = @(164, 142, 120)
for ($index = 0; $index -lt $controlNames.Count; $index++) {
    $widget = $style.widgets.($controlNames[$index])
    if ($null -eq $widget) {
        throw "Universal terminal is missing the $($controlNames[$index]) control"
    }
    Assert-Equal $widget.left 147 "$($controlNames[$index]) must be centered between both work areas"
    Assert-Equal $widget.bottom $controlBottoms[$index] "$($controlNames[$index]) vertical position is wrong"
}
Assert-Equal $style.widgets.jeiTransferTarget.width 16 'JEI target button must use the AE2 toolbar button width'
Assert-Equal $style.widgets.jeiTransferTarget.height 16 'JEI target button must use the AE2 toolbar button height'
$magnetButton = $style.widgets.magnetCardMenuButton
if ($null -eq $magnetButton) {
    throw 'Universal terminal is missing the compact magnet-card control beside search'
}
Assert-Equal $magnetButton.left 227 'Magnet-card control must sit immediately left of search'
Assert-Equal $magnetButton.top 4 'Magnet-card control vertical position is wrong'
Assert-Equal $magnetButton.width 12 'Magnet-card control must use the compact width'
Assert-Equal $magnetButton.height 12 'Magnet-card control must use the compact height'
$baseTerminalStylePath = Join-Path $resourceRoot 'assets/ae2/screens/terminals/base_terminal.json'
$baseTerminalStyle = Get-Content -Raw -LiteralPath $baseTerminalStylePath | ConvertFrom-Json
Assert-Equal ($baseTerminalStyle.widgets.search.left - ($magnetButton.left + $magnetButton.width)) 2 `
    'Magnet-card control and search must retain a two-pixel gap'
$screenPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/client/WideUniversalScreen.java'
$screenSource = Get-Content -Raw -LiteralPath $screenPath
if ($screenSource -notmatch 'CompactMagnetButton\(button') {
    throw 'Universal terminal must construct the compact magnet-card control'
}
$compactButtonPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/client/CompactMagnetButton.java'
$compactButtonSource = Get-Content -Raw -LiteralPath $compactButtonPath
if ($compactButtonSource -notmatch 'Icon\.MAGNET\b') {
    throw 'Compact magnet-card control must use the native magnet icon'
}

$armorSemantics = @('AE2WTLIB_HELMET', 'AE2WTLIB_CHESTPLATE', 'AE2WTLIB_LEGGINGS', 'AE2WTLIB_BOOTS')
$armorBottoms = @(84, 65, 45, 26)
for ($index = 0; $index -lt $armorSemantics.Count; $index++) {
    $slot = $style.slots.($armorSemantics[$index])
    Assert-Equal $slot.left 8 "$($armorSemantics[$index]) must use the wireless crafting terminal's equipment x position"
    Assert-Equal $slot.bottom $armorBottoms[$index] "$($armorSemantics[$index]) must distribute the four extra pixels symmetrically"
}
Assert-Equal $style.slots.AE2WTLIB_OFFHAND.left 73 'Offhand slot must touch the wireless crafting terminal player module'
Assert-Equal $style.slots.AE2WTLIB_OFFHAND.bottom 26 'Offhand slot must align with the boots and player hotbar'
Assert-Equal $style.widgets.playerPreview.left 26 'Player preview must use the wireless crafting terminal x position'
Assert-Equal $style.widgets.playerPreview.bottom 85 'Player preview vertical position is wrong'

if ($screenSource -notmatch 'new PlayerEntityWidget\(') {
    throw 'Universal terminal must use AE2WTLib''s wireless crafting terminal player renderer'
}
$playerViewport = [regex]::Match(
    $screenSource,
    'fill\(offsetX \+ (?<left>\d+), offsetY \+ imageHeight - (?<top>\d+),\s*' +
        'offsetX \+ (?<right>\d+), offsetY \+ imageHeight - (?<bottom>\d+)')
if (-not $playerViewport.Success) {
    throw 'Player viewport must share the exact top and bottom edges of the four armor backgrounds'
}
$armorBackgroundRight = [int]$style.slots.AE2WTLIB_HELMET.left - 1 + 18
$offhandBackgroundLeft = [int]$style.slots.AE2WTLIB_OFFHAND.left - 1
Assert-Equal ([int]$playerViewport.Groups['left'].Value) $armorBackgroundRight `
    'Player viewport must touch the armor backgrounds without a gray gap'
Assert-Equal ([int]$playerViewport.Groups['right'].Value) $offhandBackgroundLeft `
    'Player viewport must stop before the offhand background instead of overlapping it'
$inventoryModuleTop = [int]$style.slots.PLAYER_INVENTORY.bottom + 1
$inventoryModuleBottom = [int]$style.slots.PLAYER_HOTBAR.bottom - 17
Assert-Equal ([int]$playerViewport.Groups['top'].Value) $inventoryModuleTop `
    'Player viewport top must align with the player inventory background'
Assert-Equal ([int]$playerViewport.Groups['bottom'].Value) $inventoryModuleBottom `
    'Player viewport bottom must align with the hotbar background'

$armorBackgroundMatches = [regex]::Matches(
    $screenSource,
    'drawSlotBackground\(guiGraphics, offsetX \+ 7, offsetY \+ imageHeight - (?<bottom>\d+)\)')
$armorBackgroundBottoms = @(85, 66, 46, 27)
Assert-Equal $armorBackgroundMatches.Count $armorBackgroundBottoms.Count `
    'Universal terminal must draw all four armor backgrounds'
for ($index = 0; $index -lt $armorBackgroundBottoms.Count; $index++) {
    Assert-Equal ([int]$armorBackgroundMatches[$index].Groups['bottom'].Value) $armorBackgroundBottoms[$index] `
        "Armor background $index must align with its reflowed slot"
}

$modsTomlPath = Join-Path $resourceRoot 'META-INF/neoforge.mods.toml'
$modsToml = Get-Content -Raw -LiteralPath $modsTomlPath
if ($modsToml -notmatch '(?ms)^\[\[mixins\]\]\s*config="ae2_wide_wireless\.mixins\.json"') {
    throw 'NeoForge metadata must register ae2_wide_wireless.mixins.json'
}

$propertiesPath = Join-Path $ProjectRoot 'gradle.properties'
if (Test-Path -LiteralPath $propertiesPath) {
    $properties = Get-Content -Raw -LiteralPath $propertiesPath
    if ($properties -notmatch '(?m)^ae2_version_range=\[19\.2\.17\]$') {
        throw 'Source dependency range must pin the verified AE2 19.2.17 release'
    }
} else {
    if ($modsToml -notmatch '(?ms)modId="ae2".*?versionRange="\[19\.2\.17\]"') {
        throw 'Packaged metadata must pin the verified AE2 19.2.17 release'
    }
    if ($modsToml -notmatch '(?ms)modId="ae2wtlib".*?versionRange="\[19\.5\.1\]"') {
        throw 'Packaged metadata must pin the verified AE2WTLib 19.5.1 release'
    }
}

Write-Host 'Layout regression checks passed.'
