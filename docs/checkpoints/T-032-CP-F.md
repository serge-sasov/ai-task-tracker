# T-032 CP-F: U-01 UX flows and screens for S-04 model column

| Field | Value |
|---|---|
| task | T-032 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | ux-designer |
| stage | U-01 |
| artifacts | docs/ux/user-flows-ai-task-tracker.md, docs/ux/screens-ai-task-tracker.md |
| status | awaiting_review |
| requested_at | 2026-11-20T01:30:15 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | 88742ba |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-032-CP-F.checks.json |

## Review scope

- Stage `U-01` acceptance for T-032.
- `docs/ux/screens-ai-task-tracker.md` is extended with the S-04 day view screen inventory and the new "Модель" column, including the four states: primary model only, primary + `+N`, tooltip/`aria-label` full ordered list, and "Нет данных".
- `docs/ux/user-flows-ai-task-tracker.md` is extended with the S-04 day-view journey (open `/calendar/:date` -> view day table -> read the model column) and traces to UC-E2E-005 and `R-015`.
- The S-04 screen states (success / loading / empty / error) are documented, and the model-cell states reuse the S-01 (S-01 list view) terminology and labels ("Модель", "Нет данных").
- The screen inventory is consistent with the existing S-01 model-column UX (T-020) so the same cell rules apply in both views; no conflicting state is introduced.
- The UX doc explicitly states the day view is read-only and the model column does not add new interactions beyond the S-01 tooltip/aria behavior.
- `CP-1` (UX flows and screens) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | ux-designer | 1 | S-04 day-view journey in user-flows (R-015, UC-E2E-005) |
| C2 | done | d1 | ux-designer | 1 | S-04 Модель column + states in screens (R-015, reuse S-01 labels) |

Ledger: `docs/chunks/T-032.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 2 file(s) |
| artifact-hashes | hash-match | PASS | 2 artifact(s) match ledger |
| user-flows-exist | file-exists | PASS | 1 file(s) |
| screens-exist | file-exists | PASS | 1 file(s) |
| screen-ids | ids-unique | PASS | 6 unique id(s) |
| flows-requirements | pattern-present | PASS | 1 pattern(s) in 1 file(s) |
| screens-states | pattern-present | PASS | 4 pattern(s) in 1 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 2 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 2 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 2 chunk(s) within 48/48 KB |

Summary: 10 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 5 | 240623 | 35379 | 0 | 2662219 | 11.1x | 93 | 0.00 |

## Diff stat

```
docs/activity.puml                      |   2 +-
 docs/activity.svg                       |   2 +-
 docs/checkpoints/T-032-CP-1.checks.json |  80 ++++++++++++++++++++++
 docs/checkpoints/T-032-CP-1.md          |  83 +++++++++++++++++++++++
 docs/chunks/T-032.json                  | 113 ++++++++++++++++++++++++++++++++
 docs/evidence/T-032.md                  |   8 +++
 docs/plan.md                            |   2 +-
 docs/tasks/T-032-ux-flows-s04-model.md  |  13 ++--
 docs/ux/screens-ai-task-tracker.md      |  27 +++++---
 docs/ux/user-flows-ai-task-tracker.md   |  16 +++--
 10 files changed, 323 insertions(+), 23 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint