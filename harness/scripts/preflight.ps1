#requires -Version 5.1
<#
.SYNOPSIS
Run the stage checks before a checkpoint without writing a checkpoint.

.DESCRIPTION
Runs verify-artifacts.ps1 for the task/stage, prints a compact summary, fails on
FAIL or UNCHECKED, and treats MANUAL as a warning for the upcoming user review.
Optionally writes docs/checkpoints/T-XXX-CP-N.preflight.json for traceability.

.EXAMPLE
.\preflight.ps1 -Task T-033 -Stage U-02 -Checkpoint CP-2
.\preflight.ps1 -Task T-021 -Stage U-02
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Task,
    [Parameter(Mandatory = $true)][string]$Stage,
    [string]$Checkpoint = '',
    [string]$Root = '',
    [string]$ChecksDir = '',
    [switch]$NoMarker
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "harness-lib.ps1")
if (-not $Root) { $Root = Get-HarnessRoot }
$Root = (Resolve-Path -LiteralPath $Root).Path

$verify = Join-Path $PSScriptRoot 'verify-artifacts.ps1'
$temp = Join-Path ([System.IO.Path]::GetTempPath()) ("harness-preflight-{0}-{1}.json" -f $Task, [guid]::NewGuid().ToString('N'))
$args = @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $verify,
    '-Root', $Root, '-Task', $Task, '-Stage', $Stage, '-Json', $temp)
if ($ChecksDir) { $args += @('-ChecksDir', $ChecksDir) }

try {
    $output = & powershell @args 2>&1
    $code = $LASTEXITCODE
    if (-not (Test-Path -LiteralPath $temp)) {
        Write-Host "[preflight] FAIL: checks JSON not written" -ForegroundColor Red
        $output | ForEach-Object { Write-Host $_ }
        exit 2
    }
    $checks = (Read-Utf8 $temp) | ConvertFrom-Json
    $fail = [int]$checks.summary.fail
    $unchecked = [int]$checks.summary.unchecked
    $manual = [int]$checks.summary.manual
    $pass = [int]$checks.summary.pass

    Write-Host ("[preflight] {0} {1}: {2} PASS / {3} FAIL / {4} MANUAL / {5} UNCHECKED" -f `
            $Task, $Stage, $pass, $fail, $manual, $unchecked) -ForegroundColor $(if ($fail -gt 0 -or $unchecked -gt 0) { 'Red' } else { 'Green' })
    $problems = @($checks.results | Where-Object { $_.result -in @('FAIL', 'UNCHECKED') })
    if ($problems.Count -gt 0) {
        foreach ($p in ($problems | Select-Object -First 8)) {
            Write-Host ("  {0}: {1} - {2}" -f $p.result, $p.id, $p.detail) -ForegroundColor Red
        }
    }
    if ($manual -gt 0) {
        Write-Host "  manual items will still require the user at the checkpoint" -ForegroundColor Yellow
    }

    if ($Checkpoint -and -not $NoMarker) {
        $markerRel = "docs/checkpoints/$Task-$Checkpoint.preflight.json"
        $markerPath = Join-Path $Root ($markerRel -replace '/', '\')
        $markerDir = Split-Path -Parent $markerPath
        if (-not (Test-Path -LiteralPath $markerDir)) { New-Item -ItemType Directory -Force -Path $markerDir | Out-Null }
        $marker = [ordered]@{
            task = $Task
            stage = $Stage
            checkpoint = $Checkpoint
            generated_at = (Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')
            summary = $checks.summary
            problems = @($problems | ForEach-Object { [ordered]@{ id = $_.id; result = $_.result; detail = $_.detail } })
        }
        Write-Utf8NoBom $markerPath (($marker | ConvertTo-Json -Depth 20) + "`n")
        Write-Host "preflight marker: $markerRel" -ForegroundColor DarkGray
    }

    if ($fail -gt 0 -or $unchecked -gt 0) { exit 1 }
    exit 0
}
finally {
    Remove-Item -LiteralPath $temp -Force -ErrorAction SilentlyContinue
}
