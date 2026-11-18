# BUG-008: UC-E2E-004 AI-search e2e test failures (helper tuple contract + HTTP 503)

| Field | Value |
|---|---|
| id | BUG-008 |
| title | UC-E2E-004 AI-search e2e tests fail: `_post_json` returns a tuple on non-200 (TypeError/AttributeError) + HTTP 503 on English query |
| status | open |
| severity | P2 |
| reported_at | 2026-11-18T19:57:47+03:00 |
| reporter | QA (T-027 Q-02 closure) |
| affected | `tests/e2e/uc_e2e_004_006/test_uc_e2e_004_ai_search.py` — `test_semantic_query_top_k`, `test_unrelated_query_empty`, `test_response_time_bound`, `test_english_query_valid_200` |
| requirements | n/a (e2e helper/test issue, not an R-014 behavior) |
| use_cases | UC-E2E-004 |
| tasks | T-027 (linked, not fixed) |
| duplicate_of | - |
| commit | 9bdbaec (e2e run evidence) |
| issues | - |

## Observed behavior

Full e2e suite: **55 PASS / 4 FAIL** (all four in UC-E2E-004 AI search).

- `test_semantic_query_top_k` — `TypeError: tuple indices must be integers` — `_post_json` returned a `(status, body)` tuple on a non-200 response.
- `test_unrelated_query_empty` — `AttributeError: 'tuple' object has no attribute 'get'` — same helper contract issue.
- `test_response_time_bound` — `AttributeError: 'tuple' object has no attribute 'get'` — same helper contract issue.
- `test_english_query_valid_200` — HTTP 503 from `/api/search` for an English query while Ollama is up.

## Expected behavior

- The `_post_json` helper returns a parsed JSON body (dict) on 2xx and raises / returns a structured error on non-2xx, so tests can assert on the body consistently.
- `GET /api/search?q=<English query>` returns HTTP 200 with a valid result set when Ollama is healthy.

## Reproduction

```
python -m pytest tests/e2e/uc_e2e_004_006/test_uc_e2e_004_ai_search.py -q
```

Run with the local Docker stack up and Ollama available. The four tests above fail; the rest of the suite passes.

Evidence: `tests/e2e/results/UC-E2E-004.json`, `tests/e2e/results/e2e-results.json` (commit `9bdbaec`).

## Cause / impact

- **Root cause (helper):** `_post_json` in the UC-E2E-004 test module returns a `(status, body)` tuple on non-200 responses instead of raising or returning a structured error. Tests that expect a dict body then hit `TypeError` / `AttributeError`.
- **Root cause (503):** The search endpoint returns 503 for an English query while Ollama is up. Likely a timeout or embedding failure that surfaces as 503 rather than a clean empty result.
- **Impact:** The full e2e suite cannot report 100 % pass. The four failures are unrelated to R-014 (model column) and do not affect the UC-E2E-003 acceptance evidence (24/24 PASS).
- **Severity rationale:** P2 — blocks a clean full-suite signal; no production logic defect in the feature under test.

## Fix summary

Not yet fixed (status `open`). The fix requires:
1. Fix `_post_json` in `tests/e2e/uc_e2e_004_006/test_uc_e2e_004_ai_search.py` (or a shared helper) to return a consistent body (dict) on 2xx and raise on non-2xx, matching the pattern used in other e2e test modules.
2. Investigate and fix the HTTP 503 on English queries (possibly an Ollama timeout or search-service issue).

## Verification

Outstanding — a fix must make `python -m pytest tests/e2e/uc_e2e_004_006/test_uc_e2e_004_ai_search.py -q` exit 0 with all four tests passing.

## Postmortem (P0/P1)

Not applicable (P2).

## Links

- T-027 (`docs/tasks/T-027-qa-model-column.md`) — linked; the failures were observed during T-027 QA and are out of scope for R-014.
- `docs/qa/e2e-report-task-list-model.md` — full-suite failure table.
- `docs/qa/review-T-027.md` — recommendation to triage as a separate task.
- Evidence: `tests/e2e/results/UC-E2E-004.json` (commit `9bdbaec`).

## Log

- 2026-11-18: reported by QA during T-027 Q-02 closure; 4 UC-E2E-004 failures observed in the full-suite run; status `open` (linked to T-027, not fixed there).
