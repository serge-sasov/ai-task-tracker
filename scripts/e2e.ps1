# -*- coding: utf-8 -*-
#Requires -Version 5.1
<#
.SYNOPSIS
    Deterministic E2E runner for ai-task-tracker (T-009, stage I-02).

.DESCRIPTION
    Manages an ISOLATED LOCAL test stack only (127.0.0.1) and runs the
    harness-e2e/1 dataset against it via the pytest plugin in tests/e2e/.
    Production systems are never touched.

.PARAMETER Cmd
    list    load the YAML and print UC ids/names (no stack required)
    up      bring up the isolated local stack (fail-fast, bounded health wait)
    down    docker compose down -v (isolated local stack)
    run     bring up the stack if down; starts pytest detached by default
    status  show the current detached e2e run status and log tail
    abort   stop the current detached e2e run and its process tree
    all     list then run (default)

.PARAMETER UC
    Restrict the run to one or more UC-E2E-NNN ids (repeatable).

.PARAMETER ResultsDir
    Override the JSON results directory (default: tests/e2e/results).

.PARAMETER Wait
    Run pytest in the foreground; intended for CI/QA short checks only.

.PARAMETER Reset
    Tear down the stack with `docker compose down -v` before starting it.

.PARAMETER Build
    Rebuild the images on `up`/`run` (implies a clean build of changed services).

.PARAMETER StackTimeoutSec
    Bound for the backend/frontend health wait (default 90 seconds).

.PARAMETER RunTimeoutSec
    Bound for a detached pytest run (default 900 seconds).

.EXAMPLE
    .\scripts\e2e.ps1 list
    .\scripts\e2e.ps1 up
    .\scripts\e2e.ps1 run -UC UC-E2E-001 -UC UC-E2E-002
    .\scripts\e2e.ps1 status
    .\scripts\e2e.ps1 abort
    .\scripts\e2e.ps1 run -Wait -UC UC-E2E-007
#>
[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet('list','up','down','run','all','status','abort')]
    [string]$Cmd = 'all',

    [string[]]$UC,

    [string]$ResultsDir = '',

    [switch]$Force,
    [switch]$Wait,
    [switch]$Reset,
    [switch]$Build,
    [int]$StackTimeoutSec = 90,
    [int]$RunTimeoutSec = 900,

    [switch]$NoExit
)

$ErrorActionPreference = 'Stop'
$RepoRoot = $PSScriptRoot
# Resolve back to repo root (scripts/ is one level below root).
$RepoRoot = (Get-Item $PSScriptRoot).Parent.FullName

$Dataset = Join-Path $RepoRoot 'docs\analysis\e2e-test-data-ai-task-tracker.yaml'
$Conftest = Join-Path $RepoRoot 'tests\e2e\conftest.py'
$ComposeFile = Join-Path $RepoRoot 'docker-compose.yml'
$DefaultResults = Join-Path $RepoRoot 'tests\e2e\results'
$Results = if ($ResultsDir) { $ResultsDir } else { $DefaultResults }
# Dedicated, deterministic source dir for the e2e seed corpus (host side).
# docker-compose bind-mounts it read-only; the suite writes the fixtures there.
$SeedDir = Join-Path $RepoRoot 'data\e2e-opencode'
$Monitor = Join-Path $RepoRoot 'harness\scripts\run-monitored.ps1'
$RunName = 'e2e-run'
$RunStateDir = Join-Path $RepoRoot 'harness\run-state'
$RunStateFile = Join-Path $RunStateDir "$RunName\status.json"

# Base URL/port: honour E2E_BASE_URL and let the stack bind a free host port.
# compose maps 127.0.0.1:${E2E_BACKEND_PORT:-8080}:8080, so the same value must
# be exported for the compose commands and used for the health checks.
$BaseUrl = if ($env:E2E_BASE_URL) { $env:E2E_BASE_URL.TrimEnd('/') } else { 'http://127.0.0.1:8080' }
if (-not $env:E2E_BACKEND_PORT) {
    try {
        $uri = [System.Uri]$BaseUrl
        $env:E2E_BACKEND_PORT = if ($uri.Port -gt 0) { [string]$uri.Port } else { '8080' }
    } catch {
        $env:E2E_BACKEND_PORT = '8080'
    }
}
$env:E2E_BASE_URL = $BaseUrl
# The frontend is served on its own host port (docker-compose maps it to
# 127.0.0.1:${E2E_FRONTEND_PORT:-8081}:80); the runner exports the same URL to
# the tests so the built-frontend DOM smoke can read the served SPA.
$FrontendBase = if ($env:E2E_FRONTEND_URL) { $env:E2E_FRONTEND_URL.TrimEnd('/') } else {
    $fp = $env:E2E_FRONTEND_PORT
    if (-not $fp) { $fp = '8081' }
    "http://127.0.0.1:$fp"
}
if (-not $env:E2E_FRONTEND_PORT) {
    try {
        $uri = [System.Uri]$FrontendBase
        $env:E2E_FRONTEND_PORT = if ($uri.Port -gt 0) { [string]$uri.Port } else { '8081' }
    } catch {
        $env:E2E_FRONTEND_PORT = '8081'
    }
    $fp = $env:E2E_FRONTEND_PORT
    $FrontendBase = if ($env:E2E_FRONTEND_URL) { $env:E2E_FRONTEND_URL.TrimEnd('/') } else { "http://127.0.0.1:$fp" }
}

