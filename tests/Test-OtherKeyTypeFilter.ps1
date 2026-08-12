param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'

function Assert-Contains {
    param(
        [Parameter(Mandatory)][string]$Text,
        [Parameter(Mandatory)][string]$Needle,
        [Parameter(Mandatory)][string]$Because
    )

    if ($Text -notmatch [regex]::Escape($Needle)) {
        throw "$Because (missing '$Needle')"
    }
}

function Read-Json {
    param([Parameter(Mandatory)][string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Missing JSON resource: $Path"
    }
    Get-Content -Raw -Encoding UTF8 -LiteralPath $Path | ConvertFrom-Json
}

$sourcePath = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless/mixin/MEStorageScreenMixin.java'
$source = Get-Content -Raw -Encoding UTF8 -LiteralPath $sourcePath

Assert-Contains $source 'wideOtherFilter' 'MEStorageScreenMixin must create the other-key-type filter button'
Assert-Contains $source 'AEKeyTypes.getAll()' 'Other filter must derive its key types from the AE2 registry'
Assert-Contains $source 'AEKeyType.items()' 'Other filter must exclude the item key type'
Assert-Contains $source 'AEKeyType.fluids()' 'Other filter must exclude the fluid key type'
Assert-Contains $source 'ae2Wide$isOnlyOtherEnabled' 'Other filter must have its own checked-state calculation'

$wideStyle = Read-Json (Join-Path $ProjectRoot 'src/main/resources/assets/ae2/screens/terminals/base_terminal.json')
if ($null -eq $wideStyle.widgets.wideOtherFilter) {
    throw 'Wide terminal style is missing wideOtherFilter'
}
if ($wideStyle.widgets.wideOtherFilter.left -le $wideStyle.widgets.wideFluidFilter.left) {
    throw 'wideOtherFilter must be placed after wideFluidFilter'
}

$narrowStyle = Read-Json (Join-Path $ProjectRoot 'src/main/resources/assets/ae2/screens/ae2_wide_wireless/narrow/base_terminal.json')
if ($null -eq $narrowStyle.widgets.wideOtherFilter) {
    throw 'Narrow terminal style is missing wideOtherFilter'
}
if ($narrowStyle.widgets.wideOtherFilter.left -le $narrowStyle.widgets.wideFluidFilter.left) {
    throw 'Narrow wideOtherFilter must be placed after wideFluidFilter'
}

foreach ($lang in @('en_us.json', 'zh_cn.json')) {
    $langText = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $ProjectRoot "src/main/resources/assets/ae2_wide_wireless/lang/$lang")
    Assert-Contains $langText 'gui.ae2_wide_wireless.other_filter' "$lang must define the other filter label"
    Assert-Contains $langText 'gui.ae2_wide_wireless.other_filter.hint' "$lang must define the other filter hint"
}

Write-Host 'Other key-type filter checks passed.'
