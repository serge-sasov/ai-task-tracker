# ISSUE-20261119-184132: Shell file-removal cmdlet denied in analyst role

| Field | Value |
|---|---|
| id | ISSUE-20261119-184132 |
| status | open |
| severity | minor |
| type | permission |
| detected_at | 2026-11-19T18:41:32.5173997+03:00 |
| source | T-030 |
| role | harness-analyst |
| location | C:\study\ai\research\ai-task-tracker\docs\chunks\T-030\complete-A1.ps1 |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

Analyst role (and then orchestrator) could not delete an inert helper file left under docs/chunks/T-030/. The opencode permission rule denying the shell file-removal cmdlet blocks all shell file deletion; the role had to finish its chunk with the helper still present.

## Reproduction

1. A role creates a scratch helper script at `docs/chunks/T-030/complete-A1.ps1`.
2. The role (or orchestrator) runs `Remove-Item -LiteralPath docs/chunks/T-030/complete-A1.ps1 -Force` in the shell.
3. The opencode permission engine matches the command against the `*Remove-Item*` deny rule and blocks the call.
4. Re-running with the tokens worded differently (e.g. in a `new-issue.ps1` argument) is also blocked because the matcher scans the whole command line.

## Expected / actual behavior

- Expected: the role can delete the single inert helper file it created, or an orchestrator-only deletion path is available.
- Actual: shell file removal is denied globally; the file is left as residue and must be recorded rather than deleted in-session.

## Cause

opencode bash permission rules deny the shell file-removal cmdlet and recursive shell deletion globally; no dedicated tool exists for deleting a single file, so any stray helper a role creates cannot be cleaned up in-session.

## Solution / workaround

Workaround: record the file as residue in the task card log and the chunk notes; verify it is inert (untracked, not in owns/produced, not referenced by any check) before closing the checkpoint. In this case docs/chunks/T-030/complete-A1.ps1 breaks no A-01 check and is not committed.

## Verification

Not yet verified - status open; to be confirmed by a future run where the file is absent or by an explicit user deletion.

## Prevention / follow-up

Add an orchestrator-only, allow-listed single-file deletion path (or an explicit user deletion), and instruct roles not to drop helper scripts into docs/chunks/<task>/ - use the temp dir for scratch files.

## Links

Task card docs/tasks/T-030-requirements-s04-model.md; chunk ledger docs/chunks/T-030.json; residue file docs/chunks/T-030/complete-A1.ps1

## Timeline

- 2026-11-19T18:41:32.5173997+03:00: detected