function Get-PythonCmd {
    if (Get-Command python -ErrorAction SilentlyContinue) { return 'python' }
    if (Get-Command py -ErrorAction SilentlyContinue) { return 'py' }
    throw "python interpreter not found on PATH"
}
$PY = Get-PythonCmd

function New-ResultArg {
    # Emit the pytest --results-dir argument only when overridden.
    if ($ResultsDir) { return @('--results-dir', $Results) }
    return @()
}

# --------------------------------------------------------------------------- #
# list : load YAML, print UC ids/names (no stack)
# --------------------------------------------------------------------------- #
function Invoke-List {
    if (-not (Test-Path -LiteralPath $Dataset)) {
        throw "dataset not found: $Dataset"
    }
    $code = @'
import os, sys
sys.path.insert(0, os.environ.get("E2E_PYROOT", ""))
from tests.e2e import conftest
rows = conftest.list_use_cases()
for r in rows:
    print("%s\t[%s]\t%s" % (r["id"], r.get("priority",""), r["title"]))
print("uc_count=%d" % len(rows))
'@
    $env:E2E_PYROOT = $RepoRoot
    python_lines $code
    Remove-Variable -Name E2E_PYROOT -Scope Script -ErrorAction SilentlyContinue
}

# --------------------------------------------------------------------------- #
# up / down : manage the isolated local stack
# --------------------------------------------------------------------------- #
# Run `docker compose` with explicit arguments and capture stderr.
#
# PowerShell 5.1 promotes ANY native-command output on stderr to a terminating
# error (NativeCommandUseError) when
# $ErrorActionPreference = 'Stop'. The compose CLI writes progress
# ("Network ... Removing", "Container ... Starting") to stderr on a normal,
# successful run, so a bare call would blow up here. We save and relax
# $ErrorActionPreference around the docker call (this is the only reliably
# suppressing mechanism in 5.1 -- neither `2>&1` nor `*>` temp-file redirect
# neutralizes the terminating error), read $LASTEXITCODE to decide success,
# then restore the caller's $ErrorActionPreference. Any stderr text is
# captured for diagnostics and surfaced only when the command fails.
# --------------------------------------------------------------------------- #
$script:ComposeCmd = $null
function Get-ComposeCommand {
    if ($script:ComposeCmd) { return $script:ComposeCmd }
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'SilentlyContinue'
    try {
        if (Get-Command docker -ErrorAction SilentlyContinue) {
            & docker compose version *> $null
            if ($LASTEXITCODE -eq 0) { $script:ComposeCmd = @('docker', 'compose'); return $script:ComposeCmd }
        }
        if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
            & docker-compose version *> $null
            if ($LASTEXITCODE -eq 0) { $script:ComposeCmd = @('docker-compose'); return $script:ComposeCmd }
        }
    }
    finally {
        $ErrorActionPreference = $prev
    }
    throw 'Docker Compose is not available (neither "docker compose" nor "docker-compose")'
}

function Invoke-Compose([string[]]$ComposeArgs) {
    $cmd = Get-ComposeCommand
    $exe = $cmd[0]
    $prefix = @()
    if ($cmd.Count -gt 1) { $prefix = @($cmd[1..($cmd.Count - 1)]) }
    $prevEap = $ErrorActionPreference
    $ErrorActionPreference = 'SilentlyContinue'
    try {
        $output = & $exe @prefix -f $ComposeFile @ComposeArgs 2>&1
        $text = if ($output) { ($output -join "`r`n").Trim() } else { '' }
        if ($LASTEXITCODE -ne 0) {
            throw "docker compose $($ComposeArgs -join ' ') failed (exit $LASTEXITCODE). output: $text"
        }
    }
    finally {
        $ErrorActionPreference = $prevEap
    }
    return ''
}

function Test-Health([string]$Url) {
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        return ($response.StatusCode -eq 200)
    }
    catch {
        return $false
    }
}

function Show-Diagnostics {
    Write-Host '[e2e] diagnostics:' -ForegroundColor Yellow
    try {
        $cmd = Get-ComposeCommand
        $exe = $cmd[0]
        $prefix = @()
        if ($cmd.Count -gt 1) { $prefix = @($cmd[1..($cmd.Count - 1)]) }
        & $exe @prefix -f $ComposeFile ps 2>&1 | ForEach-Object { Write-Host "  $_" }
        foreach ($service in @('backend', 'frontend')) {
            Write-Host "  --- logs: $service ---"
            & $exe @prefix -f $ComposeFile logs --tail 30 $service 2>&1 | ForEach-Object { Write-Host "  $_" }
        }
    }
    catch {
        Write-Host "  diagnostics failed: $($_.Exception.Message)"
    }
}

function Invoke-Up {
    Write-Host '[e2e] bringing up the isolated local stack'
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'SilentlyContinue'
    try {
        if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
            throw 'Docker CLI is not available'
        }
        & docker info --format '{{.ServerVersion}}' *> $null
        if ($LASTEXITCODE -ne 0) { throw 'Docker daemon is not available' }
    }
    finally {
        $ErrorActionPreference = $prev
    }

    if (-not (Test-Path -LiteralPath $SeedDir)) {
        New-Item -ItemType Directory -Path $SeedDir -Force | Out-Null
    }
    Get-ChildItem -LiteralPath $SeedDir -Filter '*.json' -File -ErrorAction SilentlyContinue |
        Remove-Item -Force -ErrorAction SilentlyContinue
    # Regenerate the deterministic seed corpus (5 opencode sessions) from the
    # dataset so the backend has data to ingest on this run. Stdlib only.
    $SeedGen = Join-Path $RepoRoot 'scripts\generate-e2e-seed.py'
    if (Test-Path -LiteralPath $SeedGen) {
        & $PY $SeedGen --target-dir $SeedDir *> $null
    }
    $env:OPENCODE_DATA_DIR = $SeedDir
    $env:E2E_OPPENCODE_DATA_DIR = $SeedDir
    $env:E2E_FRONTEND_URL = $FrontendBase

    if ($Reset) { Invoke-Compose @('down', '-v') }
    Invoke-Compose @('up', '-d', '--no-deps', 'postgres', 'redis')
    $backendArgs = @('up', '-d')
    if ($Force -or $Build) { $backendArgs += '--build' }
    $backendArgs += 'backend'
    Invoke-Compose $backendArgs
    $frontendArgs = @('up', '-d')
    if ($Force -or $Build) { $frontendArgs += '--build' }
    $frontendArgs += 'frontend'
    Invoke-Compose $frontendArgs

    Write-Host "[e2e] waiting for backend/frontend health (max ${StackTimeoutSec}s)"
    $start = Get-Date
    $deadline = $start.AddSeconds([math]::Max(15, $StackTimeoutSec))
    $lastReport = $start.AddSeconds(-10)
    $backendOk = $false
    $frontendOk = $false
    while ((Get-Date) -lt $deadline) {
        $backendOk = Test-Health "$BaseUrl/api/health"
        $frontendOk = Test-Health $FrontendBase
        if ($backendOk -and $frontendOk) {
            Write-Host '[e2e] backend and frontend healthy'
            return
        }
        if (((Get-Date) - $lastReport).TotalSeconds -ge 10) {
            $elapsed = [int]((Get-Date) - $start).TotalSeconds
            Write-Host ("[e2e] waiting {0}s: backend={1} frontend={2}" -f $elapsed, $backendOk, $frontendOk)
            $lastReport = Get-Date
        }
        Start-Sleep -Seconds 2
    }
    Show-Diagnostics
    throw "stack not healthy after ${StackTimeoutSec}s (backend=$backendOk, frontend=$frontendOk)"
}

function Invoke-Down {
    Write-Host '[e2e] tearing down the isolated local stack (docker compose down -v)'
    Invoke-Compose @('down', '-v')
}

