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
    up      docker compose up -d --build (isolated local stack)
    down    docker compose down -v (isolated local stack)
    run     bring up the stack if down, run `python -m pytest tests/e2e`,
            results go to tests/e2e/results/
    all     list then run (default)

.PARAMETER UC
    Restrict the run to one or more UC-E2E-NNN ids (repeatable).

.PARAMETER ResultsDir
    Override the JSON results directory (default: tests/e2e/results).

.EXAMPLE
    .\scripts\e2e.ps1 list
    .\scripts\e2e.ps1 up
    .\scripts\e2e.ps1 down
    .\scripts\e2e.ps1 run -UC UC-E2E-001 -UC UC-E2E-002
    .\scripts\e2e.ps1 all
#>
[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet('list','up','down','run','all')]
    [string]$Cmd = 'all',

    [string[]]$UC,

    [string]$ResultsDir = '',

    [switch]$Force
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
function Invoke-Compose([string[]]$Args) {
    docker compose -f $ComposeFile @Args
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose ${Args -join ' '} failed (exit $LASTEXITCODE)"
    }
}

function Invoke-Up {
    Write-Host "[e2e] bringing up the isolated local stack (docker compose up -d --build)"
    # Deterministic seed source: clear it and point both the stack and the tests
    # at the same dedicated directory.
    if (-not (Test-Path -LiteralPath $SeedDir)) {
        New-Item -ItemType Directory -Path $SeedDir -Force | Out-Null
    }
    Get-ChildItem -LiteralPath $SeedDir -Filter '*.json' -File -ErrorAction SilentlyContinue |
        Remove-Item -Force -ErrorAction SilentlyContinue
    $env:OPENCODE_DATA_DIR = $SeedDir
    $env:E2E_OPPENCODE_DATA_DIR = $SeedDir
    # Clean slate first so the DB starts empty and deterministic.
    Invoke-Compose @('down','-v')
    Invoke-Compose @('up','-d','--no-deps','postgres','redis')
    Invoke-Compose @('up','-d','backend') + $(if ($Force) { @('--build') } else { @() })
    Write-Host "[e2e] waiting for backend health"
    $deadline = (Get-Date).AddMinutes(15)
    do {
        try {
            $body = Invoke-WebRequest -Uri "$BaseUrl/api/health"
                -UseBasicParsing -TimeoutSec 5
            if ($body.StatusCode -eq 200) {
                Write-Host "[e2e] backend healthy"
                return
            }
        } catch { Start-Sleep -Seconds 5 }
    } while ((Get-Date) -lt $deadline)
    Write-Host "[e2e] WARN: backend not healthy yet; tests will retry the wait" -ForegroundColor Yellow
}

function Invoke-Down {
    Write-Host "[e2e] tearing down the isolated local stack (docker compose down -v)"
    Invoke-Compose @('down','-v')
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
    } else { $ucArgs = @() }
    $resultArgs = New-ResultArg
    $env:E2E_OPPENCODE_DATA_DIR = $SeedDir

    # Ensure the stack is up.
    try {
        $health = Invoke-WebRequest -Uri "$BaseUrl/api/health"
            -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        if ($health.StatusCode -ne 200) { throw 'not healthy' }
        Write-Host "[e2e] stack already up"
    } catch {
        Write-Host "[e2e] stack not up -> starting it"
        try {
            Invoke-Up
        } catch {
            Write-Warning "[e2e] stack could not be started: $($_.Exception.Message)"
            Write-Warning "[e2e] continuing to pytest; stack-dependent tests will skip/error and results will still be recorded"
        }
    }

    # Run pytest with the repo root on sys.path and rootdir=.
    $pytestArgs = @('tests\e2e', '-q', '--rootdir=.')
    $pytestArgs += @('-p', 'no:cacheprovider')
    $pytestArgs += $ucArgs + $resultArgs

    & $PY -m @pytestArgs
    $code = $LASTEXITCODE
    if ($code -eq 0) {
        Write-Host "[e2e] results written to: $Results"
        if (Test-Path -LiteralPath $Results) {
            Write-Host "[e2e] result files:"
            Get-ChildItem -LiteralPath $Results -Filter '*.json' -ErrorAction SilentlyContinue |
                ForEach-Object { Write-Host ("  " + $_.Name) }
        }
    }
    exit $code
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
switch ($Cmd) {
    'list' { Invoke-List }
    'up'   { Invoke-Up }
    'down' { Invoke-Down }
    'run'  { Invoke-Run }
    'all'  {
        Invoke-List
        Write-Host ''
        Invoke-Run
    }
}
