#requires -Version 5.1
<#
.SYNOPSIS
Report harness time signals per task: authoring, review wait, sessions, usage.

.DESCRIPTION
Reads task cards, checkpoint files, checks JSON, and chunk ledgers. Work time and
calendar time remain in checkpoint/activity data; this report fills the gaps that
the harness can measure deterministically: chunk authoring time (elapsed_min or
started/finished), review waits, sessions/input/cache/minutes, attempts, produced
files, and chunk-budget failures.

.EXAMPLE
.\time-report.ps1 -Task T-033
.\time-report.ps1 -All -Markdown -OutFile docs/metrics/harness-time.md
.\time-report.ps1 -All -Json
#>
[CmdletBinding()]
param(
    [string]$Task = '',
    [switch]$All,
    [string]$Root = '',
    [switch]$Json,
    [switch]$Markdown,
    [string]$OutFile = ''
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot "harness-lib.ps1")
if (-not $Root) { $Root = Get-HarnessRoot }
$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not $Task -and -not $All) { throw 'Specify -Task T-XXX or -All' }

function Minutes([datetime]$a, [datetime]$b) {
    if ($null -eq $a -or $null -eq $b) { return $null }
    [math]::Max(0, [math]::Round(($b - $a).TotalMinutes))
}

function Parse-Date($value) {
    if (-not $value -or $value -eq '-') { return $null }
    try { [datetime]::Parse([string]$value) } catch { $null }
}

function Get-CardInfo([string]$id) {
    $path = Get-CardFile -Root $Root -Task $id
    if (-not $path) { return $null }
    $fields = Get-CardFields -Path $path
    [pscustomobject]@{
        Path = $path
        Stage = if ($fields.ContainsKey('stage')) { ($fields['stage'] -split '/')[0].Trim() } else { '?' }
        Role = if ($fields.ContainsKey('role')) { $fields['role'].Trim() } else { '?' }
        Status = if ($fields.ContainsKey('status')) { $fields['status'].Trim() } else { '?' }
        Scope = if ($fields.ContainsKey('scope_class')) { $fields['scope_class'].Trim() } else { '-' }
    }
}

function Get-ChunkStats([string]$id) {
    $ledgerPath = Join-Path $Root "docs\chunks\$id.json"
    $result = [pscustomobject]@{ Chunks = 0; Produced = 0; Attempts = 0; AuthoringMin = 0; Sessions = 0 }
    if (-not (Test-Path -LiteralPath $ledgerPath)) { return $result }
    $ledger = (Read-Utf8 $ledgerPath) | ConvertFrom-Json
    $chunks = @($ledger.chunks)
    $authoring = 0.0
    $sessions = 0
    $produced = 0
    $attempts = 0
    foreach ($c in $chunks) {
        $produced += @($c.produced).Count
        $attempts += [int]($c.attempts)
        if ($c.PSObject.Properties['sessions'] -and $c.sessions) { $sessions += [int]$c.sessions }
        if ($c.PSObject.Properties['elapsed_min'] -and $null -ne $c.elapsed_min -and [double]$c.elapsed_min -gt 0) {
            $authoring += [double]$c.elapsed_min
        }
        else {
            $start = Parse-Date $c.started
            $finish = Parse-Date $c.finished
            if ($start -and $finish) { $authoring += [double](Minutes $start $finish) }
        }
    }
    [pscustomobject]@{
        Chunks = $chunks.Count
        Produced = $produced
        Attempts = $attempts
        AuthoringMin = [math]::Round($authoring)
        Sessions = $sessions
    }
}

function Get-CpStats([string]$id) {
    $files = @(Get-ChildItem -LiteralPath (Join-Path $Root 'docs\checkpoints') -Filter "$id-CP-*.md" -File -ErrorAction SilentlyContinue | Sort-Object Name)
    $review = 0.0
    $requests = @()
    $reviews = @()
    $usage = $null
    $budgetFail = 0
    foreach ($f in $files) {
        $text = Read-Utf8 $f.FullName
        $req = $null
        $rev = $null
        $rm = [regex]::Match($text, '(?m)^\|\s*requested_at\s*\|\s*(.*?)\s*\|')
        if ($rm.Success) { $req = Parse-Date $rm.Groups[1].Value; if ($req) { $requests += $req } }
        $rm = [regex]::Match($text, '(?m)^\|\s*reviewed_at\s*\|\s*(.*?)\s*\|')
        if ($rm.Success) { $rev = Parse-Date $rm.Groups[1].Value; if ($rev) { $reviews += $rev } }
        if ($req -and $rev) { $review += [double](Minutes $req $rev) }

        $um = [regex]::Match($text, '(?s)## Usage\s*\n+\|\s*Sessions\s*\|.*?\n\|[-| ]+\|\s*\n\|([^\n]+)\|')
        if ($um.Success) {
            $cells = @($um.Groups[1].Value.Split('|') | ForEach-Object { $_.Trim() })
            if ($cells.Count -ge 7 -and $cells[0] -match '^\d+$') {
                $u = [pscustomobject]@{
                    Sessions = [int]$cells[0]; Input = [int]$cells[1]; Output = [int]$cells[2]
                    Cache = [int]$cells[4]; UsageMin = [double]$cells[6]
                }
                if (-not $usage -or $u.Sessions -ge $usage.Sessions) { $usage = $u }
            }
        }
        $checksPath = [System.IO.Path]::ChangeExtension($f.FullName, '.checks.json')
        if (Test-Path -LiteralPath $checksPath) {
            $checks = (Read-Utf8 $checksPath) | ConvertFrom-Json
            $budgetFail += @($checks.results | Where-Object { $_.id -eq 'chunk-budget' -and $_.result -eq 'FAIL' }).Count
        }
    }
    $span = if ($requests.Count -gt 0 -and $reviews.Count -gt 0) {
        [double](Minutes ($requests | Measure-Object -Minimum).Minimum ($reviews | Measure-Object -Maximum).Maximum)
    } else { $null }
    [pscustomobject]@{
        Checkpoints = $files.Count
        ReviewWaitMin = [math]::Round($review)
        CpSpanMin = $span
        Sessions = if ($usage) { $usage.Sessions } else { 0 }
        Input = if ($usage) { $usage.Input } else { 0 }
        Cache = if ($usage) { $usage.Cache } else { 0 }
        UsageMin = if ($usage) { [math]::Round($usage.UsageMin) } else { 0 }
        BudgetFail = $budgetFail
    }
}

$ids = if ($All) {
    @(Get-ChildItem -LiteralPath (Join-Path $Root 'docs\tasks') -Filter 'T-*.md' -File |
        ForEach-Object { if ($_.BaseName -match '^(T-\d{3})') { $Matches[1] } } |
        Where-Object { $_ } | Sort-Object -Unique)
} else { @($Task) }

$rows = @()
foreach ($id in $ids) {
    $card = Get-CardInfo $id
    if (-not $card) { continue }
    $c = Get-ChunkStats $id
    $cp = Get-CpStats $id
    $rows += [pscustomobject]@{
        Task = $id; Stage = $card.Stage; Role = $card.Role; Status = $card.Status; Scope = $card.Scope
        Chunks = $c.Chunks; Produced = $c.Produced; Attempts = $c.Attempts
        AuthoringMin = $c.AuthoringMin; ReviewWaitMin = $cp.ReviewWaitMin; CpSpanMin = $cp.CpSpanMin
        Sessions = if ($cp.Sessions) { $cp.Sessions } else { $c.Sessions }
        Input = $cp.Input; Cache = $cp.Cache; UsageMin = $cp.UsageMin; BudgetFail = $cp.BudgetFail
    }
}

if ($Json) {
    $rows | ConvertTo-Json -Depth 6
    exit 0
}

function Format-Min($m) { if ($null -eq $m) { '-' } elseif ($m -lt 60) { "$m min" } else { $h=[math]::Floor($m/60); $r=$m%60; if ($r) { "$h h $r min" } else { "$h h" } } }
$lines = New-Object System.Collections.Generic.List[string]
$lines.Add('# Harness time signals')
$lines.Add('')
$lines.Add("Generated: $(Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')")
$lines.Add('Harness-measurable signals; work/calendar remain in checkpoint/task history.')
$lines.Add('')
$lines.Add('| Task | Stage | Role | Status | Scope | Chunks | Produced | Attempts | Authoring | Review wait | CP span | Sessions | Input | Cache | Usage min | Budget FAIL |')
$lines.Add('|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|')
foreach ($r in $rows) {
    $lines.Add(('| {0} | {1} | {2} | {3} | {4} | {5} | {6} | {7} | {8} | {9} | {10} | {11} | {12} | {13} | {14} | {15} |' -f `
        $r.Task, $r.Stage, $r.Role, $r.Status, $r.Scope, $r.Chunks, $r.Produced, $r.Attempts,
        (Format-Min $r.AuthoringMin), (Format-Min $r.ReviewWaitMin), (Format-Min $r.CpSpanMin),
        $r.Sessions, $r.Input, $r.Cache, (Format-Min $r.UsageMin), $r.BudgetFail))
}
$text = ($lines -join "`r`n") + "`r`n"
if ($OutFile) {
    $path = if ([System.IO.Path]::IsPathRooted($OutFile)) { $OutFile } else { Join-Path $Root $OutFile }
    $dir = Split-Path -Parent $path
    if (-not (Test-Path -LiteralPath $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
    Write-Utf8NoBom $path $text
    Write-Host "markdown: $path"
}
else { Write-Output $text }
