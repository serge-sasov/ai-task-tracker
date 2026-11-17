# ISSUE-20261113-124639: stage-done record step fails on git LF/CRLF warning

| Field | Value |
|---|---|
| id | ISSUE-20261113-124639 |
| status | resolved |
| severity | major |
| type | git |
| detected_at | 2026-11-13T12:46:39.5721565+03:00 |
| source | stage-done.ps1 |
| role | orchestrator |
| location | C:\study\ai\research\ai-task-tracker\harness\scripts\stage-done.ps1 (record step, Invoke-Git at harness/scripts/harness-lib.ps1:335) |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

During T-006 CP-1 and CP-F, stage-done.ps1 committed the snapshot, then terminated at the record step: Invoke-Git treats git's stderr warning 'LF will be replaced by CRLF the next time Git touches it' as a terminating error because ErrorActionPreference=Stop. Checkpoint files and evidence ledger were written, but the snapshot hash was not recorded automatically.

## Reproduction

1. Use a repository with `core.autocrlf=true` and no `.gitattributes` (this project before 2026-11-15).
2. Run `harness/scripts/stage-done.ps1` on a stage whose checkpoint and card files are rewritten by `Write-Utf8NoBom` (UTF-8 without BOM, LF line endings).
3. The record step calls `Invoke-Git add` on those LF files; git writes `warning: in the working copy of '...', LF will be replaced by CRLF the next time Git touches it` to stderr and exits 0.
4. `stage-done.ps1:33` sets `$ErrorActionPreference = "Stop"`, so the old `Invoke-Git` promoted the stderr warning to a terminating error before checking `$LASTEXITCODE`.

## Expected / actual behavior

- Expected: a git call with exit code 0 succeeds regardless of informational stderr; warnings are surfaced but not fatal.
- Actual: `stage-done.ps1` terminated after the snapshot commit, skipped the automatic record commit, and left the snapshot hash to be recorded manually.

## Cause

`Invoke-Git` in `harness-lib.ps1` merged stderr into the output stream via `2>&1` without lowering `$ErrorActionPreference`. In Windows PowerShell 5.1 native stderr becomes an `ErrorRecord`; under `ErrorActionPreference=Stop` that record is a terminating error even when git exits 0.

## Solution / workaround

- Workaround used at detection time: the orchestrator manually staged `docs/checkpoints/T-006-CP-1.md`, `docs/plan.md`, `docs/tasks/T-006-architecture-contracts.md` and committed `T-006: record CP-N snapshot <hash>`; hashes were verified in the checkpoint files, board and card.
- Permanent fix (harness source `24b5eb5`, synced here in `531fa2d`): `Invoke-Git` sets `$ErrorActionPreference = 'Continue'` only around the git call, splits stderr (`ErrorRecord`) from stdout, throws only on a non-zero exit code, and writes exit-0 stderr with `Write-Verbose`. The result object gained `StdErr`; `Output` is now stdout-only.
- Repository policy: `.gitattributes` with `* text=auto eol=lf` was added to both the harness source and this project so harness-written LF files no longer trigger the LF-to-CRLF warning; any remaining CRLF-to-LF warning is visible with `-Verbose`.

## Verification

- `harness/scripts/verify-artifacts.ps1 -SelfTest` reports `Invoke-Git tolerates a CRLF warning under EAP=Stop: PASS`. The case creates a temp repo with `core.autocrlf=true`, adds an LF file under `ErrorActionPreference=Stop`, and asserts exit code 0, the warning in `StdErr`, and no warning in `Output`. Passed in the harness source, the global install (`~/.config/opencode/harness`) and this project's copy.
- `git log` shows 6608194 (CP-1 snapshot), 2c22a5a (CP-1 record), 2b6c829 (CP-F snapshot), d7dad0f (CP-F record); working tree clean; checkpoint `snapshot_commit` fields match.

## Prevention / follow-up

- Regression self-test case added to `verify-artifacts.ps1 -SelfTest` (also runs from `doctor.ps1` via the verification engine self-test).
- stderr is no longer merged into parsed stdout; exit-0 warnings are retained via `Write-Verbose` and the `StdErr` property.
- `.gitattributes` policy added to both repositories; future harness projects could receive the same file from the init template.

## Links

- `docs/tasks/T-006-architecture-contracts.md`; `docs/checkpoints/T-006-CP-1.md`; `docs/checkpoints/T-006-CP-F.md`; `docs/evidence/T-006.md`
- `harness/scripts/harness-lib.ps1:335` (`Invoke-Git`), `harness/scripts/verify-artifacts.ps1` (self-test case)
- Harness repo: `24b5eb5` (harness 2.5.0 source), `harness/scripts/harness-lib.ps1:335`
- This project: `531fa2d` (harness 2.6.0 sync with the fix)

## Timeline

- 2026-11-13T12:46:39.5721565+03:00: detected.
- 2026-11-13T14:25:34+03:00: fixed in the harness source and synced to this project (`531fa2d`).
- 2026-11-15T10:07:33+03:00: regression self-test, stderr separation and `.gitattributes` policy added; status set to resolved.
