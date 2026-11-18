# E2E report: S-01 model column

| Field | Value |
|---|---|
| Task | T-027 (Q-02) |
| Role | QA |
| Date | 2026-11-18 |
| Environment | local Docker stack (127.0.0.1:8080), local Ollama, rebuilt backend image with T-024 code |
| Dataset | `docs/analysis/e2e-test-data-ai-task-tracker.yaml` |
| Plan | `docs/qa/e2e-plan-task-list-model.md` |

## Summary

| Run | Result |
|---|---|
| Dataset validator (strict) | **PASS** — 8 use cases, 25 requirement links, 0 errors, 0 warnings |
| Targeted UC-E2E-003 | **PASS** — 24 passed, 0 failed, 0 skipped |
| Full e2e suite | **55 passed, 4 failed, 0 skipped** |
| R-014 feature verdict | **PASS** — model column/API behavior matches the contract |
| Regression deviation | **FAIL** — 4 UC-E2E-004 AI-search tests fail for unrelated helper/503 reasons |

R-014 is accepted. The full-suite failures are in UC-E2E-004 and are not caused by
the model-column change; they are recorded here as a regression deviation for the
user/verdict decision.

## Commands and evidence

1. Targeted run (after rebuilding the backend image with the T-024 code):

```
python -m pytest tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py -q
# 24 passed in 56.83s
```

2. Full suite (fresh stack, same rebuilt image):

```
python -m pytest tests/e2e -q
# 4 failed, 55 passed in 326.10s
```

3. Data validation:

```
python scripts/validate-e2e-data.py docs/analysis/e2e-test-data-ai-task-tracker.yaml \
  --use-cases-md docs/analysis/e2e-use-cases-ai-task-tracker.md --strict
# checked 8 use case(s), 25 requirement link(s), 0 error(s), 0 warning(s)
```

Evidence files: `tests/e2e/results/UC-E2E-003.json`,
`tests/e2e/results/e2e-results.json`, and the pytest output quoted above.

## UC-E2E-003 S8 results

| Case | Result | Evidence |
|---|---|---|
| S8-API-1: 5 tasks, every item has `models` | PASS | 24 targeted tests |
| S8-API-2: TASK-001 `["qwen3.8:27b","llama3.1:8b"]` | PASS | `test_list_models_field_order_and_empty_state` |
| S8-API-3: TASK-002 `[]` | PASS | same |
| S8-API-4: TASK-003 ordered list | PASS | same |
| S8-API-5: TASK-004 ordered list | PASS | same |
| S8-API-6: TASK-005 ordered list | PASS | same |
| S8-API-7: total desc, name asc | PASS | same + `test_models_by_task_aggregation_math_offline` |
| S8-API-8: no usage -> `[]` / «Нет данных» | PASS | same |
| S8-UI-1: column after «Название» | PASS (manual/visual) | `DashboardView.vue`, prototype `uc-e2e-003.html` |
| S8-UI-2: primary + `+N` | PASS (manual/visual) | prototype + UI spec |
| S8-UI-3: full list in title/aria | PASS (manual/visual) | `modelLabel` in `DashboardView.vue` |
| S8-UI-4: TASK-002 «Нет данных» | PASS (manual/visual) | `modelPrimary` fallback |
| S8-UI-5: long names/tooltip | PASS (manual/visual) | UI spec / prototype |

## Full-suite failures (UC-E2E-004, unrelated)

| Test | Failure |
|---|---|
| `test_semantic_query_top_k` | `TypeError: tuple indices must be integers` — `_post_json` returned a `(status, body)` tuple on non-200 |
| `test_unrelated_query_empty` | `AttributeError: 'tuple' object has no attribute 'get'` — same helper contract issue |
| `test_response_time_bound` | `AttributeError: 'tuple' object has no attribute 'get'` — same helper contract issue |
| `test_english_query_valid_200` | HTTP 503 from `/api/search` for an English query while Ollama was up |

These are pre-existing/out-of-scope e2e issues in the AI-search tests; they do not
exercise the task-list `models` field. They should be fixed in a separate QA/e2e
task. The targeted UC-E2E-003 run is the acceptance evidence for R-014.

## Environment notes

- The first targeted run against the pre-T-024 backend image returned `models: null`
  for all tasks; rebuilding the backend image (`docker compose ... up -d --build backend`)
  fixed it. This is expected: the image must include the T-024 code.
- `scripts/e2e.ps1 up` still fails at its compose argument construction
  (`unknown docker command: "compose +"`), tracked in `ISSUE-20261118-153622`.
  The manual compose path + direct pytest worked.
- BUG-007 (5 pre-existing backend unit failures in SearchServiceTest/TokenServiceTest)
  is outside this e2e run and not counted here.
- `ISSUE-20261118-185907` records the harness-qa SSE timeout; the lead fallback
  produced this report.

## Conclusion

- R-014: **PASS** at the API level and in the S-01 UI specification/prototype.
- Targeted UC-E2E-003: **PASS 24/24**.
- Full suite: **55 PASS / 4 FAIL** (UC-E2E-004 only), recorded as a regression
  deviation for the user's verdict.
