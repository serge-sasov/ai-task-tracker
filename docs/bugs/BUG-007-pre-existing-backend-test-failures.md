# BUG-007: Pre-existing backend test failures (SearchServiceTest, TokenServiceTest)

| Field | Value |
|---|---|
| id | BUG-007 |
| title | 5 pre-existing backend unit-test failures (SearchServiceTest ×3, TokenServiceTest ×2) block a clean full-suite run |
| status | open |
| severity | P2 |
| reported_at | 2026-11-18T13:49:33+03:00 |
| reporter | harness lead / coder (during T-024 I-01 closure) |
| affected | `SearchServiceTest` (ranksAndTopKSelection, respectsTopK, ollamaFailureReturnsSearchUnavailable), `TokenServiceTest` (emptyPeriodReturnsZeroTotalsAndNoData, allPeriodReturnsHasDataWithRows) |
| requirements | n/a (test-only / pre-existing; not an R-014 behavior) |
| use_cases | n/a |
| tasks | T-024 (linked, not fixed — these failures are out of scope for the model column) |
| duplicate_of | - |
| commit | d4dde64 (base where the failures were reproduced) |
| issues | - |

## Observed behavior

`mvn -f backend/pom.xml test` reports 5 failures, all unrelated to the T-024 model-column change:

- `SearchServiceTest.ranksAndTopKSelection` — `AssertionFailedError: expected: <3> but was: <0>`.
- `SearchServiceTest.respectsTopK` — `AssertionFailedError: expected: <1> but was: <0>`.
- `SearchServiceTest.ollamaFailureReturnsSearchUnavailable` — `AssertionFailedError: Expected SearchUnavailableException to be thrown, but nothing was thrown.`
- `TokenServiceTest.emptyPeriodReturnsZeroTotalsAndNoData` — `NullPointerException: Cannot invoke "Page.getContent()" because the return value of "TaskRepository.findAll(Pageable)" is null` (TokenService.build line 87).
- `TokenServiceTest.allPeriodReturnsHasDataWithRows` — same `NullPointerException` at `TokenService.build` line 87 (mock `TaskRepository.findAll(Pageable)` returns null).

## Expected behavior

A clean full backend suite: `mvn -f backend/pom.xml test` exits 0 with all unit tests green.

## Reproduction

```
mvn -f backend/pom.xml test
```

Reproduced at base commit `d4dde64` (before the T-024 change was committed). Evidence:
`backend/target/surefire-reports/ru.aitasktracker.api.search.service.SearchServiceTest.txt`
and `backend/target/surefire-reports/ru.aitasktracker.api.tokens.TokenServiceTest.txt`.

The T-024 change touches `TaskListItem`, `TaskQueryService`, `TaskQueryServiceTest`,
`src/web/src/api/types.ts`, `src/web/src/views/DashboardView.vue` only — none of which
are exercised by the failing tests.

## Cause / impact

- Root cause (pre-existing, test-only): the test mocks / fixtures for `SearchService`
  and `TokenService` do not return the expected search rankings / token rows — the
  search tests return an empty result set, and the token tests hit a `null` page from
  an unstubbed `TaskRepository.findAll(Pageable)`.
- Impact: blocks a clean "full backend suite green" claim only. It does not affect the
  R-014 model-column behavior, which is verified by the targeted `TaskQueryServiceTest`
  (15/15 pass) and the `npm run build` of `src/web` (pass).
- Severity rationale: P2 — a failing CI signal that should be fixed before a release,
  but no production/data/logic defect and no impact on the feature under test.

## Root cause

To be determined by the fixing task (test-fixture / mock gaps in `SearchServiceTest`
and `TokenServiceTest`; `TokenService.build` line 87 assumes a non-null page from
`TaskRepository.findAll(Pageable)`).

## Fix summary

Not yet fixed (status `open`).

## Verification

Outstanding — a fix must make `mvn -f backend/pom.xml test` exit 0.

## Postmortem (P0/P1)

Not applicable (P2).

## Links

- T-024 (`docs/tasks/T-024-implement-model-column.md`) — linked, the failures are
  pre-existing and out of scope for the model-column change.
- Evidence: `backend/target/surefire-reports/ru.aitasktracker.api.search.service.SearchServiceTest.txt`,
  `backend/target/surefire-reports/ru.aitasktracker.api.tokens.TokenServiceTest.txt`.

## Log

- 2026-11-18: reported by the harness lead/coder during T-024 closure; 5 pre-existing
  failures reproduced at base `d4dde64`; status `open` (linked to T-024, not fixed there).
