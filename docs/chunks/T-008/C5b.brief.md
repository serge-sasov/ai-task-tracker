# C5b: API-003 search and Ollama client

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | `src/main/java/ru/aitasktracker/api/search/**`, `src/main/java/ru/aitasktracker/search/**`, `tests/java/ru/aitasktracker/api/search/**`, `tests/java/ru/aitasktracker/search/**` |
| budget | 12 KB write, 40 KB session, stop at 65% |

## Goal

Implement `POST /api/search` (API-003) and a local-only Ollama client with a
bounded 15 s timeout and the `AI_SEARCH_UNAVAILABLE` 503 path, in new packages
only — no file outside `owns` changes.

## Inputs

- `docs/architecture/openapi-api-003-ai-task-tracker.yaml` (whole file).
- `docs/architecture/contracts-ai-task-tracker.md` lines 86-291 (§4 REST API,
  §5 error model, §6 Ollama contract).
- Existing conventions: `api/dto/ApiError.java`, `api/controller/ApiErrorAdapter.java`
  (maps `ApiException` -> RU envelope), `api/exception/ApiException.java`
  (abstract: `code()` / `status()`), `api/service/OllamaChecker.java` (HTTP
  client usage), `api/calendar/*` (controller/service/DTO style).
- `config/TrackerProperties.java` exposes `tracker.ollama.base-url` and
  `tracker.search-top-k` (default 5).

## Work

- `search/OllamaException.java` — RuntimeException, thrown by the client on any
  LLM failure (timeout, connection error, malformed response).
- `search/OllamaClient.java` — local-only (`baseUrl`) HTTP client, connect + read
  total bounded to 15 s, defensive JSON parsing of the `/api/chat` response.
  Throws `OllamaException` on any failure.
- `api/search/dto/SearchRequest.java`, `SearchHit.java`, `SearchResponse.java`
  (must carry the `score` field on each hit).
- `api/search/EmptyQueryException.java` — ApiException 400 `INVALID_QUERY`.
- `api/search/SearchUnavailableException.java` — ApiException 503
  `AI_SEARCH_UNAVAILABLE` with the canonical RU message.
- `api/search/service/SearchService.java` — `@Service`, top-K selection +
  synthesis, calls the Ollama client. `@Value` constructor.
- `api/search/controller/SearchController.java` — `@RestController`,
  `POST /api/search`, validates, delegates.

Behaviour: empty/blank `query` -> 400 `INVALID_QUERY`; Ollama failure -> 503
`AI_SEARCH_UNAVAILABLE` «AI-поиск недоступен: LLM не отвечает. Попробуйте позже.»
(never a 500). Results ordered by score desc, top-K = 5 (configurable), each hit
carries non-empty `text`/snippet and a `score`.

## Acceptance

- [ ] SearchResponse has `total`, `results[]` each with `task_id`, `kind`,
      `text`, `score`; top-K respected.
- [ ] Ollama failure -> 503 `AI_SEARCH_UNAVAILABLE` RU envelope.
- [ ] Empty query -> 400 `INVALID_QUERY`.
- [ ] OllamaClient uses localhost base URL, bounded timeout, defensive parse.
- [ ] `mvn clean test` green; no TODO/FIXME; no secrets.

## Stop rule

Stop at the budget or when a shared file outside `owns` must change; record with
`chunk.ps1 fail`. Do not widen `owns`.

## Return

At most ten lines: produced paths, test counts, state, any issue id.
