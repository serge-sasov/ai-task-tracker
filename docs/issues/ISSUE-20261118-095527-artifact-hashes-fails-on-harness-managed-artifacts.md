# ISSUE-20261118-095527: artifact-hashes fails on harness-managed artifacts at CP-F

| Field | Value |
|---|---|
| id | ISSUE-20261118-095527 |
| status | resolved |
| severity | major |
| type | tool |
| detected_at | 2026-11-18T09:55:27+03:00 |
| source | T-017 CP-F |
| role | harness-orchestrator |
| location | C:\study\ai\research\ai-task-tracker\harness\scripts\verify-artifacts.ps1 (Check-ArtifactHashes) |
| environment | Windows PowerShell 5.1; local-harness 3.0.0 |

## Symptoms

`verify-stage.ps1 -Task T-017 -Stage P-00` passed at CP-1, but the CP-F verification failed with:

```
[FAIL] artifact-hashes  docs/activity.puml (hash changed); docs/activity.svg (hash changed); docs/plan.md (hash changed); docs/tasks/T-017-task-list-model-planning.md (hash changed)
```

The checkpoint could not be closed because `FAIL=1`.

## Reproduction

1. Run `stage-done.ps1 -Task T-017 -Checkpoint CP-1`; the ledger records hashes before the script updates the card, board and activity.
2. Run `verify-stage.ps1 -Task T-017 -Stage P-00`; `artifact-hashes` compares the current card/plan/activity against the CP-1 hashes and fails.
3. The same failure occurs even without manual edits, because `stage-done.ps1` changes the card status, board row and activity after writing the ledger.

## Expected / actual behavior

- Expected: a final checkpoint can be closed after the harness itself updates status/plan/activity; hash verification should cover immutable acceptance artifacts, not harness-managed status files.
- Actual: the universal `artifact-hashes` check treats `docs/plan.md`, the task card and `docs/activity.*` as immutable and blocks every CP-F for P-00.

## Cause

`Check-ArtifactHashes` compared every ledger path, including files that `stage-done.ps1` and `board-update.ps1` intentionally rewrite after the ledger is written. `docs/plan.md`, `docs/tasks/T-*.md`, `docs/activity.puml`, `docs/activity.svg`, `docs/checkpoints/*` and `docs/evidence/*` are harness-managed; their hashes cannot remain stable across checkpoints.

## Solution / workaround

`Check-ArtifactHashes` now skips harness-managed paths and returns `NOT_APPLICABLE` when the ledger contains only such paths. Added the self-test case `hash-match skips harness-managed`. Immutable artifacts are still hash-checked; `hash-match changed` continues to FAIL.

## Verification

- `verify-artifacts.ps1 -SelfTest`: `SELF-TEST PASSED`, including `hash-match changed: FAIL` and `hash-match skips harness-managed: NOT_APPLICABLE`.
- `verify-stage.ps1 -Task T-017 -Stage P-00`: `5 PASS, 0 FAIL, 0 MANUAL, 1 N/A, 0 UNCHECKED`.
- `stage-done.ps1 -Task T-017 -Checkpoint CP-F -AutoApprove`: approved; commit `5a28f17`.

## Prevention / follow-up

The universal hash check now distinguishes immutable deliverables from harness-managed status files. When adding new harness-managed paths, extend the skip list in `Check-ArtifactHashes` and add a self-test case.

## Links

T-017 P-00/CP-F; `harness/scripts/verify-artifacts.ps1`; commit to follow.

## Timeline

- 2026-11-18T09:55:27+03:00: detected during T-017 CP-F verification.
- 2026-11-18T09:55:27+03:00: fixed, self-tested and used to close T-017 CP-F.
