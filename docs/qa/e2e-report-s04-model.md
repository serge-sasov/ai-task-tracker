# E2E report: S-04 day-view model column

| Field | Value |
|---|---|
| Task | T-039 (Q-02) |
| Role | qa |
| Date | 2026-11-22 |
| Scope | S-04 e2e (UC-E2E-005, R-015) + S-01 regression (UC-E2E-003 + unit/component) |
| Base plan | `docs/qa/e2e-plan-s04-model.md` (T-038, Q-01) |
| Test data | `docs/analysis/e2e-test-data-ai-task-tracker.yaml` (`#day_2026-11-11-models`, `#day_2026-11-10-models`) — validator: 0 error(s), 0 warning(s) |
| Use cases | UC-E2E-005 (S-04), UC-E2E-003 (S-01 regression) |
| Tests | `test_uc_e2e_005_calendar.py` (7), `test_uc_e2e_003_task_list_detail.py` (24), `ModelCell.spec.ts` (5), `views.spec.ts` (3) |
| Environment | isolated Docker (backend 127.0.0.1:8080, frontend :8081, postgres, redis); Python 3.12.10, pytest 9.2.0; vitest 2.1.9; base commit `e18ca65` |
| Status | **PASS — e2e 31/31, unit 8/8; see §8** |

## 1. Scope

Executed the S-04 e2e suite (UC-E2E-005) against the **built** frontend per the
Q-01 plan (§6), re-ran the S-01 regression (UC-E2E-003 e2e + S-01 unit/component
tests), and recorded per-test PASS/FAIL with observed cell states. Per the plan a
SKIP is not pass evidence — every recorded test was **executed and passed** on a
healthy, seeded stack.

## 2. Environment

- Stack: `aitasktracker-backend/frontend/postgres/redis` (compose) on 127.0.0.1:8080/8081; `GET /api/health` 200 (`postgres ok, redis ok, ollama up`).
- Seed: deterministic 5-task corpus (`GET /api/tasks` total=5), verified before the live run.
- Note: `pytest_sessionfinish` tears the stack down unless `E2E_KEEP_STACK=1`. The
  final combined run used `E2E_KEEP_STACK=1`. Two earlier standalone S-04 runs
  failed/skipped only because a prior session had torn the stack down — environment,
  not product; superseded by the final passing run (§3) and results JSONs (§8).

## 3. S-04 e2e (UC-E2E-005) — 7/7 PASSED

Final combined run (S-04 + S-01, one session): `31 passed in 1.28s`.
Evidence: `tests/e2e/results/UC-E2E-005.json` → `status: pass`, `{total 7, pass 7, fail 0, skipped 0}`.

| Test | Result | Assertions / observed |
|---|---|---|
| `test_week_view_days` | **PASS** | week 11-09..15 = 7 cells; Tue 11-10 = {TASK-001, TASK-002}; anchor Mon 11-09 empty, not `outside` |
| `test_week_cell_tasks_match_dataset` | **PASS** | per-day task sets match dataset (11-10: 2, 11-11: 3) |
| `test_day_view_duration_semantics` | **PASS** | day 11-11 count = 3; durations per dataset |
| `test_month_view_task_counts` | **PASS** | month grid 28–42 cells; 11-10 = 2, 11-11 = 3 |
| `test_day_view_model_column` | **PASS** | R-015 cell contract (§3.1) |
| `test_built_frontend_has_model_cell` | **PASS** | served SPA bundles + `ModelCell-*.js` chunk contain the `model-cell` marker (built-frontend / stale-image guard) |
| `test_invalid_period_400` | **PASS** | missing/invalid `date` → 400 `INVALID_PERIOD` |

### 3.1 Observed cell states (R-015) — `test_day_view_model_column`

Expectations read only from dataset fixtures; `models` from `GET /api/tasks`
(order = total tokens desc, name tie-break, R-014 rule reused).

| Case | Task (fixture) | Observed `models` | Cell visible | `title`/`aria-label` |
|---|---|---|---|---|
| multi | TASK-003 (11-11) | `["qwen3.8:27b","llama3.1:8b"]` | `qwen3.8:27b` + `+1` | `Модель: qwen3.8:27b, llama3.1:8b` |
| multi | TASK-004 (11-11) | `["qwen3.8:27b","llama3.1:8b"]` | `qwen3.8:27b` + `+1` | `Модель: qwen3.8:27b, llama3.1:8b` |
| multi | TASK-005 (11-11) | `["qwen3.8:27b","llama3.1:8b"]` | `qwen3.8:27b` + `+1` | `Модель: qwen3.8:27b, llama3.1:8b` |
| multi | TASK-001 (11-10) | `["qwen3.8:27b","llama3.1:8b"]` | `qwen3.8:27b` + `+1` | `Модель: qwen3.8:27b, llama3.1:8b` |
| empty | TASK-002 (11-10) | `[]` | `Нет данных` (no `+N`) | `Модель: Нет данных` |
| column | both | — | after `Задача` | fixture `column_after == "Задача"` asserted |

Assertions per row: multi-model → `got == full_list`, `got[0] == primary`, `plus_n`
starts with `+`; empty → `got == []`. Rendering (primary, `+N`, full ordered list in
`title`/`aria-label`, «Нет данных») is `ModelCell.vue` (shared S-01/S-04), asserted at
component level (§5) and in-view; the e2e layer proves the **built SPA** ships the cell
and the API data behind it is correct.

## 4. S-01 regression e2e (UC-E2E-003) — 24/24 PASSED

Evidence: `tests/e2e/results/UC-E2E-003.json` → `status: pass`, `{total 24, pass 24, fail 0, skipped 0}`.

| Group | n | Result |
|---|---|---|
| `test_list_shows_five_tasks_with_seed_titles` | 5 | PASS (5 tasks, RU titles, durations R-009) |
| `test_list_task_status_is_canonical` | 5 | PASS (canonical RU status R-006) |
| `test_detail_task_001_actions_and_chosen_solution` | 1 | PASS (R-003/R-005) |
| `test_detail_solutions_match_seed` | 5 | PASS (R-004/R-005; empty TASK-004/005) |
| `test_detail_task_005_has_explicit_empty_state` | 1 | PASS |
| `test_detail_token_block_matches_seed_usage` | 1 | PASS (R-012) |
| `test_list_models_field_order_and_empty_state` | 5 | PASS (**S-01 model column R-014 — primary/`+N`/«Нет данных» unchanged**) |
| `test_models_by_task_aggregation_math_offline` | 1 | PASS |

S-01 behavior is **unchanged and green**.

## 5. S-01 unit/component — 8/8 PASSED

`vitest run tests/unit/ModelCell.spec.ts tests/unit/views.spec.ts` (jsdom): **2 files, 8 passed**.
- `ModelCell.spec.ts` (5/5): empty → «Нет данных» + `Модель: Нет данных`; single → primary, no `+N`; two → primary + `+1` + full `title`/`aria-label`; custom prefix; long name ellipsis + full title.
- `views.spec.ts` (3/3): S-01 `DashboardView` renders shared ModelCell (primary + `+1` + aria); S-04 `CalendarDayView` renders «Модель» column immediately after «Задача» with the shared cell, and «Нет данных» for a no-usage task.
- Benign: `Vue warn: Failed to resolve component: router-link` (jsdom; views use `router-link`, harness doesn't register it) — does not affect asserted cells; minor harness-hygiene item.

## 6. Defects

**None.** No product defect. The two earlier flaky S-04 observations were
environment-caused (stack torn down by a previous session's `pytest_sessionfinish`
→ next standalone run saw a down/empty stack); resolved by re-seeding +
`E2E_KEEP_STACK=1`. Recorded as operational notes, not defects.

## 7. Residual risks

1. **Visual rendering** (font/ellipsis, exact `title`/`aria-label` in a real browser over the S-04 day table) is covered at component level and by the built-frontend `model-cell` marker, not by a full-browser DOM walk of `CalendarDayView`. Text + a11y states are automated; a visual pass remains a separate (lead/vision) item. Low.
2. **jsdom `router-link` warning** in `views.spec.ts` — harness-only; recommend registering a stub. Minor.
3. **Stack lifecycle coupling**: default `pytest_sessionfinish` teardown can yield misleading skip/fail runs when interleaving suites. Mitigated by running S-04 + S-01 in one session with `E2E_KEEP_STACK=1`. Operational, low.

## 8. Verdict (e2e) and evidence

**E2E verdict: PASS (accept for e2e).**

- `tests/e2e/results/UC-E2E-005.json` — `pass`, 7/7 (S-04).
- `tests/e2e/results/UC-E2E-003.json` — `pass`, 24/24 (S-01 regression).
- `vitest` — 8/8 unit/component (S-01 `ModelCell` + views).
- Dataset validator — 0 errors, 0 warnings.
- Built-frontend guard — `test_built_frontend_has_model_cell` PASS (served SPA contains `model-cell`).

S-01 unchanged and green; S-04 cell states (primary, `+N`, full list in
`title`/`aria-label`, «Нет данных», column after «Задача») match R-015 and the UX
spec. No defects. Final accept/reject for the whole task is in `docs/qa/review-T-039.md` (C2).

**CHECKPOINT CP-2 reached** — e2e report complete; verdict input recorded. C1 done;
C2 (code review + final verdict) is the next chunk.
