# T-020: U-01 UX flows and screens for S-01 model display

| Field | Value |
|---|---|
| id | T-020 |
| title | U-01 UX flows and screens: S-01 model column |
| workflow | feature |
| stage | U-01 |
| role | ux-designer |
| status | in_progress |
| hold | false |
| depends_on | T-018, T-019 |
| inputs | `docs/tasks/T-018-requirements-model-column.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md` |
| artifacts | `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md` |
| e2e | required - R-014 is asserted by UC-E2E-003 S8; U-02 will mirror it in the prototype |
| ux | required - this stage defines the visible S-01 model cell, `+N`, tooltip/aria and "Нет данных" |
| chunks | `docs/chunks/T-020.json` (C1: flows; C2: screens) |
| budget | max_write 32 KB / session 64 KB; two chunks |
| base_commit | aa9d8cb |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [x] F-1 describes the model column immediately after "Название", the primary model plus `+N`, the tooltip/aria full list and "Нет данных".
- [x] The screen registry, S-01 section, states, a11y guidance, UI copy and traceability include R-014.
- [x] Screen IDs and flow IDs remain unique; no existing states or flows are removed.
- [x] T-018 requirements and T-019 UC-E2E-003 remain consistent with the UX text.
- [x] Chunks C1 and C2 are `done` with produced hashes in `docs/chunks/T-020.json`.
- [x] `CP-1` (flows and screens) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | U-01 flows and screens | `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md` | 9P/0F/0M | approved | docs/checkpoints/T-020-CP-1.md |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-020-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | U-01 | ux-designer | F-1 model-column flow | `docs/ux/user-flows-ai-task-tracker.md` | done |
| C2 | U-01 | ux-designer | S-01 screen, states and a11y for the model column | `docs/ux/screens-ai-task-tracker.md` | done |

## Log

- 2026-11-18: T-020 started on base `aa9d8cb`; T-018/T-019 are done. F-1 and S-01 updated for R-014; chunks recorded.
- 2026-11-18: CP-1 approved (U-01); checks 9 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (P2)
