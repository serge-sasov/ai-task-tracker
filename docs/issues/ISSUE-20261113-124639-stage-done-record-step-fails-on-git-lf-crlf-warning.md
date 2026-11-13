# ISSUE-20261113-124639: stage-done record step fails on git LF/CRLF warning

| Field | Value |
|---|---|
| id | ISSUE-20261113-124639 |
| status | workaround |
| severity | major |
| type | git |
| detected_at | 2026-11-13T12:46:39.5721565+03:00 |
| source | stage-done.ps1 |
| role | orchestrator |
| location | C:\study\ai\research\ai-task-tracker\harness\scripts\stage-done.ps1 (record step, Invoke-Git at harness/scripts/harness-lib.ps1:341) |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

During T-006 CP-1 and CP-F, stage-done.ps1 committed the snapshot, then terminated at the record step: Invoke-Git treats git's stderr warning 'LF will be replaced by CRLF the next time Git touches it' as a terminating error because ErrorActionPreference=Stop. Checkpoint files and evidence ledger were written, but the snapshot hash was not recorded automatically.

## Reproduction

1. 
2. 
3. 

## Expected / actual behavior

- Expected:
- Actual:

## Cause

Invoke-Git in harness-lib.ps1 does not distinguish git informational stderr (CRLF warnings) from real errors; a successful exit code with stderr output becomes a terminating PowerShell error under ErrorActionPreference=Stop.

## Solution / workaround

Workaround: after the failed record step, the orchestrator manually staged docs/checkpoints/T-006-CP-1.md, docs/plan.md, docs/tasks/T-006-architecture-contracts.md and committed 'T-006: record CP-N snapshot <hash>'; verified hashes in the checkpoint files, board and card. Script fix is a follow-up in the harness source.

## Verification

git log shows 6608194 (CP-1 snapshot), 2c22a5a (CP-1 record), 2b6c829 (CP-F snapshot), d7dad0f (CP-F record); working tree clean; checkpoint snapshot_commit fields match.

## Prevention / follow-up

Fix Invoke-Git to ignore exit-code-0 git warnings on stderr (or run git with -c core.safecrlf=false / capture stderr only on non-zero exit); add a doctor/self-test case for a CRLF warning. Follow-up task in the global harness source, not this project.

## Links

docs/tasks/T-006-architecture-contracts.md; docs/checkpoints/T-006-CP-1.md; docs/checkpoints/T-006-CP-F.md; docs/evidence/T-006.md

## Timeline

- 2026-11-13T12:46:39.5721565+03:00: detected
