# T-025: I-02 E2E automation: S-01 model column

| Field | Value |
|---|---|
| id | T-025 |
| title | I-02 E2E automation: S-01 model column |
| workflow | feature |
| stage | I-02 |
| role | coder |
| status | in_progress |
| hold | false |
| depends_on | T-024, T-019, T-023 |
| inputs | `docs/tasks/T-024-implement-model-column.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md` (UC-E2E-003 S8), `docs/analysis/e2e-test-data-ai-task-tracker.yaml` (UC-E2E-003 S8, `fixtures.sessions`), `docs/architecture/contracts-ai-task-tracker.md` (§4.2), `docs/architecture/openapi-api-001-ai-task-tracker.yaml` (API-001) |
| artifacts | `tests/e2e/uc_e2e_001_003/shared.py`, `tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py` |
| e2e | required - this stage automates R-014 (S-01 model column) end-to-end |
| ux | required - assertions cover the model cell contract consumed by S-01 |
| chunks | `docs/chunks/T-025.json` (C1: e2e helper + UC-E2E-003 S8 model assertions) |
| budget | max_write 24 KB / session 48 KB; one chunk |
| base_commit | 240a2fc |
| usage | n/a |
| issues | ISSUE-20261118-153622 |
| commit | - |

## Acceptance

- [x] `shared.py` exposes a helper that computes the expected `models` list per task from the YAML `fixtures.sessions` `usage`: sum `input_tokens + output_tokens + cache_tokens` per model, order by that total descending then model name ascending; a session with `usage: null`/empty yields an empty list. (helper `models_by_task()`).
- [x] `test_uc_e2e_003_task_list_detail.py` asserts `GET /api/tasks` returns `models` for every TASK-001..TASK-005, including `[]` (empty list) for TASK-002 (no usage → «Нет данных»). (`test_list_models_field_order_and_empty_state`, parametrized TASK-001..005).
- [x] The test asserts the ordering (total tokens desc, tie-break by model name asc) so `TASK-001/003/004/005` return `["qwen3.8:27b", "llama3.1:8b"]` in that order. (offline `test_models_by_task_aggregation_math_offline` passed).
- [x] `python -m py_compile` passes for both `shared.py` and `test_uc_e2e_003_task_list_detail.py`.
- [x] No production endpoints are used (local `127.0.0.1` stack only); no new network calls or storage writes are introduced.
- [x] Targeted live run skipped: runner/environment issue tracked in ISSUE-20261118-153622; `py_compile` + offline aggregation test + YAML validation (0 errors) pass.
- [x] Chunk C1 is `done` with produced hashes in `docs/chunks/T-025.json`.
- [x] No unrelated changes; the runner `scripts/e2e.ps1` and validator `scripts/validate-e2e-data.py` are unchanged.
- [x] `CP-2` (e2e automation) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | I-02 e2e automation | shared.py, test_uc_e2e_003_task_list_detail.py | - | pending | `docs/checkpoints/T-025-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-025-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | I-02 | coder | E2E: S-01 model column (models field, order, empty state) | `tests/e2e/uc_e2e_001_003/shared.py`, `tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py` | pending |

## Log

- 2026-11-18: T-025 started on base `240a2fc`; T-024, T-019, T-023 are done. C1 (e2e helper + UC-E2E-003 S8 model assertions) planned as `pending` in `docs/chunks/T-025.json`; coder will claim/complete it.
- 2026-11-18: Coder completed C1 (`done` in `docs/chunks/T-025.json`). Added the `models_by_task()` helper and `test_list_models_field_order_and_empty_state` (parametrized TASK-001..005, incl. `[]` for TASK-002 and total-desc/name-asc order) plus an offline aggregation-math test. `python -m py_compile` passes; offline pytest 1 passed / 23 skipped (stack-dependent tests skip cleanly); YAML validator 0 errors. Targeted live run skipped: runner/environment issue tracked in ISSUE-20261118-153622 (`docker compose` plugin missing + runner pytest backslash path); `scripts/e2e.ps1` and test `__init__` files left unchanged per scope.
