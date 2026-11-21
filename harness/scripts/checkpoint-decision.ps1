#requires -Version 5.1
<#
.SYNOPSIS
Record an explicit user decision for an awaiting harness checkpoint.

.DESCRIPTION
The script is the deterministic writer for user checkpoint decisions. It never
infers approval: the caller must pass the user's explicit -Decision. It updates
the checkpoint fields, the card checkpoint row and status, the board status,
re-renders the derived activity, and commits the decision.

Transitions:
  intermediate approved       -> in_progress
  CP-F approved               -> done
  changes_requested           -> in_progress
  rejected                    -> failed

.EXAMPLE
.\checkpoint-decision.ps1 -Task T-033 -Checkpoint CP-2 -Decision approved
.\checkpoint-decision.ps1 -Task T-033 -Checkpoint CP-F -Decision approved -Remark "accepted"
.\checkpoint-decision.ps1 -Task T-033 -Checkpoint CP-F -Decision changes_requested -Remark "fix the empty state"
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Task,
    [Parameter(Mandatory = $true)][string]$Checkpoint,
    [Parameter(Mandatory = $true)]
    [ValidateSet('approved', 'changes_requested', 'rejected')]
    [string]$Decision,
    [string]$Remark = '',
    [string]$Root = '',
    [switch]$DryRun,
    [switch]$NoCommit
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "harness-lib.ps1")

if (-not $Root) { $Root = Get-HarnessRoot }
$Root = (Resolve-Path -LiteralPath $Root).Path

if ($Task -notmatch '^T-\d{3,}$') { throw "Task must look like T-XXX, got '$Task'" }
$cardPath = Get-CardFile -Root $Root -Task $Task
if (-not $cardPath) { throw "Task card not found for $Task" }

$cpRel = "docs/checkpoints/$Task-$Checkpoint.md"
$cpPath = Join-Path $Root ($cpRel -replace '/', '\')
if (-not (Test-Path -LiteralPath $cpPath)) { throw "checkpoint not found: $cpRel" }

$cpStatus = Get-CheckpointField -Path $cpPath -Name 'status'
if ($cpStatus -ne 'awaiting_review') {
    throw "checkpoint $Checkpoint is '$cpStatus', not awaiting_review"
}

$isFinal = ($Checkpoint -eq 'CP-F')
$newCardStatus = switch ($Decision) {
    'approved' { if ($isFinal) { 'done' } else { 'in_progress' } }
    'changes_requested' { 'in_progress' }
    'rejected' { 'failed' }
}
$newCpStatus = switch ($Decision) {
    'approved' { 'approved' }
    'changes_requested' { 'changes_requested' }
    'rejected' { 'rejected' }
}
$nextAction = switch ($Decision) {
    'approved' { if ($isFinal) { 'task closed; no resume needed' } else { "continue; resume with /harness-run $Task" } }
    'changes_requested' { "return to $Checkpoint after fixes; resume with /harness-run $Task" }
    'rejected' { 'task failed; no further work' }
}

$now = Get-Date
$nowText = $now.ToString('yyyy-MM-ddTHH:mm:ss')
$today = $now.ToString('yyyy-MM-dd')

function Set-CheckpointField {
    param([string]$Path, [string]$Name, [string]$Value)
    $text = Read-Utf8 $Path
    $pattern = "(?m)^\|\s*" + [regex]::Escape($Name) + "\s*\|\s*.*?\s*\|\s*$"
    $replacement = "| $Name | $Value |"
    if ([regex]::IsMatch($text, $pattern)) {
        $text = [regex]::Replace($text, $pattern, $replacement, 1)
        Write-Utf8NoBom $Path $text
    }
}

if ($DryRun) {
    Write-Host "[dry] $Task $Checkpoint decision=$Decision status=$newCpStatus card=$newCardStatus" -ForegroundColor Cyan
    Write-Host "[dry] next: $nextAction" -ForegroundColor Cyan
    exit 0
}

Set-CheckpointField -Path $cpPath -Name 'status' -Value $newCpStatus
Set-CheckpointField -Path $cpPath -Name 'reviewed_at' -Value $nowText
Set-CheckpointField -Path $cpPath -Name 'reviewer' -Value 'user'
Set-CheckpointField -Path $cpPath -Name 'decision' -Value $Decision

if ($Remark) {
    $text = Read-Utf8 $cpPath
    $placeholder = '- (user fills; may be empty if artifacts were edited directly)'
    if ($text.Contains($placeholder)) {
        $text = $text.Replace($placeholder, "- $Remark")
    }
    else {
        $text = $text.TrimEnd() + "`r`n`r`n## Decision remark`r`n`r`n- $Remark`r`n"
    }
    Write-Utf8NoBom $cpPath $text
}

$null = Set-CardField -Path $cardPath -Name 'status' -Value $newCardStatus
$null = Set-CardCheckpointRow -Path $cardPath -Checkpoint $Checkpoint -Status $newCpStatus -ReviewFile $cpRel -Checks ''
$cardText = (Read-Utf8 $cardPath).TrimEnd([char[]]"`r`n") + "`r`n" +
    "- ${today}: ${Checkpoint} decision=${Decision} by user; task ${newCardStatus}" +
    $(if ($Remark) { "; remark: $Remark" } else { '' }) + "`r`n"
Write-Utf8NoBom $cardPath $cardText

$null = Set-BoardRow -Root $Root -Task $Task -Values @{ status = $newCardStatus }
$activity = Sync-Activity -Root $Root

Write-Host "checkpoint: $cpRel" -ForegroundColor Cyan
Write-Host "decision: $Decision ($newCpStatus); task: $newCardStatus" -ForegroundColor Cyan
Write-Host "next: $nextAction" -ForegroundColor Cyan
if ($activity.Rendered) { Write-Host "activity: rendered $($activity.Svg)" -ForegroundColor Green }
else { Write-Host "activity: puml updated, render skipped ($($activity.Reason))" -ForegroundColor Yellow }

if ($NoCommit) {
    Write-Host "commit skipped (-NoCommit)" -ForegroundColor Yellow
    exit 0
}

$cardRel = ConvertTo-ForwardSlash ((Resolve-Path -LiteralPath $cardPath).Path.Substring($Root.TrimEnd('\').Length + 1))
$paths = @($cpRel, 'docs/plan.md', $cardRel, 'docs/activity.puml', 'docs/activity.svg')
$paths = @($paths | Where-Object { Test-Path -LiteralPath (Join-Path $Root ($_ -replace '/', '\')) } | Sort-Object -Unique)
$null = Invoke-Git -Root $Root -Arguments (@('add', '--') + $paths)
$staged = Invoke-Git -Root $Root -Arguments @('diff', '--cached', '--name-only') -AllowFail
if ($staged.Output.Count -eq 0) {
    Write-Host "nothing to commit" -ForegroundColor Yellow
    exit 0
}
$message = "$Task`: $Checkpoint $Decision (user)"
$null = Invoke-Git -Root $Root -Arguments @('commit', '-m', $message)
$head = Invoke-Git -Root $Root -Arguments @('rev-parse', '--short', 'HEAD')
Write-Host "commit: $(([string]$head.Output[0]).Trim())" -ForegroundColor Green
exit 0
