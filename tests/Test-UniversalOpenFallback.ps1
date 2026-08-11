param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'

$modPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/WideWirelessMod.java'
$modSource = Get-Content -Raw -LiteralPath $modPath
if ($modSource -notmatch 'WTMenuHost\.class') {
    throw 'The dual-layout menu must resolve AE2WTLib native WTMenuHost instances'
}
if ($modSource -match 'MenuTypeBuilder[\s\S]*WideUniversalMenuHost\.class') {
    throw 'The dual-layout menu must not require an exact WideUniversalMenuHost from the item locator'
}

$menuPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/menu/WideUniversalMenu.java'
$menuSource = Get-Content -Raw -LiteralPath $menuPath
if ($menuSource -notmatch 'WideUniversalMenu\(int id, Inventory playerInventory, WTMenuHost host\)') {
    throw 'WideUniversalMenu must adapt a native WTMenuHost into the combined host'
}

$mixinConfigPath = Join-Path $ProjectRoot 'src/main/resources/ae2_wide_wireless.mixins.json'
$mixinConfig = Get-Content -Raw -LiteralPath $mixinConfigPath | ConvertFrom-Json
if ($mixinConfig.mixins -contains 'ItemWTMenuHostMixin') {
    throw 'The fragile exact-host ItemWTMenuHostMixin must no longer be active'
}

$itemMixinPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/ItemWUTMixin.java'
$itemMixin = Get-Content -Raw -LiteralPath $itemMixinPath
if ($itemMixin -notmatch 'if \(WideWirelessMod\.shouldOpenWideUniversal[\s\S]*?&& MenuOpener\.open\([\s\S]*?\)\) \{[\s\S]*?cir\.setReturnValue\(true\)') {
    throw 'Direct opening must cancel the native flow only after the combined menu opens successfully'
}

$packetMixinPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/SelectTerminalPacketMixin.java'
$packetMixin = Get-Content -Raw -LiteralPath $packetMixinPath
if ($packetMixin -notmatch 'if \(WideWirelessMod\.shouldOpenWideUniversal[\s\S]*?&& MenuOpener\.open') {
    throw 'Selector routing must treat the combined opener as a conditional attempt'
}
if ($packetMixin -notmatch 'return WUTHandler\.open\(player, locator, returningFromSubmenu\);') {
    throw 'Selector routing must fall back to AE2WTLib when the combined opener fails'
}

Write-Host 'Universal-terminal open fallback regression checks passed.'
