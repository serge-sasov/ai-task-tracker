# T-027: Q-02 QA, acceptance and verdict: S-01 model column

| Field | Value |
|---|---|
| id | T-027 |
| title | Q-02 QA, acceptance and verdict: S-01 model column |
| workflow | feature |
| stage | Q-02 |
| role | qa |
| status | awaiting_review |
| hold | true |
| depends_on | T-026, T-025, T-024, T-023, T-021 |
| inputs | `docs/qa/e2e-plan-task-list-model.md`, `tests/e2e/uc_e2e_001_003/shared.py`, `tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py`, `docs/tasks/T-024-implement-model-column.md`, `docs/tasks/T-023-contracts-model-column.md`, `docs/architecture/contracts-ai-task-tracker.md` (§4.2), `docs/architecture/openapi-api-001-ai-task-tracker.yaml`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/uc-e2e-003.html`, `docs/issues/ISSUE-20261118-153622-e2e-runner-cannot-start-the-local-stack-and-invokes-pytest-w.md`, `docs/issues/ISSUE-20261118-185907-harness-qa-subagent-sse-read-timeout-during-t-026.md`, `docs/bugs/BUG-007-pre-existing-backend-test-failures.md` |
| artifacts | `docs/qa/e2e-report-task-list-model.md`, `docs/qa/review-T-027.md` |
| e2e | required - run the UC-E2E-003 suite (S8 model cases) and the full suite; record results |
| ux | required - verify the S-01 model cell (column after «Название», primary + `+N`, tooltip/aria, «Нет данных») against the prototype and UI spec |
| chunks | `docs/chunks/T-027.json` (C1: run e2e and write the report; C2: code/UX/contract review and verdict) |
| budget | max_write 32 KB / session 64 KB; two chunks |
| base_commit | c8139c2 |
| usage | n/a |
| issues | ISSUE-20261118-153622, ISSUE-20261118-185907 |
| commit | - |

## Acceptance

- [x] A targeted UC-E2E-003 run is performed with the S8 API cases (S8-API-1..8: `models` field, ordered list `["qwen3.8:27b","llama3.1:8b"]`, empty `[]` for TASK-002, ordering rule); use the runner `.\scripts\e2e.ps1 run -UC UC-E2E-003` or, while ISSUE-20261118-153622 is open, the forward-slash workaround `python -m pytest tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py -q`. (forward-slash workaround used; 24/24 PASS).
- [x] The full e2e suite is attempted (`.\scripts\e2e.ps1 run` or the forward-slash equivalent) and the outcome (pass / documented environment failure) is recorded. (full suite attempted via forward-slash pytest: targeted UC-E2E-003 24/24 PASS; full suite 55 PASS/4 FAIL in UC-E2E-004 (unrelated AI-search helper/503), recorded as a regression deviation; BUG-007 and the two issues accounted for).
- [x] `docs/qa/e2e-report-task-list-model.md` contains the per-case results with **PASS** and **FAIL** counts and the evidence paths (`tests/e2e/results/UC-E2E-003.json`, `tests/e2e/results/e2e-results.json`, pytest log lines). (registry `qa-report-results` requires both `PASS` and `FAIL` tokens).
- [x] `docs/qa/review-T-027.md` reviews the code (`TaskListItem.java`, `TaskQueryService.java`, `types.ts`, `DashboardView.vue`), the UX (S-01 cell vs prototype `uc-e2e-003.html` and `ui-spec-ai-task-tracker.md`), and the contracts (contracts §4.2, `openapi-api-001-ai-task-tracker.yaml`), and contains an explicit **Verdict** (PASS/FAIL). (registry `qa-verdict` requires the `Verdict` token).
- [x] BUG-007 (5 pre-existing backend unit failures) is accounted for as out-of-scope for R-014 and not counted as a T-027 failure; ISSUE-20261118-153622 (runner/env) and ISSUE-20261118-185907 (harness-qa SSE timeout) are noted with their impact on the live run.
- [x] The S-01 UI states (primary model + `+N`, tooltip/aria full list, «Нет данных» for TASK-002) are confirmed by visual/manual review of the offline prototype.
- [x] Chunks C1 and C2 are `done` with produced hashes in `docs/chunks/T-027.json`.
- [x] `CP-2` (e2e report and verdict) and `CP-F` (final acceptance) are declared. Note: `hold: true` forces a user review at both checkpoints even if the machine checks are clean.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | Q-02 e2e report and verdict | `docs/qa/e2e-report-task-list-model.md`, `docs/qa/review-T-027.md` | 8P/0F/0M | awaiting_review | docs/checkpoints/T-027-CP-2.md |
| CP-F | final acceptance | all task artifacts | - | pending | docs/checkpoints/T-027-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | Q-02 | qa | Run UC-E2E-003 (S8) + full suite, write the e2e report | `docs/qa/e2e-report-task-list-model.md` | done |
| C2 | Q-02 | qa | Code/UX/contract review and verdict | `docs/qa/review-T-027.md` | done |

## Log

- 2026-11-18: T-027 started on base `c8139c2`; T-026, T-025, T-024, T-023, T-021 are all done. C1 (run e2e + write report) and C2 (review + verdict) planned as `pending` in `docs/chunks/T-027.json`; QA will claim/complete them. `hold: true` — both checkpoints wait for the user. Note ISSUE-20261118-185907 (harness-qa SSE timeout on T-026): retry the `harness-qa` role and confirm it completes; if it times out again, the lead falls back.
- 2026-11-18: QA completed C1 and C2 (both `done` in `docs/chunks/T-027.json`). Targeted UC-E2E-003 24/24 PASS (S8 model cases; forward-slash pytest workaround per ISSUE-20261118-153622, after rebuilding the backend image with the T-024 code). Full suite 55 PASS / 4 FAIL — all four in UC-E2E-004 AI search (3× `_post_json` helper tuple contract + 1× HTTP 503), unrelated to R-014, recorded as a regression deviation. Dataset validator 0 errors (8 use cases, 25 links). Verdict: PASS for R-014 with the regression deviation documented (see `docs/qa/review-T-027.md`). BUG-007 and both issues accounted for; the run evidence JSONs committed separately as `9bdbaec`.
- 2026-11-18: CP-2 awaiting_review (Q-02); checks 8 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer user
