# QA review T-027: S-01 model column

| Field | Value |
|---|---|
| Task | T-027 (Q-02) |
| Reviewer | QA (lead fallback after harness-qa timeout) |
| Date | 2026-11-18 |
| Scope | R-014 implementation, contracts, UX and e2e evidence |
| Plan | `docs/qa/e2e-plan-task-list-model.md` |
| Report | `docs/qa/e2e-report-task-list-model.md` |

## Verdict

**Verdict: PASS for R-014 with a documented regression deviation** — the S-01
model column and `GET /api/tasks.models` behave per contract; targeted UC-E2E-003
passes 24/24. The full suite has 4 unrelated UC-E2E-004 AI-search failures that
must be handled separately.

## Code review

- `TaskListItem` adds an immutable `List<String> models` (`Collections.unmodifiableList`
  over `List.copyOf`) with a safe empty default; the existing 7-arg constructor is
  preserved. This is correct and null-safe.
- `TaskQueryService.toListItem` calls `tokenUsageRepository.findByTaskId` exactly once
  and feeds the same rows to `total(rows)` and `tokenModels(rows)`. The old
  `tokensTotal(String)` path is gone from the list flow; no duplicate query.
- `tokenModels` aggregates per model (input+output+cache), sorts total desc then name
  asc, and returns an empty list for no rows. The ordering matches contracts §4.2 and
  R-014.
- `TaskQueryServiceTest` adds ordering, tie-break, empty-usage, cache-included and
  defensive-immutability coverage; targeted suite 15/15 passed in T-024.
- `src/web/src/api/types.ts` adds `models?: string[]` for compatibility.
- `DashboardView.vue` places «Модель» immediately after «Название», uses `models ?? []`,
  renders the primary plus `+N`, puts the full ordered list in `title` and
  `aria-label`, and falls back to «Нет данных». The table remains responsive.
- No unrelated production changes were found in the T-024 diff.

## Contract and UX review

- Contracts §4.2, ADR §4, architecture traceability and OpenAPI API-001 agree on
  `models`, ordering and empty-array semantics; OpenAPI keeps `models` optional.
- The UI spec, S-01 screen states, Salt wireframe and offline prototype all show the
  model column, `+1`, tooltip/aria and «Нет данных»; no contradiction with R-014.
- The implementation does not add a database migration or a second token query.

## E2E review

- Targeted UC-E2E-003: 24 passed, 0 failed, 0 skipped; the new
  `test_list_models_field_order_and_empty_state` covers all five tasks including
  TASK-002 `[]`; the offline math test covers total-desc/name-asc and cache math.
- Full suite: 55 passed, 4 failed. All four failures are in UC-E2E-004 AI search:
  three are a `_post_json` helper contract bug (tuple returned on non-200) and one is
  an HTTP 503 for an English query. They are unrelated to R-014 and do not touch the
  task-list endpoint.
- The targeted run required a rebuilt backend image; the first run against the old
  image correctly exposed missing `models`. This is an environment note, not a code
  defect.

## Issues and deviations

- `BUG-007`: pre-existing backend unit failures (SearchServiceTest/TokenServiceTest);
  out of scope for R-014.
- `ISSUE-20261118-153622`: e2e runner compose/pytest invocation; workaround used
  (manual compose + forward-slash pytest).
- `ISSUE-20261118-185907`: harness-qa SSE timeout; this review is the lead fallback.
- Full-suite UC-E2E-004 failures: recommended follow-up bug/e2e fix; they do not
  block R-014 acceptance but must be visible to the user.

## Recommendation

Accept R-014 and close T-027 with the regression deviation recorded. Before final
feature closure, the UC-E2E-004 failures should be triaged as a separate bug/e2e
task (not silently absorbed by this feature).
