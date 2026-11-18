# T-020 CP-F: U-01 UX flows and screens: S-01 model column

| Field | Value |
|---|---|
| task | T-020 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | ux-designer |
| stage | U-01 |
| artifacts | docs/ux/user-flows-ai-task-tracker.md, docs/ux/screens-ai-task-tracker.md |
| status | approved |
| requested_at | 2026-11-18T10:06:59 |
| reviewed_at | 2026-11-18T10:06:59 |
| reviewer | auto (explicit -AutoApprove) |
| decision | approved |
| snapshot_commit | 381ff63 |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-020-CP-F.checks.json |

## Review scope

- Stage `U-01` acceptance for T-020.
- F-1 describes the model column immediately after "Название", the primary model plus `+N`, the tooltip/aria full list and "Нет данных".
- The screen registry, S-01 section, states, a11y guidance, UI copy and traceability include R-014.
- Screen IDs and flow IDs remain unique; no existing states or flows are removed.
- T-018 requirements and T-019 UC-E2E-003 remain consistent with the UX text.
- Chunks C1 and C2 are `done` with produced hashes in `docs/chunks/T-020.json`.
- `CP-1` (flows and screens) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | ux-designer | 1 | F-1 model-column flow |
| C2 | done | d1 | ux-designer | 1 | S-01 screen, states and a11y for the model column |

Ledger: `docs/chunks/T-020.json`

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
| chunk-budget | chunk-ledger | PASS | 2 chunk(s) within 32/64 KB |

Summary: 10 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

- token usage not available (opencode CLI or database unavailable)

## Diff stat

```
docs/activity.puml                      |   2 +-
 docs/activity.svg                       |   2 +-
 docs/checkpoints/T-020-CP-1.checks.json |  80 +++++++++++++++++++++++++
 docs/checkpoints/T-020-CP-1.md          |  81 ++++++++++++++++++++++++++
 docs/chunks/T-020.json                  | 100 ++++++++++++++++++++++++++++++++
 docs/evidence/T-020.md                  |   8 +++
 docs/plan.md                            |   2 +-
 docs/tasks/T-020-ux-model-column.md     |  53 +++++++++++++++++
 docs/ux/screens-ai-task-tracker.md      |  97 +++++++++++++++++--------------
 docs/ux/user-flows-ai-task-tracker.md   |  10 ++--
 10 files changed, 385 insertions(+), 50 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- accept task