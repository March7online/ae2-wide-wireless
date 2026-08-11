param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$JarPath = ''
)

$ErrorActionPreference = 'Stop'

function Assert-Contains {
    param([string]$Text, [string]$Pattern, [string]$Because)
    if ($Text -notmatch $Pattern) {
        throw $Because
    }
}

function Assert-NotContains {
    param([string]$Text, [string]$Pattern, [string]$Because)
    if ($Text -match $Pattern) {
        throw $Because
    }
}

$javaRoot = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless'
$resourceRoot = Join-Path $ProjectRoot 'src/main/resources'
$entrypoint = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $javaRoot 'WideWirelessMod.java')
$mixins = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $resourceRoot 'ae2_wide_wireless.mixins.json') | ConvertFrom-Json
$metadata = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $resourceRoot 'META-INF/neoforge.mods.toml')
$readme = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $ProjectRoot 'README.md')

Assert-Contains $entrypoint '@Mod\s*\(\s*value\s*=\s*WideWirelessMod\.MOD_ID\s*,\s*dist\s*=\s*Dist\.CLIENT\s*\)' `
    'Mod entrypoint must be restricted to Dist.CLIENT.'
Assert-NotContains $entrypoint 'MenuType|RegisterEvent|registerMenus|WideUniversalMenu|RegisterMenuScreensEvent' `
    'Client-only entrypoint must not register or reference a custom menu.'

if (@($mixins.mixins).Count -ne 0) {
    throw 'Common mixin list must be empty in the client-only edition.'
}

$forbiddenSources = @(
    'WideSlotSemantics.java',
    'client/CompactMagnetButton.java',
    'client/PlayerPreviewWidget.java',
    'client/RecipeTransferTargetButton.java',
    'client/WideUniversalScreen.java',
    'client/WideUniversalSettingsScreen.java',
    'menu/WideUniversalMenu.java',
    'menu/WideUniversalMenuHost.java',
    'mixin/CraftingTerminalHandlerMixin.java',
    'mixin/ItemWTMenuHostMixin.java',
    'mixin/ItemWUTMixin.java',
    'mixin/SelectTerminalPacketMixin.java',
    'mixin/TerminalSelectionButtonMixin.java',
    'mixin/Ae2JeiGhostIngredientHandlerMixin.java',
    'compat/jei/WideCraftingRecipeTransferHandler.java',
    'compat/jei/WideJeiPlugin.java',
    'compat/jei/WideJeiTransfers.java',
    'compat/jei/WideUniversalGhostIngredientHandler.java',
    'compat/jei/WideUniversalRecipeTransferHandler.java'
)
foreach ($relativePath in $forbiddenSources) {
    if (Test-Path -LiteralPath (Join-Path $javaRoot $relativePath)) {
        throw "Server or dual-layout source remains in client-only edition: $relativePath"
    }
}

$forbiddenResources = @(
    'assets/ae2/screens/ae2_wide_wireless/wide_universal_terminal.json',
    'assets/ae2_wide_wireless/textures/gui/wide_universal.png'
)
foreach ($relativePath in $forbiddenResources) {
    if (Test-Path -LiteralPath (Join-Path $resourceRoot $relativePath)) {
        throw "Dual-layout resource remains in client-only edition: $relativePath"
    }
}

$requiredDependencies = [regex]::Matches(
    $metadata,
    '(?ms)^\[\[dependencies\.\$\{mod_id\}\]\]\r?\n(?<body>.*?)(?=^\[\[|\z)') |
    ForEach-Object { $_.Groups['body'].Value } |
    Where-Object { $_ -match '(?m)^type="required"\r?$' }
if (@($requiredDependencies).Count -ne 4) {
    throw 'Expected four required dependencies in NeoForge metadata.'
}
foreach ($dependency in $requiredDependencies) {
    Assert-Contains $dependency '(?m)^side="CLIENT"\r?$' `
        'Every required dependency must be client-sided.'
}

Assert-Contains $metadata 'client-only|client only' `
    'Mod description must identify the client-only edition.'
Assert-Contains $readme 'Client-only edition' 'README must state that this is a client-only edition.'
Assert-Contains $readme 'No server installation is required' 'README must state that the server does not need this mod.'
Assert-Contains $readme 'cannot be installed at the same time' `
    'README must warn that client-only and full editions cannot coexist.'

if ($JarPath) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $JarPath))
    try {
        $entryNames = @($archive.Entries | ForEach-Object FullName)
        $forbiddenJarPatterns = @(
            '^dev/codex/ae2widewireless/menu/WideUniversal',
            '^dev/codex/ae2widewireless/client/WideUniversal',
            '^dev/codex/ae2widewireless/client/CompactMagnetButton',
            '^dev/codex/ae2widewireless/client/PlayerPreviewWidget',
            '^dev/codex/ae2widewireless/client/RecipeTransferTargetButton',
            '^dev/codex/ae2widewireless/mixin/(CraftingTerminalHandler|ItemWTMenuHost|ItemWUT|SelectTerminalPacket|TerminalSelectionButton|Ae2JeiGhostIngredientHandler)Mixin',
            '^dev/codex/ae2widewireless/compat/jei/',
            'wide_universal_terminal\.json$',
            'wide_universal\.png$'
        )
        foreach ($pattern in $forbiddenJarPatterns) {
            if ($entryNames -match $pattern) {
                throw "Client-only JAR contains forbidden entry matching: $pattern"
            }
        }
        if ($entryNames | Where-Object { $_ -match '\\' }) {
            throw 'JAR entries must use forward slashes.'
        }
    }
    finally {
        $archive.Dispose()
    }
}

Write-Host 'Client-only distribution checks passed.'
