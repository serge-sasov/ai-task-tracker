# T-038 CP-F: Q-01 E2E plan and coverage for S-04 model column

| Field | Value |
|---|---|
| task | T-038 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | qa |
| stage | Q-01 |
| artifacts | docs/qa/e2e-plan-s04-model.md |
| status | awaiting_review |
| requested_at | 2026-11-22T14:59:00 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | - |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-038-CP-F.checks.json |

## Review scope

- Stage `Q-01` acceptance for T-038.
- `docs/qa/e2e-plan-s04-model.md` contains a requirement -> use case -> test matrix covering `R-015`, `UC-E2E-005`, and the S-04 e2e test(s), with the expected result for each cell state (primary, `+N`, full list, "Нет данных").
- The plan states the test data is validated by `validate-e2e-data.py` and includes the multi-model and empty-model day-view tasks.
- The plan states the execution approach: the check runs against the actually built frontend (rebuilt image or DOM smoke), and the stale-frontend failure mode from T-027/T-028 is a covered risk.
- The plan traces every `R-015` acceptance item to at least one test and lists any gap as an open item with a reason (no silent skip).
- The plan reuses the S-01 (T-026) plan structure and does not duplicate the S-01 coverage; the S-04 scope is the delta.
- `CP-1` (e2e plan and coverage) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Elapsed | Usage min | Title |
|---|---|---|---|---|---|---|---|
| C1 | done | d1 | qa | 1 | 17 | 0 | Q-01 e2e plan + coverage matrix for S-04 model column (R-015/UC-E2E-005) |

Ledger: `docs/chunks/T-038.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 1 file(s) |
| artifact-hashes | hash-match | PASS | 1 artifact(s) match ledger |
| e2e-plan-exist | file-exists | PASS | 2 file(s) |
| e2e-plan-matrix | pattern-present | PASS | 2 pattern(s) in 1 file(s) |
| e2e-plan-uc-coverage | id-coverage | PASS | 8 id(s) covered by 2 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 1 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 1 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 1 chunk(s), authored-delta |

Summary: 8 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 4 | 166571 | 24297 | 0 | 1283572 | 7.7x | 35 | 0.00 |

## Diff stat

```
docs/activity.puml                         |   2 +-
 docs/activity.svg                          |   2 +-
 docs/checkpoints/T-038-CP-1.checks.json    |  68 +++++++++++++
 docs/checkpoints/T-038-CP-1.md             |  86 +++++++++++++++++
 docs/checkpoints/T-038-CP-1.preflight.json |  16 ++++
 docs/chunks/T-038.json                     | 105 ++++++++++++++++++++
 docs/chunks/T-038/C1.brief.md              |  59 ++++++++++++
 docs/evidence/T-038.md                     |   7 ++
 docs/plan.md                               |   2 +-
 docs/qa/e2e-plan-s04-model.md              | 148 +++++++++++++++++++++++++++++
 docs/tasks/T-038-e2e-plan-s04-model.md     |  20 ++--
 11 files changed, 505 insertions(+), 10 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint