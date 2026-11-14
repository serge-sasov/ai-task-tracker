# C5b: API-003 search and Ollama client

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | `src/main/java/ru/aitasktracker/api/search/**`, `.../search/**`, matching test packages |
| budget | 12 KB write, 40 KB session, stop at 65% |

## Goal

Implement `POST /api/search` and the local Ollama client with a bounded timeout
and the `AI_SEARCH_UNAVAILABLE` 503 path.

## Inputs

- `docs/architecture/openapi-api-003-ai-task-tracker.yaml` (whole file, 3.3 KB).
- `docs/architecture/contracts-ai-task-tracker.md` lines 86-291 (sections 4, 5,
  and 6 only; do not read the rest of the file).

## Work

- `api/search/`: controller and service, top-K 5, synthesized answer.
- `search/`: Ollama HTTP client, `localhost` only, connect/read timeout bounded to
  15 s total, defensive parsing.
- On LLM failure: 503 `AI_SEARCH_UNAVAILABLE` with the RU message; never a 500.
- Tests must not require a live LLM: stub the client, assert timeout and 503.

## Acceptance

- [ ] Request/response match API-003, including the score field.
- [ ] Timeout is enforced and the 503 envelope is correct.
- [ ] Tests deterministic and offline; `mvn clean test` green.

## Stop rule

Stop at the budget or when a shared file outside `owns` must change; record with
`chunk.ps1 fail` and a note.

## Return

At most ten lines: produced paths, test counts, state, any issue id.
