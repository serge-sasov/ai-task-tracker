# E2E plan and coverage: S-04 day-view model column

| Field | Value |
|---|---|
| Task | T-038 (Q-01) |
| Role | QA |
| Date | 2026-11-22 |
| Scope | R-015 / UC-E2E-005 S-04 feature delta (day-view «Модель» column); regression via UC-E2E-005 suite |
| Base plan | `docs/qa/e2e-plan-task-list-model.md` (T-026, S-01) — structure reused; S-01 coverage NOT duplicated, this plan is the S-04 delta only |
| Test data | `docs/analysis/e2e-test-data-ai-task-tracker.yaml` (`fixtures.calendar.day_2026-11-10-models`, `#day_2026-11-11-models`) |
| Use cases | `docs/analysis/e2e-use-cases-ai-task-tracker.md` (UC-E2E-005) |
| Tests | `tests/e2e/uc_e2e_004_006/test_uc_e2e_005_calendar.py` |
| Environment | isolated local Docker stack, `E2E_BASE_URL` (127.0.0.1), frontend on `E2E_FRONTEND_URL` (default `http://127.0.0.1:8081`) |
| Status | draft (CP-1 pending) |

## 1. Scope and objectives

Verifies R-015: the S-04 day view (`/calendar/:date`) shows the «Модель» column
immediately after «Задача», with cell states identical to S-01 (R-014): primary
model, `+N`, full ordered list in `title`/`aria-label`, «Нет данных» when
`models` is empty. Ordering/tie-break reuse the R-014 rule (total tokens desc,
tie-break by model name); no new rule. S-01 behavior (UC-E2E-003 S8, T-026 plan)
is unchanged and NOT re-verified here; regression is covered by the existing
UC-E2E-005 suite (`test_week_view_days`, `test_week_cell_tasks_match_dataset`,
`test_day_view_duration_semantics`, `test_month_view_task_counts`,
`test_invalid_period_400`).

Objectives:

- prove every R-015 acceptance item has an automated assertion on the dataset-driven expected values;
- prove the check runs against the actually BUILT frontend (served SPA), not only the API — the T-027/T-028 stale-frontend failure mode is a covered risk;
- prove the dataset is valid and includes both multi-model and empty-model day-view tasks;
- record evidence and residual risks.

## 2. Requirement -> UC -> test matrix (S-04 delta)

| Requirement | UC-E2E | Test / evidence | Level |
|---|---|---|---|
| **R-015** (primary + `+N`, full list in `title`/`aria-label`, «Нет данных», column after «Задача») | **UC-E2E-005 step 2/2a** | **`tests/e2e/uc_e2e_004_006/test_uc_e2e_005_calendar.py::test_day_view_model_column`** (parametrized over `day_2026-11-11-models` + `day_2026-11-10-models` fixtures) | API + built-frontend guard |
| R-015 (served SPA actually contains the ModelCell) | UC-E2E-005 preconditions (built frontend) | `test_uc_e2e_005_calendar.py::test_built_frontend_has_model_cell` (`model-cell` marker over all served JS bundles + Vite `ModelCell-*.js` chunks, 127.0.0.1) | DOM smoke |
| R-008, R-009 (regression, unchanged) | UC-E2E-005 steps 1, 2, 3, 4 | `test_week_view_days`, `test_week_cell_tasks_match_dataset`, `test_day_view_duration_semantics`, `test_month_view_task_counts` | API |
| R-008 error handling (invalid period) | UC-E2E-005 E2 | `test_invalid_period_400` | API |
| R-014 (S-01, dependency — NOT re-tested here) | UC-E2E-003 S8 | covered by T-026 plan / `test_uc_e2e_003_task_list_detail.py` | out of scope |
| R-023, R-030 (indirect: load < 3 s, RU texts) | UC-E2E-005 | `LOAD_TIMEOUT_SECONDS = 3` in the test module; RU strings from dataset fixtures | environment |

Every R-015 acceptance item is covered — traceability table in section 4; no gaps.

## 3. ModelCell states and expected results (S-04 delta)

Cell contract per `src/web/src/components/ModelCell.vue` (read-only reference):
`title`/`aria-label` = `«Модель: <comma-joined ordered list>»` (or `«Модель: Нет данных»`),
visible primary = first model, visible `+N` when more than one model.

| Case | Fixture (dataset anchor) | Visible cell | `title` / `aria-label` | Expected | Automated by |
|---|---|---|---|---|---|
| S4-UI-1 | `day_2026-11-11-models` (TASK-003/004/005) | `qwen3.8:27b` + `+1` | `Модель: qwen3.8:27b, llama3.1:8b` | primary + `+N` for >1 model; full ordered list in title/aria | `test_day_view_model_column` |
| S4-UI-2 | `day_2026-11-10-models` (TASK-001) | `qwen3.8:27b` + `+1` | `Модель: qwen3.8:27b, llama3.1:8b` | same on the second day (10.11) | `test_day_view_model_column` |
| S4-UI-3 | `day_2026-11-10-models` (TASK-002) | `Нет данных` (no `+N`) | `Модель: Нет данных` | empty `models` -> «Нет данных» | `test_day_view_model_column` |
| S4-UI-4 | both `*-models` fixtures | column header position | `column_after: "Задача"` | column immediately after «Задача» | `test_day_view_model_column` (`_models_expectations` asserts `column_after`) |
| S4-FE-1 | served SPA bundle | — | — | built frontend contains ModelCell markup (`model-cell` marker) across all JS bundles + `ModelCell-*.js` chunks | `test_built_frontend_has_model_cell` |

