# QA review — T-039 (Q-02, S-04 day-view model column)

| Field | Value |
|---|---|
| Task | T-039 (Q-02, chunk C2 — code review + verdict) |
| Role | qa (`harness-qa`) |
| Date | 2026-11-22 |
| Scope | Code/text review of the shared model cell and its S-01/S-04 usage; final verdict |
| Inputs | `ModelCell.vue`, `DashboardView.vue`, `CalendarDayView.vue` (src/web/src), UX spec §3.2/§3.3/§6, R-014/R-015, `docs/qa/e2e-report-s04-model.md` (C1), `ModelCell.spec.ts`, `views.spec.ts` |
| Status | **ACCEPT** — §7 |

## 1. Scope of review

Read-only review of the shared cell and both consumer views, vs card acceptance 2 and 4:

- `src/web/src/components/ModelCell.vue` (60 lines, full read) — cell contract.
- `src/web/src/views/DashboardView.vue` (297 lines) — S-01 usage (R-014).
- `src/web/src/views/CalendarDayView.vue` (282 lines) — S-04 usage (R-015).
- Specs: UX §3.2 (R-014), §3.3 (R-015), §6; runtime evidence: C1 report (S-04 7/7, S-01 regression 24/24, unit 8/8) — referenced, not re-run.

Out of scope: visual rendering (lead/vision), backend API/DB (unchanged per R-015).

## 2. ModelCell.vue

File: `src/web/src/components/ModelCell.vue`. All checks **pass**:

| # | Check | Evidence | Result |
|---|---|---|---|
| 2.1 | Primary = first element of `models` | L24-26 | PASS |
| 2.2 | `+N` only when >1 model | L28-30 `moreCount`; L8 `v-if` | PASS |
| 2.3 | Full ordered list in `title`+`aria-label` | L4-5 `:title="fullLabel"`; L32-35 join(', ') in API order | PASS |
| 2.4 | «Нет данных» when empty | L25/L34 fallbacks | PASS |
| 2.5 | RU text, `title`+`aria-label` | L19 prefix «Модель»; matches UX §3.2 L72, §6 L122 | PASS |
| 2.6 | Long names: ellipsis, full value in tooltip | L48-54 `text-overflow:ellipsis`; L7 `:title`; UX §3.2 L73 | PASS |
| 2.7 | No hardcoded data | only props; RU literals are labels | PASS |
| 2.8 | Ordering (tokens desc, name tie-break) | backend-owned per R-014; component passes order through — correct | PASS |

Coverage: `ModelCell.spec.ts` 5/5 incl. exact `title`/`aria-label` strings. No defects.

**Info (not defect):** L19 prefix prop «Модель» is the only one used by both views (UX §3.3 L79: same labels as S-01); the «Модели» unit case is speculative, harmless.

## 3. DashboardView.vue (S-01) — unchanged

File: `src/web/src/views/DashboardView.vue`.

- L56 header «Модель»; column order (L55-59) = UX §3.2 L69.
- L72: `<ModelCell :models="task.models" />` — shared component, one prop, no local logic.
- L93/L98 import/registration; `models?: string[]` (`api/types.ts` L21), `?? []` covers undefined.

**Conclusion:** S-01 behavior unchanged — same component/props/contract, no S-04 additions. Runtime: C1 §4 S-01 regression 24/24 incl. `test_list_models_field_order_and_empty_state`; C1 §5 `views.spec.ts` S-01 PASS.

## 4. CalendarDayView.vue (S-04) — UX spec + R-015

File: `src/web/src/views/CalendarDayView.vue`.

| # | Requirement | Evidence | Result |
|---|---|---|---|
| 4.1 | «Модель» column right after «Задача» | L35-36 `<th>Задача</th><th>Модель</th>` (L35-40 order = UX §3.3 L77) | PASS |
| 4.2 | Cell identical to S-01 | L49 `<ModelCell :models="task.models" />` — same as DashboardView L72 | PASS |
| 4.3 | Primary + `+N` | delegated to `ModelCell` (§2.1-2.2) | PASS |
| 4.4 | `title`/`aria-label` full list | delegated (§2.3) | PASS |
| 4.5 | «Нет данных» | delegated (§2.4) | PASS |
| 4.6 | Same labels/tokens as S-01 | same component/default prefix/styles; no new CSS tokens | PASS |
| 4.7 | No new ordering rule | no models-sorting in view (L157 sorts tasks only); backend-owned per R-015 L158-159 | PASS |
| 4.8 | Data: `GET /api/tasks` returns `models` | L152-156 `tasksApi.list`; `api/types.ts` L21; no API change | PASS |

Cross-view: both bind the same component with the same prop (DashboardView.vue:72 = CalendarDayView.vue:49), identical header «Модель» (L56/L36), column after task-name per UX §3.2 L69 / §3.3 L77 — **no inconsistency found**. Runtime: C1 §3.1 — six observed cell cases PASS.

## 5. Defects

**No product defects** (C1 §6 likewise: none).

Info (no BUG-XXX required):
1. `views.spec.ts` jsdom warning `Failed to resolve component: router-link` — harness hygiene, already in C1 §7.2.
2. `ModelCell.vue:19` `modelLabelPrefix` unused by both views; «Модели» unit case speculative — no R-014/R-015 impact.

## 6. Final verdict

**VERDICT: ACCEPT**

| Acceptance (T-039 card) | Result | Evidence |
|---|---|---|
| 1. e2e report records S-04 execution + cell states | PASS (C1) | C1 §3 — UC-E2E-005 7/7; primary/`+N`/«Нет данных» observed |
| 2. review covers cell + both views; S-01 unchanged; S-04 = UX spec + R-015 | PASS (C2) | this file §2-§4 (ModelCell L4-35; DashboardView L56/L72; CalendarDayView L35-36/L49) |
| 3. S-01 regression re-run green | PASS (C1) | C1 §4 24/24; §5 8/8 |
| 4. verdict + evidence; T-029 acceptance met | PASS | this §6; column after «Задача», identical cell, no backend change |
| 5. defects recorded or none | PASS | §5 — none, 2 info notes |
| 6. CP-2 and CP-F declared | PASS | CP-2 reached (this + C1); CP-F pending lead preflight |

Evidence base: `tests/e2e/results/UC-E2E-005.json` (7/7), `UC-E2E-003.json` (24/24), vitest 8/8, dataset validator 0 errors, built-frontend `model-cell` marker PASS (C1 §8).

**CHECKPOINT CP-2 reached.** Q-02 artifacts complete: `docs/qa/e2e-report-s04-model.md` (C1) + `docs/qa/review-T-039.md` (C2). Ready for lead preflight + `stage-done`.
