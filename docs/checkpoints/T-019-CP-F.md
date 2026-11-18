# T-019 CP-F: A-02 E2E use cases and dataset: S-01 model column

| Field | Value |
|---|---|
| task | T-019 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | analyst |
| stage | A-02 |
| artifacts | docs/analysis/e2e-use-cases-ai-task-tracker.md, docs/analysis/e2e-test-data-ai-task-tracker.yaml |
| status | approved |
| requested_at | 2026-11-18T10:03:21 |
| reviewed_at | 2026-11-18T10:03:21 |
| reviewer | auto (explicit -AutoApprove) |
| decision | approved |
| snapshot_commit | 2d0ed43 |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-019-CP-F.checks.json |

## Review scope

- Stage `A-02` acceptance for T-019.
- UC-E2E-003 has R-014 in its source requirements and a step-11 assertion for the visible model column, `+N`, tooltip/aria full list and "Нет данных".
- The coverage summary includes R-014 and the totals are updated to 31 requirements.
- The YAML dataset includes R-014 in UC-E2E-003 and an S8 step with the per-task ordered model lists.
- `python scripts/validate-e2e-data.py ... --use-cases-md ... --strict` passes with 0 errors.
- T-018's R-014 wording and the T-017 contract remain unchanged.
- Chunks C1 and C2 are `done` with produced hashes in `docs/chunks/T-019.json`.
- `CP-2` (e2e use cases and dataset) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | analyst | 1 | UC-E2E-003 model-column use case |
| C2 | done | d1 | analyst | 1 | YAML dataset S8 model assertions |

Ledger: `docs/chunks/T-019.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 2 file(s) |
| artifact-hashes | hash-match | PASS | 2 artifact(s) match ledger |
| e2e-use-cases-exist | file-exists | PASS | 1 file(s) |
| e2e-yaml-exist | file-exists | PASS | 1 file(s) |
| e2e-yaml-valid | python-validator | PASS | 1 file(s) |
| uc-ids | ids-unique | PASS | 8 unique id(s) |
| requirement-coverage | id-coverage | PASS | 25 id(s) covered by 2 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 2 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 2 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 2 chunk(s) within 64/96 KB |

Summary: 10 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

- token usage not available (opencode CLI or database unavailable)

## Diff stat

```
docs/activity.puml                               |   2 +-
 docs/activity.svg                                |   2 +-
 docs/analysis/e2e-test-data-ai-task-tracker.yaml |  18 +++-
 docs/analysis/e2e-use-cases-ai-task-tracker.md   |  18 ++--
 docs/checkpoints/T-019-CP-2.checks.json          |  80 ++++++++++++++++++
 docs/checkpoints/T-019-CP-2.md                   |  82 +++++++++++++++++++
 docs/chunks/T-019.json                           | 100 +++++++++++++++++++++++
 docs/evidence/T-019.md                           |   8 ++
 docs/plan.md                                     |   2 +-
 docs/tasks/T-019-e2e-model-column.md             |  54 ++++++++++++
 10 files changed, 353 insertions(+), 13 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- accept task