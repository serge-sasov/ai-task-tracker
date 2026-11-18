# E2E plan and coverage: S-01 model column

| Field | Value |
|---|---|
| Task | T-026 (Q-01) |
| Role | QA |
| Date | 2026-11-18 |
| Scope | R-014 / UC-E2E-003 S8 feature delta + regression coverage UC-E2E-001..008 |
| Test data | `docs/analysis/e2e-test-data-ai-task-tracker.yaml` (`fixtures.sessions`, UC-E2E-003 S8) |
| Use cases | `docs/analysis/e2e-use-cases-ai-task-tracker.md` |
| Tests | `tests/e2e/uc_e2e_001_003/`, `tests/e2e/uc_e2e_004_006/`, `tests/e2e/uc_e2e_007_008/` |
| Environment | local Docker stack, `E2E_BASE_URL` (default `http://127.0.0.1:8080`), local Ollama |
| Known issue | `ISSUE-20261118-153622` (runner start / pytest module path) |
| Status | draft |

## 1. Scope and objectives

This plan verifies R-014: the S-01 task list shows the primary model and the full
ordered model list, and `GET /api/tasks` exposes `models` without changing other
fields. It also keeps regression coverage for UC-E2E-001..UC-E2E-008.

Objectives:

- prove the API contract (`models`, ordering, empty array) with deterministic data;
- prove the S-01 UI states (primary + `+N`, tooltip/aria full list, «Нет данных»);
- prove the data contract is valid and the existing e2e suite still passes;
- record evidence and any environment deviation.

## 2. Requirement -> UC -> test matrix

| Requirement | UC-E2E | Test / evidence | Level |
|---|---|---|---|
| R-001, R-026 | UC-E2E-001, UC-E2E-002 | `tests/e2e/uc_e2e_001_003/test_uc_e2e_001_stack_start.py`, `test_uc_e2e_002_ingest.py` | API/stack |
| R-002, R-006, R-007, R-009 | UC-E2E-003 | `tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py` (list/titles/durations/statuses) | API |
| R-003, R-004, R-005 | UC-E2E-003 | same file: detail steps/solutions (proposed/chosen/empty) | API |
| **R-014** | **UC-E2E-003 S8** | **same file: `test_list_models_field_order_and_empty_state`; offline `test_models_by_task_aggregation_math_offline`** | **API + helper** |
| R-007, R-012, R-013 | UC-E2E-003, UC-E2E-006 | detail token block; `tests/e2e/uc_e2e_004_006/test_uc_e2e_006_token_stats.py` | API |
| R-008, R-009 | UC-E2E-005 | `tests/e2e/uc_e2e_004_006/test_uc_e2e_005_calendar.py` | API |
| R-010, R-011 | UC-E2E-004 | `tests/e2e/uc_e2e_004_006/test_uc_e2e_004_ai_search.py` | API/LLM |
| R-025, R-026, R-028 | UC-E2E-007 | `tests/e2e/uc_e2e_007_008/test_uc_e2e_007_restart_idempotency.py` | API |
| R-028, R-029 | UC-E2E-008 | `tests/e2e/uc_e2e_007_008/test_uc_e2e_008_manual_deletion.py` | API |
| R-020, R-021, R-022, R-027, R-030 | UC-E2E-001..008 | stack/UI environment checks in the suite; RU copy in the prototype | environment |
| R-023, R-024 | UC-E2E-003, 004, 005 | timing/scale assertions (QA evidence) | environment |

Every documented use case is represented: **UC-E2E-001, UC-E2E-002,
UC-E2E-003, UC-E2E-004, UC-E2E-005, UC-E2E-006, UC-E2E-007, UC-E2E-008**.

## 3. UC-E2E-003 S8 — model column cases

### 3.1 API cases

| Case | Input | Expected |
|---|---|---|
| S8-API-1 | `GET /api/tasks` | 200, `total=5`, every item has a `models` array |
| S8-API-2 | TASK-001 | `["qwen3.8:27b", "llama3.1:8b"]` (qwen total 10200 > llama 1950) |
| S8-API-3 | TASK-002 | `[]` (usage is null; no fabricated model) |
| S8-API-4 | TASK-003 | `["qwen3.8:27b", "llama3.1:8b"]` (2900 > 550) |
| S8-API-5 | TASK-004 | `["qwen3.8:27b", "llama3.1:8b"]` (1400 > 270) |
| S8-API-6 | TASK-005 | `["qwen3.8:27b", "llama3.1:8b"]` (840 > 150) |
| S8-API-7 | ordering rule | total tokens (input+output+cache) desc; equal totals -> model name asc |
| S8-API-8 | empty/no-usage | empty array; UI maps it to «Нет данных» |

