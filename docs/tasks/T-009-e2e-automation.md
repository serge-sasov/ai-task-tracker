# T-009: I-02 E2E automation and runner

| Field | Value |
|---|---|
| id | T-009 |
| title | I-02 E2E automation and runner |
| workflow | feature |
| stage | I-02 |
| role | coder |
| status | in_progress |
| hold | false |
| depends_on | T-008, T-003, T-007 |
| inputs | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/architecture/contracts-ai-task-tracker.md` |
| artifacts | `tests/e2e/**`, `scripts/e2e.ps1`, `scripts/validate-e2e-data.py` |
| e2e | required - runs the YAML dataset against the local stack |
| ux | not applicable (automation only) |
| chunks | ledger `docs/chunks/T-009.json` (single source of truth for per-chunk state); runner skeleton, dataset validator, three UC batches, fixtures and cleanup, final full run |
| budget | `max_write_kb` / `max_session_kb` / `context_stop_pct` / `max_turns` / `max_input_tokens` / `max_wall_min`; effective values in the ledger `budget` object |
| base_commit | - |
| usage | - |
| issues | - |
| commit | pending |

## Acceptance

- [ ] Tests are generated from the YAML dataset and run against an isolated local/staging stack only.
- [ ] The runner emits machine-readable results per `UC-E2E-NNN` and cleans up test data.
- [ ] No chunk reads more than its own UC batch and the runner API.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | runner and validator | `scripts/e2e.ps1`, `scripts/validate-e2e-data.py` | - | pending | `docs/checkpoints/T-009-CP-1.md` |
| CP-2 | UC batches and full run | `tests/e2e/**` | - | pending | `docs/checkpoints/T-009-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-009-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- none

## Log

- 2026-11-14: card created during the 3.0.0 rollout (T-046) so the board row has a card and a chunk ledger before the task starts.
- 2026-11-16: T-009 started (I-02); board set in_progress; first chunk C1 dispatched.
