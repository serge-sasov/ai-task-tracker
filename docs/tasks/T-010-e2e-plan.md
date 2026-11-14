# T-010: Q-01 E2E plan and coverage

| Field | Value |
|---|---|
| id | T-010 |
| title | Q-01 E2E plan and coverage |
| workflow | feature |
| stage | Q-01 |
| role | qa |
| status | pending |
| hold | false |
| depends_on | T-003, T-009 |
| inputs | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `tests/e2e/**` |
| artifacts | `docs/qa/e2e-plan-*.md` |
| e2e | required - coverage matrix over UC-E2E-001..008 |
| ux | not applicable |
| chunks | ledger `docs/chunks/T-010.json` (single source of truth for per-chunk state); one coverage-matrix batch per feature area plus one execution-approach chunk |
| budget | `max_write_kb` / `max_session_kb` / `context_stop_pct` / `max_turns` / `max_input_tokens` / `max_wall_min`; effective values in the ledger `budget` object |
| base_commit | - |
| usage | - |
| issues | - |
| commit | pending |

## Acceptance

- [ ] Every requirement maps to a `UC-E2E-NNN` and to at least one automated test.
- [ ] The dataset is validated against the analysis and the runner contract.
- [ ] The execution approach names the environment, the order, and the cleanup.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | coverage matrix | `docs/qa/e2e-plan-*.md` | - | pending | `docs/checkpoints/T-010-CP-1.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-010-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- none

## Log

- 2026-11-14: card created during the 3.0.0 rollout (T-046) so the board row has a card and a chunk ledger before the task starts.