Automated by `test_list_models_field_order_and_empty_state` (parametrized over
TASK-001..005) using `shared.models_by_task()` from the YAML dataset. The helper
math/order/empty path is also covered without a stack by
`test_models_by_task_aggregation_math_offline`.

### 3.2 UI cases (S-01)

| Case | Expected |
|---|---|
| S8-UI-1 | column «Модель» is immediately after «Название» |
| S8-UI-2 | primary model is the first ordered model; `+1` is visible when more than one model exists |
| S8-UI-3 | cell `title` and `aria-label` contain the full ordered list (not hover-only) |
| S8-UI-4 | TASK-002 shows «Нет данных»; accessible name «Модель: Нет данных» |
| S8-UI-5 | long model names do not break the table; full value remains in tooltip/aria |

UI evidence is the offline prototype `docs/ux/prototype/ai-task-tracker/uc-e2e-003.html`
and the Q-02 visual/manual review (there is no browser-DOM automation in the
current suite). The API contract is automated; the visual review confirms S8-UI-1..5.

## 4. Test data validation

Command:

```
python scripts/validate-e2e-data.py docs/analysis/e2e-test-data-ai-task-tracker.yaml \
  --use-cases-md docs/analysis/e2e-use-cases-ai-task-tracker.md --strict
```

Expected: `0 error(s), 0 warning(s)`, 8 use cases, all UC ids in Markdown and YAML.
The dataset is the single source for TASK-001..005 titles, durations, statuses and
`usage`; no secrets, no external endpoints.

## 5. Execution approach

1. Validate the dataset (section 4) and compile the e2e modules:
   `python -m py_compile tests/e2e/uc_e2e_001_003/shared.py tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py`.
2. Bring up the isolated local stack (`docker compose up -d` through the runner or
   the documented compose command) and wait for `/api/health`.
3. Targeted run: `.\scripts\e2e.ps1 run -UC UC-E2E-003` (preferred) or, while
   `ISSUE-20261118-153622` is open, use the workaround:
   `python -m pytest tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py -q`
   with `E2E_BASE_URL` pointing at the stack.
4. Full regression: `.\scripts\e2e.ps1 run` (or the equivalent forward-slash pytest
   invocation), then inspect `tests/e2e/results/e2e-results.json`.
5. Cleanup: runner teardown/`docker compose down -v`; source opencode files are
   read-only and untouched.

Offline behavior: stack-dependent tests skip cleanly when the stack is unavailable;
a skip is **not** pass evidence for S8-API-1..8. The offline math test is pass evidence
only for `models_by_task()`.

### Environment caveat

`ISSUE-20261118-153622` records: (a) the local Docker CLI reports
`unknown docker command: "compose"` (Compose plugin mismatch), and (b) the runner's
`python -m pytest tests\e2e` fails with `No module named tests\e2e` while the
forward-slash path works. Until the runner/environment is fixed, use the workaround
above; do not claim a live pass without a real targeted run.

## 6. Entry / exit criteria and evidence

Entry:

- T-024 and T-025 are done; dataset validator passes; stack is available for a live run.

Exit (for Q-02 acceptance):

- targeted UC-E2E-003 run: pass, not skipped; `models` cases S8-API-1..8 pass;
- full suite: pass (or documented environment failure, not a product failure);
- YAML validator: 0 errors;
- S-01 UI S8-UI-1..5 confirmed by Q-02 visual/manual review of the prototype;
- evidence: `tests/e2e/results/UC-E2E-003.json`, `tests/e2e/results/e2e-results.json`,
  pytest log, `docs/qa/review-T-027.md`, checkpoint checks JSON. `FAIL=0` and
  `UNCHECKED=0` for acceptance; MANUAL items require the user.

## 7. Risks and assumptions

- ISSUE-20261118-153622 may block the scripted runner; direct pytest is the workaround.
- BUG-007 (pre-existing backend unit failures in SearchServiceTest/TokenServiceTest)
  is outside the e2e scope; it is not counted as a T-026 failure.
- The UI is verified visually/manually in Q-02; there is no browser automation for
  S-01 in this suite. The API field and helper math are automated.
- Test data is deterministic; no live opencode sessions are used.
