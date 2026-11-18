# T-024 CP-F: I-01 Implementation and unit tests: S-01 model column

| Field | Value |
|---|---|
| task | T-024 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | coder |
| stage | I-01 |
| artifacts | src/main/java/ru/aitasktracker/api/dto/TaskListItem.java, src/main/java/ru/aitasktracker/api/service/TaskQueryService.java, tests/java/ru/aitasktracker/api/service/TaskQueryServiceTest.java, src/web/src/api/types.ts, src/web/src/views/DashboardView.vue |
| status | approved |
| requested_at | 2026-11-18T14:09:05 |
| reviewed_at | 2026-11-18T14:09:05 |
| reviewer | auto (explicit -AutoApprove) |
| decision | approved |
| snapshot_commit | - |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-024-CP-F.checks.json |

## Review scope

- Stage `I-01` acceptance for T-024.
- `TaskListItem.models` is ordered by total tokens (input+output+cache) descending, then model name ascending (contracts §4.2, R-014).
- `models` is derived from the same `token_usage` rows used for `tokens_total`; no extra query per task (reuse the existing rows).
- An empty `models` array means no usage → the S-01 cell shows «Нет данных».
- Frontend column is placed after «Название», shows the primary model + `+N` (overflow count), `title`/`aria` carry the full model list, and «Нет данных» when the list is empty (T-021 UI spec).
- Unit tests cover ordering, ties (same total → name asc), and the empty (no-usage) case.
- Targeted `TaskQueryServiceTest` 15/15 passes and `npm run build` in `src/web` passes; full backend suite has 5 pre-existing failures reproduced at base `d4dde64` and tracked in BUG-007.
- Chunks C1 and C2 are `done` with produced hashes in `docs/chunks/T-024.json`.
- No unrelated changes.
- `CP-1` (implementation and unit tests) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | coder | 3 | Backend: ordered models field and TaskQueryService tests |
| C2 | done | d1 | coder | 2 | Frontend: S-01 model column (primary +N, tooltip/aria, empty state) |

Ledger: `docs/chunks/T-024.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 5 file(s) |
| artifact-hashes | hash-match | PASS | 5 artifact(s) match ledger |
| src-exist | file-exists | PASS | 118 file(s) |
| tests-exist | file-exists | PASS | 50 file(s) |
| no-todo | pattern-absent | PASS | 3 pattern(s) absent in 5 file(s) |
| no-secrets | pattern-absent | PASS | 3 pattern(s) absent in 5 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 2 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 2 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 2 chunk(s) within 32/64 KB |

Summary: 9 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 1 | 82511 | 17279 | 0 | 2997680 | 36.3x | 10 | 0.00 |

## Diff stat

```
docs/activity.puml                                 |   2 +-
 docs/activity.svg                                  |   2 +-
 .../BUG-007-pre-existing-backend-test-failures.md  |  87 ++++++++++++++++
 docs/bugs/README.md                                |   1 +
 docs/checkpoints/T-024-CP-1.checks.json            |  74 +++++++++++++
 docs/checkpoints/T-024-CP-1.md                     |  92 +++++++++++++++++
 docs/chunks/T-024.json                             | 115 +++++++++++++++++++++
 docs/evidence/T-024.md                             |  11 ++
 docs/plan.md                                       |   2 +-
 docs/tasks/T-024-implement-model-column.md         |  57 ++++++++++
 .../ru/aitasktracker/api/dto/TaskListItem.java     |  27 ++++-
 .../api/service/TaskQueryService.java              |  42 ++++++--
 src/web/src/api/types.ts                           |   1 +
 src/web/src/views/DashboardView.vue                |  37 ++++++-
 .../api/service/TaskQueryServiceTest.java          |  95 +++++++++++++++++
 15 files changed, 632 insertions(+), 13 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- accept task