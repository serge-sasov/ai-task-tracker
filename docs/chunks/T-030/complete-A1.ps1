$paths = @(
  'docs/analysis/requirements-ai-task-tracker.md',
  'docs/analysis/glossary.md',
  'docs/analysis/questions-ai-task-tracker.md'
)
& (Join-Path (Get-Location) 'harness\scripts\chunk.ps1') complete -Task T-030 -Id A1 `
  -Produced $paths `
  -Note 'R-015 added (S-04 day-view model column), glossary extended, questions consistent; verify-stage A-01: 8 PASS / 0 FAIL'
if ($?) {
  & (Join-Path (Get-Location) 'harness\scripts\chunk.ps1') status -Task T-030
}
