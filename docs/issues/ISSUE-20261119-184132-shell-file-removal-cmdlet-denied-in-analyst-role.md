# ISSUE-20261119-184132: Shell file-removal cmdlet denied in analyst role

| Field | Value |
|---|---|
| id | ISSUE-20261119-184132 |
| status | wontfix |
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

The deny is intentional policy: `harness/process.md` 13 forbids broadening `allow` to `Remove-Item`, and there is no single-file delete tool. The actionable defect is the role creating a scratch helper inside the repository instead of using the OS temp directory.

## Solution / workaround

Wontfix (2026-11-22): the `Remove-Item` deny stays and no allow-listed deletion path will be added. Prevention is procedural: scratch/probe files go to the OS temp directory, never to `scripts/` or `docs/` (`harness/process.md` 13). The existing residue `docs/chunks/T-030/complete-A1.ps1` is inert and untracked; the user removes it manually outside the agent session.

## Verification

- Residue verified inert: untracked, not in T-030 `owns`/`produced`, not referenced by A-01 checks.
- `harness/process.md` 13 now contains the scratch-file prohibition and states that leftovers are removed by the user.
- Manual deletion of the residue remains the only cleanup and is not required by any check.

## Prevention / follow-up

Implemented: process rule "Creating scratch/helper scripts inside the repository to work around a shell permission deny" is prohibited; one-off probes use the OS temp directory. No code or allow-pattern change.

## Links

Task card docs/tasks/T-030-requirements-s04-model.md; chunk ledger docs/chunks/T-030.json; residue file docs/chunks/T-030/complete-A1.ps1

## Timeline

- 2026-11-19T18:41:32.5173997+03:00: detected
- 2026-11-22T18:48:30+03:00: closed as wontfix - deny is intentional; scratch-file prohibition added to process.md; residue left for manual user deletion
