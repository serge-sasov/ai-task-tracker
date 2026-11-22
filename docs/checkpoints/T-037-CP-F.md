# T-037 CP-F: I-02 E2E automation and built-frontend check for S-04

| Field | Value |
|---|---|
| task | T-037 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | coder |
| stage | I-02 |
| artifacts | tests/e2e/uc_e2e_004_006/test_uc_e2e_005_calendar.py, scripts/e2e.ps1, scripts/validate-e2e-data.py, scripts/generate-e2e-seed.py |
| status | awaiting_review |
| requested_at | 2026-11-22T12:39:30 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | 84f9c1a |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-037-CP-F.checks.json |

## Review scope

- Stage `I-02` acceptance for T-037.
- An e2e test for `UC-E2E-005` is added that asserts the S-04 day view (`/calendar/:date`) shows the "Модель" column with the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- The e2e test verifies the actually built frontend (rebuilt image or DOM smoke), not only the API; the stale-frontend failure mode from T-027/T-028 is explicitly guarded (the test fails if the built artifact lacks the column).
- The e2e dataset and validator are updated so the multi-model and empty-model day-view tasks are present and validated (`validate-e2e-data.py` passes).
- The S-01 (UC-E2E-003) e2e test remains green; no existing e2e test is broken by the S-04 addition.
- The e2e runner (`scripts/e2e.ps1`) executes the new S-04 test in an isolated environment with cleanup and does not depend on production.
- `CP-2` (e2e automation and local run) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Elapsed | Usage min | Title |
|---|---|---|---|---|---|---|---|
| C1 | done | d1 | coder | 3 | 1155 | 0 | S-04 model-column e2e + built-frontend DOM smoke (UC-E2E-005) |
| C2 | done | d1 | coder | 1 | 698 | 0 | Seed generator + ModelCell marker guard for S-04 model column (CP-2) |

Ledger: `docs/chunks/T-037.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 4 file(s) |
| artifact-hashes | hash-match | PASS | 4 artifact(s) match ledger |
| e2e-tests-exist | file-exists | PASS | 28 file(s) |
| e2e-runner-exist | file-exists | PASS | 1 file(s) |
| e2e-validator-exist | file-exists | PASS | 1 file(s) |
| e2e-cleanup | pattern-present | PASS | 1 pattern(s) in 28 file(s) |
| e2e-no-production | pattern-absent | PASS | 2 pattern(s) absent in 28 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 2 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 2 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 2 chunk(s) within 80/200 KB |

Summary: 10 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 12 | 2066546 | 534892 | 0 | 47453944 | 23x | 587 | 0.00 |

## Diff stat

```
docs/checkpoints/T-037-CP-2.md    | 2 +-
 docs/plan.md                      | 2 +-
 docs/tasks/T-037-e2e-s04-model.md | 3 ++-
 3 files changed, 4 insertions(+), 3 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint