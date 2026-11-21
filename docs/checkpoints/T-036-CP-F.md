# T-036 CP-F: Final acceptance for S-04 model column

| Field | Value |
|---|---|
| task | T-036 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | coder |
| stage | I-01 |
| artifacts | src/web/src/components/ModelCell.vue, src/web/src/views/CalendarDayView.vue, src/web/src/views/DashboardView.vue, src/web/tests/unit/ModelCell.spec.ts, src/web/tests/unit/views.spec.ts, src/web/vitest.config.ts, src/web/package.json, src/web/package-lock.json |
| status | approved |
| requested_at | 2026-11-21T15:39:23 |
| reviewed_at | 2026-11-21T15:39:23 |
| reviewer | user |
| decision | approved |
| snapshot_commit | a6bdf69 |
| generated_by | orchestrator (manual) |
| checks_file | docs/checkpoints/T-036-CP-1.checks.json |

## Review scope

- Final acceptance for T-036 (all task artifacts).
- `CalendarDayView.vue` (S-04 day view) renders a "Модель" column after "Задача" showing the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- One shared model-cell component or composable (`src/web/src/components/ModelCell.vue`) is used by both `DashboardView.vue` (S-01) and `CalendarDayView.vue` (S-04); no copy-paste duplication of the cell logic.
- The S-01 behavior and its existing tests remain green; regression tests cover the S-01 cell and the S-04 cell.
- The change is frontend-only: no backend/API/schema change; the built frontend reflects the new column.
- Unit/component tests pass and the project build succeeds; no `TODO`/`FIXME` is left in the new code.
- `CP-1` (implementation and tests) and `CP-F` (final acceptance) are approved.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | coder | 8 | Shared ModelCell component + S-01/S-04 integration and tests |

Ledger: `docs/chunks/T-036.json`

## Checks

Inherited from `CP-1` (no new work between `CP-1` and `CP-F`): 7 PASS, 1 FAIL, 0 MANUAL, 1 N/A, 0 UNCHECKED.

| ID | Check | Result | Detail |
|---|---|---|---|
| chunk-budget | chunk-ledger | FAIL | C1: src/web/package-lock.json 98,3 KB > 12 KB write; C1: 125,2 KB > 40 KB session |

Known check-design gap: [ISSUE-20261120-113748](../issues/ISSUE-20261120-113748-chunk-budget-measures-final-file-size-vs-authored-output.md). The FAIL measures the final size of a machine-generated lockfile against the per-write budget; the authored code is within budget.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 7 | 540747 | 153084 | 0 | 9652968 | 17.9x | 307 | 0.00 |

## User remarks

- User explicitly approved T-036 CP-1 and CP-F on 2026-11-21. The chunk-budget FAIL is a known check-design gap (ISSUE-20261120-113748); semantic acceptance is green (build PASS, 8/8 tests).

## User edits

- none

## Role response

- Machine checks (CP-1): 1 FAIL, 0 UNCHECKED, 0 MANUAL.
- Failed checks: chunk-budget (known check-design gap ISSUE-20261120-113748).
- User decision: approved (explicit, 2026-11-21)

## Next action

- accept task
