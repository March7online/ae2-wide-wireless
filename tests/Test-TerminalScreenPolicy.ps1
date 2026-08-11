param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = 'Stop'
$sourceRoot = Join-Path $ProjectRoot 'src/main/java/dev/codex/ae2widewireless'
$testSource = Join-Path $ProjectRoot 'tests/java/dev/codex/ae2widewireless/TerminalScreenPolicyTest.java'
$policySource = Join-Path $sourceRoot 'TerminalScreenPolicy.java'
$stateSource = Join-Path $sourceRoot 'TerminalWidthState.java'

foreach ($required in @($testSource, $policySource, $stateSource)) {
    if (-not (Test-Path -LiteralPath $required)) {
        throw "Required terminal policy source is missing: $required"
    }
}

$tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
$classes = Join-Path $tempRoot ("ae2-wide-terminal-policy-" + [guid]::NewGuid().ToString('N'))
if (-not ([System.IO.Path]::GetFullPath($classes).StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase))) {
    throw "Refusing to use unexpected temporary directory: $classes"
}

try {
    New-Item -ItemType Directory -Path $classes | Out-Null
    & javac -proc:none -encoding UTF-8 -source 21 -target 21 -d $classes `
        $policySource $stateSource $testSource
    if ($LASTEXITCODE -ne 0) {
        throw "Terminal policy test compilation failed with exit code $LASTEXITCODE"
    }
    & java -cp $classes dev.codex.ae2widewireless.TerminalScreenPolicyTest
    if ($LASTEXITCODE -ne 0) {
        throw "Terminal policy test failed with exit code $LASTEXITCODE"
    }
}
finally {
    if (Test-Path -LiteralPath $classes) {
        Remove-Item -Recurse -Force -LiteralPath $classes
    }
}

Write-Host 'Terminal screen policy checks passed.'
