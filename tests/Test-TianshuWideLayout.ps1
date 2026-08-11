param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'

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

function Read-Json {
    param([Parameter(Mandatory)][string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Missing required JSON resource: $Path"
    }
    return Get-Content -Raw -Encoding UTF8 -LiteralPath $Path | ConvertFrom-Json
}

$resourceRoot = Join-Path $ProjectRoot 'src/main/resources'
$wiredPath = Join-Path $resourceRoot 'assets/ae2/screens/ae2_wide_wireless/tianshu_pattern_encoding_terminal.json'
$narrowWiredPath = Join-Path $resourceRoot 'assets/ae2/screens/ae2_wide_wireless/narrow/tianshu_pattern_encoding_terminal.json'
$narrowBasePath = Join-Path $resourceRoot 'assets/ae2/screens/ae2_wide_wireless/narrow/base_terminal.json'
$widePatternPath = Join-Path $resourceRoot 'assets/ae2/screens/terminals/pattern_encoding_terminal.json'
$wirelessPath = Join-Path $resourceRoot 'assets/ae2/screens/ae2_wide_wireless/wireless_tianshu_pattern_encoding_terminal.json'
$narrowWirelessPath = Join-Path $resourceRoot 'assets/ae2/screens/ae2_wide_wireless/narrow/wireless_tianshu_pattern_encoding_terminal.json'
$wired = Read-Json $wiredPath
$narrowWired = Read-Json $narrowWiredPath
$narrowBase = Read-Json $narrowBasePath
$widePattern = Read-Json $widePatternPath
$wireless = Read-Json $wirelessPath
$narrowWireless = Read-Json $narrowWirelessPath

if ($wired.includes -notcontains '../terminals/pattern_encoding_terminal.json') {
    throw 'Wired Tianshu style must inherit the wide pattern-encoding terminal style'
}
Assert-Equal $narrowBase.terminalStyle.slotsPerRow 9 `
    'Standalone Tianshu narrow style must resolve to nine storage columns'
if ($wireless.includes -notcontains 'tianshu_pattern_encoding_terminal.json' -or
        $wireless.includes -notcontains '../wtlib/universal_terminal.json') {
    throw 'Wireless Tianshu style must inherit the wired Tianshu and WTLib universal-terminal styles'
}

# Resolve every narrow wireless include exactly as StyleManager does. This
# catches paths that are syntactically valid JSON but point into the wrong
# ae2_wide_wireless/narrow subdirectory at runtime.
Add-Type -AssemblyName System.IO.Compression.FileSystem
$wtlibJarPath = Join-Path $ProjectRoot 'build/manual-compile-libs-2.1.17b/ae2wtlib-api-19.5.1.jar'
if (-not (Test-Path -LiteralPath $wtlibJarPath)) {
    $wtlibCacheRoot = Join-Path $env:USERPROFILE `
        '.gradle/caches/modules-2/files-2.1/de.mari_023/ae2wtlib_api/19.5.1'
    $wtlibJarPath = Get-ChildItem -LiteralPath $wtlibCacheRoot -Recurse -Filter '*.jar' -File |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $wtlibJarPath -or -not (Test-Path -LiteralPath $wtlibJarPath)) {
    throw 'AE2WTLib API 19.5.1 JAR is required for the Tianshu style include audit.'
}
$wtlibArchive = [System.IO.Compression.ZipFile]::OpenRead($wtlibJarPath)
try {
    $styleDirectory = Split-Path -Parent $narrowWirelessPath
    $rootWithSeparator = [System.IO.Path]::GetFullPath($resourceRoot).TrimEnd('\', '/') + `
        [System.IO.Path]::DirectorySeparatorChar
    foreach ($include in $narrowWireless.includes) {
        $resolvedPath = [System.IO.Path]::GetFullPath((Join-Path $styleDirectory $include))
        if (-not $resolvedPath.StartsWith($rootWithSeparator, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "Narrow wireless Tianshu include escapes the resource root: $include -> $resolvedPath"
        }
        $entryName = $resolvedPath.Substring($rootWithSeparator.Length).Replace('\', '/')
        if (-not (Test-Path -LiteralPath $resolvedPath) -and $null -eq $wtlibArchive.GetEntry($entryName)) {
            throw "Narrow wireless Tianshu include does not resolve: $include -> $entryName"
        }
    }
}
finally {
    $wtlibArchive.Dispose()
}

$expectedWidgets = @{
    modeTabButton2 = 254
    modeTabButton3 = 254
    modeTabButton4 = 254
    processingClearPattern = 160
    processingCycleOutput = 179
    processingMultiply2 = 162
    processingMultiply5 = 175
    processingDivide2 = 162
    processingDivide5 = 175
    advancedEncodingButton = 162
    overloadEncodingButton = 175
    closedLoopPanel = 90
    closedLoopScrollbar = 96
    closedLoopDetail = 149
    closedLoopAutoFill = 178
    closedLoopClear = 139
    closedLoopSeedRefill = 205
    closedLoopCycleOutput = 214
    closedLoopExecMultiplier = 160
    closedLoopStoredMultiplier = 160
}

foreach ($widgetName in $expectedWidgets.Keys) {
    $widget = $wired.widgets.$widgetName
    if ($null -eq $widget) {
        throw "Wired Tianshu style is missing widget '$widgetName'"
    }
    Assert-Equal $widget.left $expectedWidgets[$widgetName] "Wired Tianshu widget '$widgetName' is not centered"
}

$expectedWideSlots = @{
    CRAFTING_GRID = 96
    CRAFTING_RESULT = 187
    PROCESSING_INPUTS = 105
    PROCESSING_OUTPUTS = 190
    SMITHING_TABLE_TEMPLATE = 96
    SMITHING_TABLE_BASE = 114
    SMITHING_TABLE_ADDITION = 132
    SMITHING_TABLE_RESULT = 190
    STONECUTTING_INPUT = 96
}
foreach ($slotName in $expectedWideSlots.Keys) {
    $slot = $widePattern.slots.$slotName
    if ($null -eq $slot) {
        throw "Wide pattern style is missing slot '$slotName'"
    }
    Assert-Equal $slot.left $expectedWideSlots[$slotName] "Wide pattern slot '$slotName' is not centered"
}

$expectedWideWidgets = @{
    craftingClearPattern = 151
    craftingSubstitutions = 161
    craftingFluidSubstitutions = 171
    processingPatternModeScrollbar = 96
    smithingTableClearPattern = 95
    smithingTableSubstitutions = 105
    stonecuttingPatternModeScrollbar = 198
}
foreach ($widgetName in $expectedWideWidgets.Keys) {
    $widget = $widePattern.widgets.$widgetName
    if ($null -eq $widget) {
        throw "Wide pattern style is missing widget '$widgetName'"
    }
    Assert-Equal $widget.left $expectedWideWidgets[$widgetName] "Wide pattern widget '$widgetName' is not centered"
}

$expectedModePanels = @('modePanel0', 'modePanel1', 'modePanel2', 'modePanel3', 'modePanel4', 'modePanel5')
foreach ($panelName in $expectedModePanels) {
    $panel = $wired.widgets.$panelName
    if ($null -eq $panel) {
        throw "Wired Tianshu style is missing '$panelName'"
    }
    Assert-Equal $panel.left 90 "Wired Tianshu '$panelName' is not centered"
}

$mixinConfigPath = Join-Path $resourceRoot 'ae2_wide_wireless.mixins.json'
$mixinConfig = Get-Content -Raw -Encoding UTF8 -LiteralPath $mixinConfigPath | ConvertFrom-Json
if ($mixinConfig.client -notcontains 'TianshuEncodingPanelMixin') {
    throw 'Client mixin configuration must register the Tianshu background compatibility mixin'
}
if ($mixinConfig.client -notcontains 'TianshuPatternEncodingScreenMixin') {
    throw 'Client mixin configuration must register the Tianshu widget reflow mixin'
}

$screenMixinPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/TianshuPatternEncodingScreenMixin.java'
if (-not (Test-Path -LiteralPath $screenMixinPath)) {
    throw 'Tianshu pattern-encoding screen reflow mixin source is missing'
}
$screenMixinSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $screenMixinPath
foreach ($targetClass in @(
        'TianshuPatternEncodingTermScreen',
        'TianshuWirelessPatternEncodingTermScreen')) {
    if ($screenMixinSource -notmatch [regex]::Escape($targetClass)) {
        throw "Tianshu screen reflow mixin is missing target '$targetClass'"
    }
}
if ($screenMixinSource -notmatch 'ae2Wide\$setStyle' -or
        $screenMixinSource -notmatch 'getTerminalStyle\(\)\.getSlotsPerRow\(\)') {
    throw 'Tianshu screen reflow mixin must reapply the wide widget style after AE2 initializes late-added controls'
}
$initialStyleMixinPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/TianshuInitialStyleMixin.java'
if (-not (Test-Path -LiteralPath $initialStyleMixinPath)) {
    throw 'Tianshu constructor-style mixin source is missing'
}
$initialStyleMixinSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $initialStyleMixinPath
if ($initialStyleMixinSource -notmatch '@ModifyArgs' -or
        $initialStyleMixinSource -notmatch 'MEStorageScreen;<init>' -or
        $initialStyleMixinSource -notmatch 'TerminalWidthState\.getTianshuStylePath') {
    throw 'Tianshu must replace the conflicting AE2LT style before MEStorageScreen construction'
}
if ($initialStyleMixinSource -match 'TianshuWirelessPatternEncodingTermScreen') {
    throw 'Wireless Tianshu must inherit constructor style selection through the wired superclass instead of a second constructor injection'
}
if ($mixinConfig.client -notcontains 'TianshuInitialStyleMixin') {
    throw 'Client mixin configuration must register the Tianshu constructor-style mixin'
}

$mixinSourcePath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/TianshuEncodingPanelMixin.java'
if (-not (Test-Path -LiteralPath $mixinSourcePath)) {
    throw 'Tianshu encoding-panel compatibility mixin source is missing'
}
$mixinSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $mixinSourcePath
foreach ($targetClass in @(
        'TianshuCraftingEncodingPanel',
        'TianshuProcessingEncodingPanel',
        'TianshuSmithingTableEncodingPanel',
        'TianshuStonecuttingEncodingPanel')) {
    if ($mixinSource -notmatch [regex]::Escape($targetClass)) {
        throw "Tianshu compatibility mixin is missing target '$targetClass'"
    }
}
if ($mixinSource -notmatch 'ModifyConstant' -or $mixinSource -notmatch '89') {
    throw 'Tianshu compatibility mixin must move the custom panel background to x=89 in wide mode'
}

$patternMixinPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/PatternEncodingPanelMixin.java'
$patternMixinSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $patternMixinPath
if ($patternMixinSource -match 'return screen\.getStyle\(\)\.getTerminalStyle\(\)\.getSlotsPerRow\(\) > 9 \? 89 : 8;') {
    throw 'AE2 original pattern-encoding panel must not apply a second wide offset'
}
if ($patternMixinSource -notmatch '(?s)ae2Wide\$centerPatternPanelBackground.*?return original;') {
    throw 'AE2 original pattern-encoding panel must preserve its original internal offset'
}

$screenSourcePath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/MEStorageScreenMixin.java'
$screenSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $screenSourcePath
foreach ($screenName in @('TianshuPatternEncodingTermScreen', 'TianshuWirelessPatternEncodingTermScreen')) {
    if ($screenSource -notmatch [regex]::Escape($screenName)) {
        throw "MEStorageScreenMixin is missing Tianshu style routing for '$screenName'"
    }
}
foreach ($privateStylePath in @(
        '/screens/ae2_wide_wireless/tianshu_pattern_encoding_terminal.json',
        '/screens/ae2_wide_wireless/wireless_tianshu_pattern_encoding_terminal.json')) {
    if ($screenSource -notmatch [regex]::Escape($privateStylePath)) {
        throw "MEStorageScreenMixin must route Tianshu wide mode through private style '$privateStylePath'"
    }
}
if ($screenSource -match 'ae2Wide\$tianshuStyleApplied') {
    throw 'Tianshu construction must not be followed by a second queued style initialization'
}

$bridgeSourcePath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/WidgetContainerStyleBridge.java'
$bridgeSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $bridgeSourcePath
if ($bridgeSource -notmatch 'Rect2i' -or $bridgeSource -notmatch 'ae2Wide\$setStyle\(') {
    throw 'Style bridge must carry absolute and relative bounds when applying a replacement layout'
}

$widgetMixinPath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/WidgetContainerMixin.java'
$widgetMixinSource = Get-Content -Raw -Encoding UTF8 -LiteralPath $widgetMixinPath
foreach ($requiredCall in @('WidgetStyle', '\.resolve\(', '\.setPosition\(', '\.move\(')) {
    if ($widgetMixinSource -notmatch $requiredCall) {
        throw "WidgetContainer style reflow is missing '$requiredCall'"
    }
}
if ($screenSource -notmatch 'ae2Wide\$setStyle\(' -or $screenSource -notmatch 'nextStyle') {
    throw 'MEStorageScreenMixin must explicitly reflow widgets after changing the terminal style'
}

$styleAccessorSource = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/AEBaseScreenStyleAccessor.java')
if ($styleAccessorSource -notmatch '@Invoker' -or $styleAccessorSource -notmatch 'positionSlots') {
    throw 'AEBaseScreen style accessor must expose the native slot reflow after a style switch'
}
if ($screenSource -notmatch 'ae2Wide\$repositionSlots\(') {
    throw 'MEStorageScreenMixin must reposition menu slots after changing the terminal style'
}

Write-Host 'Tianshu wide-layout regression checks passed.'
