param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'

$stylePath = Join-Path $ProjectRoot 'src/main/resources/assets/ae2/screens/ae2_wide_wireless/wide_universal_terminal.json'
$style = Get-Content -Raw -Encoding UTF8 -LiteralPath $stylePath | ConvertFrom-Json
$manualSlotLeft = [int]$style.slots.AE2_WIDE_MANUAL_CRAFTING_GRID.left

$referencePath = Join-Path $ProjectRoot 'src/main/resources/assets/ae2/screens/ae2_wide_wireless/narrow/encoding/crafting.json'
$reference = Get-Content -Raw -Encoding UTF8 -LiteralPath $referencePath | ConvertFrom-Json
$referenceSlotLeft = [int]$reference.slots.CRAFTING_GRID.left
$nativePanelLeft = 8
$nativeGridInset = $referenceSlotLeft - $nativePanelLeft

$screenPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/client/WideUniversalScreen.java'
$screen = Get-Content -Raw -Encoding UTF8 -LiteralPath $screenPath
if ($screen -notmatch '\.src\(0, 0, 124, 66\)') {
    throw 'Manual crafting background must use AE2 CraftingEncodingPanel''s complete 124x66 source region'
}
$match = [regex]::Match($screen, '\.dest\(offsetX \+ (?<left>\d+), offsetY \+ imageHeight - 165\)')
if (-not $match.Success) {
    throw 'Manual crafting background destination is missing'
}
$manualPanelLeft = [int]$match.Groups['left'].Value
if (($manualSlotLeft - $manualPanelLeft) -ne $nativeGridInset) {
    throw "Manual crafting grid inset must match AE2's native $nativeGridInset-pixel inset (slot=$manualSlotLeft, panel=$manualPanelLeft)"
}

Write-Host 'Manual crafting bounds regression checks passed.'