# --------------------------------------------------------------------------- #
# run : pytest against the isolated stack
# --------------------------------------------------------------------------- #
function Invoke-Run {
    if (-not (Test-Path -LiteralPath $Conftest)) {
        throw "conftest not found: $Conftest"
    }
    if ($UC -and $UC.Count -gt 0) {
        $ucArgs = @()
        foreach ($u in $UC) { $ucArgs += @('--uc', $u) }
    }
    else { $ucArgs = @() }
    $resultArgs = New-ResultArg
    $env:E2E_OPPENCODE_DATA_DIR = $SeedDir
    $env:E2E_BASE_URL = $BaseUrl
    $env:E2E_FRONTEND_URL = $FrontendBase
    # Keep the isolated local stack (with its seeded data) alive after the
    # test run so subsequent runner invocations reuse the same seeded stack.
    # The stack is torn down only by the explicit `down` command.
    $env:E2E_KEEP_STACK = '1'

    if (-not (Test-Health "$BaseUrl/api/health") -or -not (Test-Health $FrontendBase)) {
        Write-Host '[e2e] stack not healthy -> starting it'
        Invoke-Up
    }
    else {
        Write-Host '[e2e] stack already up'
    }

    $pytestArgs = @('tests/e2e', '-q', '--rootdir=.')
    $pytestArgs += @('-p', 'no:cacheprovider')
    $pytestArgs += $ucArgs + $resultArgs

    if ($Wait) {
        & $PY -m pytest $pytestArgs
        $code = $LASTEXITCODE
        if ($code -eq 0) {
            Write-Host "[e2e] results written to: $Results"
            if (Test-Path -LiteralPath $Results) {
                Write-Host '[e2e] result files:'
                Get-ChildItem -LiteralPath $Results -Filter '*.json' -ErrorAction SilentlyContinue |
                    ForEach-Object { Write-Host ('  ' + $_.Name) }
            }
        }
        return $code
    }

    if (-not (Test-Path -LiteralPath $Monitor)) {
        throw "detached runner not found: $Monitor; use -Wait for a foreground run"
    }
    if (Test-Path -LiteralPath $RunStateFile) {
        $current = & $Monitor status -Name $RunName -StateDir $RunStateDir 2>&1
        if ($LASTEXITCODE -eq 0 -and (($current -join "`n") -match 'state=running')) {
            throw "e2e run '$RunName' is already running; use .\\scripts\\e2e.ps1 status or abort"
        }
    }
    if (-not (Test-Path -LiteralPath $Results)) {
        New-Item -ItemType Directory -Path $Results -Force | Out-Null
    }
    $logPath = Join-Path $Results 'e2e-run.log'
    & $Monitor start -Name $RunName -FilePath $PY -Arguments $pytestArgs -WorkDir $RepoRoot -TimeoutSec $RunTimeoutSec -LogPath $logPath -StateDir $RunStateDir
    if ($LASTEXITCODE -ne 0) {
        throw 'failed to start the detached e2e run'
    }
    Write-Host "[e2e] pytest started detached (name=$RunName)"
    Write-Host '[e2e] status: .\scripts\e2e.ps1 status; abort: .\scripts\e2e.ps1 abort'
    Write-Host "[e2e] log: $logPath"
    return 0
}

function Invoke-Status {
    if (-not (Test-Path -LiteralPath $Monitor)) {
        throw "detached runner not found: $Monitor"
    }
    if (-not (Test-Path -LiteralPath $RunStateFile)) {
        Write-Host '[e2e] no detached e2e run is recorded'
        return 0
    }
    & $Monitor status -Name $RunName -StateDir $RunStateDir
    if ($LASTEXITCODE -ne 0) {
        Write-Host '[e2e] no detached e2e run is recorded'
        return 0
    }
    & $Monitor tail -Name $RunName -Lines 20 -StateDir $RunStateDir
    return 0
}

function Invoke-Abort {
    if (-not (Test-Path -LiteralPath $Monitor)) {
        throw "detached runner not found: $Monitor"
    }
    if (-not (Test-Path -LiteralPath $RunStateFile)) {
        Write-Host '[e2e] no detached e2e run to abort'
        return 0
    }
    & $Monitor stop -Name $RunName -StateDir $RunStateDir
    if ($LASTEXITCODE -ne 0) {
        Write-Host '[e2e] no detached e2e run to abort'
        return 0
    }
    return 0
}

function python_lines([string]$code) {
    $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ("e2e_probe_{0}.py" -f [guid]::NewGuid().ToString('N'))
    $code | Set-Content -LiteralPath $tmp -Encoding UTF8
    try {
        & $PY $tmp
    } finally {
        Remove-Item -LiteralPath $tmp -ErrorAction SilentlyContinue
    }
}

# --------------------------------------------------------------------------- #
# dispatch
# --------------------------------------------------------------------------- #
if (-not $NoExit) {
switch ($Cmd) {
    'list'   { Invoke-List }
    'up'     { Invoke-Up }
    'down'   { Invoke-Down }
    'run'    { exit (Invoke-Run) }
    'status' { exit (Invoke-Status) }
    'abort'  { exit (Invoke-Abort) }
    'all'    {
        Invoke-List
        Write-Host ''
        exit (Invoke-Run)
    }
}
}
