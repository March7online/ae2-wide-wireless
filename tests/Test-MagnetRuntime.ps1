param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$ClassesDirectory = (Join-Path (Split-Path -Parent $PSScriptRoot) 'build/manual-classes-2.1.4')
)

$ErrorActionPreference = 'Stop'

$mixinPath = Join-Path $ProjectRoot 'src/main/resources/ae2_wide_wireless.mixins.json'
$mixinConfig = Get-Content -Raw -LiteralPath $mixinPath | ConvertFrom-Json
if ($mixinConfig.mixins -notcontains 'CraftingTerminalHandlerMixin') {
    throw 'Mixin configuration must register the crafting-terminal cache bridge'
}

$mixinClass = Join-Path $ClassesDirectory 'dev/codex/ae2widewireless/mixin/CraftingTerminalHandlerMixin.class'
if (-not (Test-Path -LiteralPath $mixinClass)) {
    throw "Compiled CraftingTerminalHandlerMixin is missing at $mixinClass"
}

$libraryJars = Get-ChildItem -File (Join-Path $ProjectRoot 'build/manual-libs-2.1.3/*.jar')
$classPath = (@($ClassesDirectory) + @($libraryJars.FullName)) -join ';'
$bytecode = & javap -classpath $classPath -p -c dev.codex.ae2widewireless.mixin.CraftingTerminalHandlerMixin 2>&1 | Out-String
if ($bytecode -notmatch 'WideUniversalMenu' -or
        $bytecode -notmatch 'invokevirtual\s+.*invalidateCache' -or
        $bytecode -notmatch 'putfield\s+.*locator' -or
        $bytecode -notmatch 'getTerminalLocator') {
    throw 'Crafting-terminal cache mixin must invalidate the native cache and bind the current universal-terminal locator'
}

Write-Host 'Magnet runtime regression checks passed.'
