#requires -Version 5.1
param(
    [Parameter(Mandatory = $true)][string]$SpecPath
)

$ErrorActionPreference = 'Continue'
$spec = Get-Content -LiteralPath $SpecPath -Raw -Encoding UTF8 | ConvertFrom-Json
Set-Location -LiteralPath ([string]$spec.work_dir)
$code = $null
try {
    & ([string]$spec.file_path) @($spec.arguments) 1> ([string]$spec.out_file) 2> ([string]$spec.err_file)
    $code = $LASTEXITCODE
    if ($null -eq $code) {
        $code = if ($?) { 0 } else { 1 }
    }
}
catch {
    $code = 1
    try {
        Add-Content -LiteralPath ([string]$spec.err_file) -Value $_.Exception.Message -Encoding UTF8
    }
    catch { }
}
Set-Content -LiteralPath ([string]$spec.exit_file) -Value ([string]$code) -Encoding ASCII
