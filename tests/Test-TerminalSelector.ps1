param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'

$mixinPath = Join-Path $ProjectRoot 'src/main/resources/ae2_wide_wireless.mixins.json'
$mixinConfig = Get-Content -Raw -LiteralPath $mixinPath | ConvertFrom-Json
if ($mixinConfig.client -notcontains 'TerminalSelectionButtonMixin') {
    throw 'Client mixin configuration must register the terminal selector bridge'
}
if ($mixinConfig.mixins -contains 'WUTHandlerMixin') {
    throw 'Terminal switching must not globally intercept WUTHandler.open'
}
if ($mixinConfig.mixins -notcontains 'SelectTerminalPacketMixin') {
    throw 'Common mixin configuration must register the selector-packet bridge'
}

$mixinPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/TerminalSelectionButtonMixin.java'
$mixin = Get-Content -Raw -LiteralPath $mixinPath
if ($mixin -notmatch 'TerminalSelectionButton') {
    throw 'Terminal selector mixin must target AE2WTLib TerminalSelectionButton'
}
if ($mixin -notmatch 'pattern_encoding') {
    throw 'Terminal selector mixin must collapse the pattern-encoding entry'
}
if ($mixin -notmatch 'supportsCombinedLayout') {
    throw 'Terminal selector mixin must only affect combined wireless universal terminals'
}

$packetPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/SelectTerminalPacketMixin.java'
if (-not (Test-Path -LiteralPath $packetPath)) {
    throw 'Selector-packet mixin source is missing'
}
$packetMixin = Get-Content -Raw -LiteralPath $packetPath
if ($packetMixin -notmatch 'SelectTerminalPacket' -or
        $packetMixin -notmatch 'shouldOpenWideUniversal' -or
        $packetMixin -notmatch '@Redirect') {
    throw 'Selector-packet mixin must narrowly redirect the selector open call'
}

$screenPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/client/WideUniversalScreen.java'
$screen = Get-Content -Raw -LiteralPath $screenPath
if ($screen -notmatch 'cycleTerminalButton\(\)') {
    throw 'Wide universal screen must retain the native selector button'
}
if ($screen -notmatch 'addToLeftToolbar\(terminalSelector\)') {
    throw 'Universal terminal selector must be placed in the left toolbar'
}
if ($screen -notmatch 'CompactMagnetButton') {
    throw 'Universal terminal must use the compact magnet-card renderer'
}

Write-Host 'Terminal selector regression checks passed.'
