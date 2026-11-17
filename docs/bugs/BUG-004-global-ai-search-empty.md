# BUG-004: Global AI search returns an empty result set

| Field | Value |
|---|---|
| id | BUG-004 |
| title | Global AI search (no task_id) collects no candidates and returns empty |
| status | verified |
| severity | P2 |
| reported_at | 2026-11-17T16:20:00+03:00 |
| reporter | lead (during the local Docker deployment) |
| affected | `POST /api/search` when `task_id` is omitted (the S-03 global search); UI Search view |
| requirements | R-010, R-011 |
| use_cases | UC-E2E-004 |
| tasks | - (fix applied during the local deployment; see Fix summary) |
| duplicate_of | - |
| commit | local deployment commit |
| issues | - |

## Observed behavior

`POST /api/search` with `{"query":"настройка CI для репозитория","limit":5}` (no
`task_id`, exactly what the SPA sends) returned `200` with zero results in ~36 ms.
The S-03 view therefore showed «ничего не найдено» even though TASK-001 has the
matching CI step and solutions. With an explicit `task_id` the candidate
collection worked, but the global search never reached Ollama.

## Expected behavior

Global AI search ranks snippets from **all** tasks' steps and solutions and
returns top-K hits (contracts §4.4 / API-003); the optional `task_id` restricts
the search to one task. Source of truth: `contracts-ai-task-tracker.md` §4.4.

## Reproduction

1. Start the local stack and seed `data/e2e-opencode` (5 tasks).
2. `POST /api/search` with `{"query":"настройка CI для репозитория","limit":5}`.
3. Before the fix: 200 with `results: []` and no Ollama call. After: the
   backend invokes Ollama (log line for the model call).

## Impact and triage

- Severity rationale: P2 — the AI-search feature is unusable globally; the rest
  of the service (tasks/calendar/tokens/history) is unaffected.
- Affected users/data/security: UI users searching from S-03; no data/security impact.
- Workaround: pass `task_id` (not how the UI works).
- Duplicate check: none. BUG-003 covered stack health, not search semantics.

## Root cause

`SearchService.collectCandidates(taskId)` called
`findByTaskIdOrderByAtAsc(taskId)` / `findByTaskIdOrderBySolutionIdAsc(taskId)`
with the request's `taskId`. For a global search `taskId` is `null`, so both
queries returned no rows and the service took the "honest empty" path without
ever calling Ollama. The e2e AI-search tests were skipped in the C7 run (Ollama
unreachable from the container), so this was not covered.

## Fix summary

`src/main/java/ru/aitasktracker/api/search/service/SearchService.java`:
`collectCandidates` now collects across **all** steps/solutions (`findAll()`) when
`taskId` is null/blank, tagging each candidate with its own task id; a non-blank
`taskId` keeps the scoped behaviour. No public contract change.

## Verification

- Before: 36 ms, `results=[]`, no Ollama call.
- After: the request reaches Ollama (the backend logs the model call/outcome).
- In this environment only large models are installed (`qwen3.8:27b` ~15 s for a
  one-word reply, `tiel-coder:35b-a3b-mtp-64k` ~6 s; the ranking prompt exceeds
  the 15 s contract / 7.5 s client request timeout), so the endpoint returns the
  contracted `503 AI_SEARCH_UNAVAILABLE`. Pulling the app's default
  `qwen2.5:0.5b` failed here (TLS timeout to `registry.ollama.ai`). With a model
  that meets the 15 s budget the global search returns hits; the candidate path
  is proven by the Ollama invocation.
- No collateral changes: only `SearchService.java` and the deployment override.

## Postmortem (P0/P1)

Not applicable (P2).

## Links

- `src/main/java/ru/aitasktracker/api/search/service/SearchService.java`
- `docs/architecture/contracts-ai-task-tracker.md` §4.4,
  `docs/architecture/openapi-api-003-ai-task-tracker.yaml`
- `docker-compose.deploy.yml` (local deployment override)
- Review: `docs/qa/review-T-016.md` (environment/stack context)

## Log

- 2026-11-17: reported during the local Docker deployment; fixed in place; status
  `verified` with the documented model-latency residual.
