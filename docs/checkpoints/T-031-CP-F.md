# T-031 CP-F: A-02 E2E use cases and dataset delta for S-04 model column

| Field | Value |
|---|---|
| task | T-031 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | analyst |
| stage | A-02 |
| artifacts | docs/analysis/e2e-use-cases-ai-task-tracker.md, docs/analysis/e2e-test-data-ai-task-tracker.yaml |
| status | awaiting_review |
| requested_at | 2026-11-19T23:48:16 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | f59ae41 |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-031-CP-F.checks.json |

## Review scope

- Stage `A-02` acceptance for T-031.
- `UC-E2E-005` is extended so the S-04 day view (`/calendar/:date`) is asserted to show a "Модель" column after "Задача", with the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- The YAML dataset `e2e-test-data-ai-task-tracker.yaml` is extended with at least one task carrying multiple `models` (to exercise `+N`) and one task with an empty `models` list (to exercise "Нет данных"), for the day view.
- `UC-E2E-005` traces to `R-015` and reuses the S-01 ordering/tie-break expectation (total tokens desc, name tie-break); no duplicate use case is introduced.
- The scenario explicitly states the check runs against the actually built frontend (rebuilt image or DOM smoke), not only the API, and does not change the S-01 (UC-E2E-003) scenario.
- The e2e scenario is validatable by the existing runner/validator and the dataset passes `validate-e2e-data.py`.
- `CP-2` (e2e use cases and dataset) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| B1 | done | d1 | analyst | 1 | UC-E2E-005 S-04 model column markdown extension |
| B2 | done | d1 | analyst | 1 | YAML dataset day-view model extensions + validation pass |

Ledger: `docs/chunks/T-031.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 2 file(s) |
| artifact-hashes | hash-match | PASS | 2 artifact(s) match ledger |
| e2e-use-cases-exist | file-exists | PASS | 1 file(s) |
| e2e-yaml-exist | file-exists | PASS | 1 file(s) |
| e2e-yaml-valid | python-validator | PASS | 1 file(s) |
| uc-ids | ids-unique | PASS | 8 unique id(s) |
| requirement-coverage | id-coverage | PASS | 26 id(s) covered by 2 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 2 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 2 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 2 chunk(s) within 48/48 KB |

Summary: 10 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 3 | 281084 | 60015 | 0 | 6797571 | 24.2x | 282 | 0.00 |

## Diff stat

```
docs/analysis/e2e-test-data-ai-task-tracker.yaml |  48 ++++++++++-
 docs/analysis/e2e-use-cases-ai-task-tracker.md   |  19 +++--
 docs/checkpoints/T-031-CP-2.checks.json          |  80 ++++++++++++++++++
 docs/checkpoints/T-031-CP-2.md                   |  89 ++++++++++++++++++++
 docs/chunks/T-031.json                           | 101 +++++++++++++++++++++++
 docs/chunks/T-031/B1.brief.md                    |  60 ++++++++++++++
 docs/chunks/T-031/B2.brief.md                    |  55 ++++++++++++
 docs/evidence/T-031.md                           |   8 ++
 docs/plan.md                                     |   2 +-
 docs/tasks/T-031-e2e-s04-model.md                |  15 ++--
 10 files changed, 460 insertions(+), 17 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint