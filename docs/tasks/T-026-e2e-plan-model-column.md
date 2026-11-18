# T-026: Q-01 E2E plan and coverage: S-01 model column

| Field | Value |
|---|---|
| id | T-026 |
| title | Q-01 E2E plan and coverage: S-01 model column |
| workflow | feature |
| stage | Q-01 |
| role | qa |
| status | done |
| hold | false |
| depends_on | T-019, T-025 |
| inputs | `docs/tasks/T-025-e2e-model-column.md`, `docs/tasks/T-019-e2e-model-column.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md` (UC-E2E-001..UC-E2E-008, UC-E2E-003 S8), `docs/analysis/e2e-test-data-ai-task-tracker.yaml` (UC-E2E-003 S8, `fixtures.sessions`), `tests/e2e/uc_e2e_001_003/shared.py`, `tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py`, `docs/issues/ISSUE-20261118-153622-e2e-runner-cannot-start-the-local-stack-and-invokes-pytest-w.md` |
| artifacts | `docs/qa/e2e-plan-task-list-model.md` |
| e2e | required - this is the e2e plan and coverage-matrix stage |
| ux | required - the plan includes the S-01 model cell assertions (model column, `+N`, tooltip/aria, «Нет данных») |
| chunks | `docs/chunks/T-026.json` (C1: e2e plan + requirement->UC->test matrix) |
| budget | max_write 32 KB / session 64 KB; one chunk |
| base_commit | 0ef0d3c |
| usage | in=60511 out=9996 cr=1620588 ratio=26.8x min=103 |
| issues | ISSUE-20261118-153622, ISSUE-20261118-185907 |
| commit | 85c202e |

## Acceptance

- [x] The plan defines a requirement -> UC -> test matrix that covers ALL `UC-E2E-001`..`UC-E2E-008` (the `e2e-plan-uc-coverage` registry check requires every documented UC id to be present). (matrix rows span UC-E2E-001..008).
- [x] The matrix includes a focused UC-E2E-003 S8 / R-014 block covering the S-01 model column: per-task ordered `models` list (`qwen3.8:27b` then `llama3.1:8b`, total desc then name asc), empty state for TASK-002 (no usage -> «Нет данных»), `+N`, tooltip/aria full list, and API `GET /api/tasks` field. (§3 S8-API-1..8 / S8-UI-1..5).
- [x] Test data validation is described: how `scripts/validate-e2e-data.py --strict` is run against `docs/analysis/e2e-test-data-ai-task-tracker.yaml` and the pass criteria (0 errors). (§4; validator: 8 use cases, 25 requirement links, 0 errors, 0 warnings).
- [x] The execution approach covers both a targeted UC run (e.g. `scripts/e2e.ps1 run -UC UC-E2E-003`) and the full suite, with the offline/skipped behavior noted. (§5).
- [x] An environment caveat is recorded for ISSUE-20261118-153622 (Docker Compose plugin missing + runner pytest backslash path) and the workaround (direct forward-slash pytest invocation). (§5 Environment caveat).
- [x] Entry/exit criteria and evidence expectations are stated (which `tests/e2e/results/*.json` and log lines constitute pass evidence; FAIL/UNCHECKED must be 0). (§6).
- [x] Chunk C1 is `done` with a produced hash in `docs/chunks/T-026.json`. (C1 `done`, sha256 in ledger).
- [x] `CP-1` (e2e plan and coverage) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | Q-01 e2e plan and coverage | `docs/qa/e2e-plan-task-list-model.md` | 7P/0F/0M | approved | docs/checkpoints/T-026-CP-1.md |
| CP-F | final acceptance | all task artifacts | 8P/0F/0M | approved | docs/checkpoints/T-026-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | Q-01 | qa | E2E plan + requirement->UC->test matrix (all UC-E2E-001..008, S-01/R-014 focus) | `docs/qa/e2e-plan-task-list-model.md` | pending |

## Log

- 2026-11-18: T-026 started on base `0ef0d3c`; T-019 and T-025 are done. C1 (e2e plan + requirement->UC->test matrix) planned as `pending` in `docs/chunks/T-026.json`; QA will claim/complete it. Inputs include UC-E2E-003/S8 (use cases + YAML), the T-025 e2e test files, and ISSUE-20261118-153622 (live-run environment caveat).
- 2026-11-18: `harness-qa` failed twice with SSE read timeouts (C1 was claimed but no plan was produced); recorded as ISSUE-20261118-185907. Lead fallback authored `docs/qa/e2e-plan-task-list-model.md` (all UC-E2E-001..008, focused UC-E2E-003 S8 / R-014 block, test-data validation, execution approach, ISSUE-20261118-153622 caveat, entry/exit criteria). YAML validator: 8 use cases, 25 requirement links, 0 errors, 0 warnings; `python -m py_compile` passes. C1 `done` with produced hash in `docs/chunks/T-026.json`.
- 2026-11-18: CP-1 approved (Q-01); checks 7 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=60511 out=9996 cr=1620588 ratio=26.8x min=103; reviewer auto (P2)
- 813579a: CP-1 snapshot
- 2026-11-18: CP-F approved (Q-01); checks 8 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=60511 out=9996 cr=1620588 ratio=26.8x min=103; reviewer auto (explicit -AutoApprove)
- 85c202e: CP-F snapshot