Ordering: `full_list_in_title_aria` equals `models` from `GET /api/tasks`
(total tokens desc, tie-break by name — R-014 rule; the test asserts
`got == exp["full_list_in_title_aria"]` and `got[0] == exp["primary"]`), so
ordering/tie-break is verified on the API side of S-04 without a new rule.

## 4. R-015 traceability (acceptance item -> test)

| R-015 acceptance item | Covered by |
|---|---|
| Column «Модель» immediately after «Задача» | `test_day_view_model_column` (asserts fixture `column_after == "Задача"`) |
| Multi-model task: first model from `models` + `+N` | `test_day_view_model_column` (primary + `plus_n` startswith `+`, tasks TASK-001/003/004/005) |
| `title`/`aria-label` contain full list in `GET /api/tasks` order (total tokens desc, name tie-break) | `test_day_view_model_column` (`got == full_list_in_title_aria`, ordering inherited from R-014 rule) |
| Empty `models` -> «Нет данных» | `test_day_view_model_column` (TASK-002: `models == []`, `no_data`/`title_aria` from fixture) |
| No backend/API/schema change; S-01 (R-014) behavior unchanged | dependency declared; S-01 regression stays in T-026 scope (no duplicate); `test_day_view_duration_semantics` + week/month tests guard UC-E2E-005 non-model behavior |

Gaps: none. No silent skips.

## 5. Test data validation

Command (expected: `0 error(s), 0 warning(s)`):

```
python scripts/validate-e2e-data.py docs/analysis/e2e-test-data-ai-task-tracker.yaml \
  --use-cases-md docs/analysis/e2e-use-cases-ai-task-tracker.md --strict
```

The dataset is the single source for S-04 cell expectations and includes BOTH
required day-view cases: multi-model tasks (TASK-001, TASK-003, TASK-004, TASK-005 —
`primary` + `plus_n` + `full_list_in_title_aria`) and the empty-model task
(TASK-002 — `no_data: "Нет данных"`), anchored by `#day_2026-11-10-models` and
`#day_2026-11-11-models`. Tests read expectations only from these fixtures
(`_models_expectations()`), never hardcoded.

## 6. Execution approach

1. Validate the dataset (section 5).
2. Bring up the isolated local stack via the runner (`scripts/e2e.ps1 run -UC UC-E2E-005`)
   or `python -m pytest tests/e2e/uc_e2e_004_006/test_uc_e2e_005_calendar.py -q`
   with `E2E_BASE_URL` (default `http://127.0.0.1:8080`) and
   `E2E_FRONTEND_URL` (default `http://127.0.0.1:8081`) pointing at the stack.
3. Run the S-04 tests; the model-column check runs against the actually built
   frontend (rebuilt image or DOM smoke over 127.0.0.1): `test_built_frontend_has_model_cell`
   fetches the served `index.html`, every referenced JS bundle, and every Vite
   `ModelCell-*.js` chunk, and fails when the `model-cell` marker is absent.
   `test_day_view_model_column` repeats the guard before asserting cell states.
4. Residual checks: inspect `tests/e2e/results/` for pass/skip counts; a SKIP of
   live tests (stack down) is NOT pass evidence.
5. Cleanup: runner teardown / `docker compose down -v`; test data (seed sessions)
   reset per UC-E2E-001 cleanup; source files untouched.

### Covered risk: stale frontend (T-027/T-028)

T-027/T-028 showed the model column can be "passed" by the API while the served
SPA bundle is stale (missing ModelCell). Mitigation, already in the suite: the
`model-cell` marker guard — `test_built_frontend_has_model_cell` asserts the
marker across ALL served bundles and discovered `ModelCell-*.js` chunks and fails
with an explicit "rebuild the frontend image" assertion otherwise. The marker is
ASCII so minification cannot strip it. This is an explicit covered risk, not a
residual one.

## 7. Entry / exit criteria and evidence

Entry:

- T-036 (S-04 implementation) and T-037 (S-04 e2e) done; snapshot `84f9c1a`.
- Dataset validator passes; isolated stack available for a live run.

Exit (for CP-1 / final acceptance):

- `test_day_view_model_column` and `test_built_frontend_has_model_cell` pass (not skipped);
- UC-E2E-005 regression tests (week/day/month/invalid-period) pass;
- YAML validator: 0 errors;
- evidence: `tests/e2e/results/e2e-results.json`, pytest log, `docs/qa/e2e-report-s04-model.md` (Q-02).

## 8. Risks and assumptions

- stale-frontend failure mode (T-027/T-028) — covered by the `model-cell` marker guard (section 6);
- live tests skip when the stack is unavailable — a skip is not pass evidence (section 6);
- visual review of cell rendering (font/ellipsis) remains a Q-02/visual item; text and a11y states are automated;
- test data deterministic; no production endpoints, no secrets.

## 9. Checkpoints (declared)

| ID | Stage | Artifacts | Checks | Status |
|---|---|---|---|---|
| CP-1 | Q-01 e2e plan and coverage | `docs/qa/e2e-plan-s04-model.md` | e2e-plan-exist, e2e-plan-matrix, e2e-plan-uc-coverage | pending |
| CP-F | final acceptance | all task artifacts incl. `docs/qa/e2e-report-s04-model.md` | stage checks + user review | pending |
