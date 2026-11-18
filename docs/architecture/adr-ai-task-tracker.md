# ADR-0001: Local opencode activity tracker — modular monolith, file-based ingestion, Ollama AI search

| Field | Value |
|---|---|
| Status | proposed |
| Date | 2026-11-13 |
| Task | T-006, T-022 |
| Updated | 2026-11-18 (T-022: R-014, S-01 model list) |
| Related ADRs | ADR-0002 |

## Context

A local service must monitor user activities in opencode (the AI agent development environment):
what was done in each task, which solutions were proposed and chosen, how each task ended;
a calendar view of tasks with duration; AI-assisted task search; and token consumption
accounted per LLM model.

Forces and constraints:

- Requirements R-001..R-014 (functional) and R-020..R-030 (non-functional) in
  `docs/analysis/requirements-ai-task-tracker.md`; the fixed stack (Docker, Java, Postgres,
  Redis backend; Vue.js web frontend) is recorded in `docs/tasks/T-001-ai-task-tracker-feature-planning.md`.
- All open questions Q-001..Q-008 in `docs/analysis/questions-ai-task-tracker.md` were resolved
  on 2026-11-11 (CP-1) as "recommended default"; this ADR converts them into binding decisions.
- e2e scenarios UC-E2E-001..008 (`docs/analysis/e2e-use-cases-ai-task-tracker.md`) and UX spec
  screens S-01..S-06 with state sets and the polling banner
  (`docs/ux/ui-spec-ai-task-tracker.md`) constrain the API surface and error UX.
- The service is read-only with respect to opencode, must run fully on localhost, and must
  degrade visibly when the opencode data source or Ollama is unavailable.

## Decision

### 1. Architecture style and deployment

A modular monolith: a single Java 21 + Spring Boot 3 backend application with internal
modules (ingestion, domain, search, stats, api), PostgreSQL 16 as the source of truth,
Redis 7 for read caching and the ingestion sync lock, and a Vue 3 SPA built into a
`frontend` nginx container (4th service) that serves the SPA on host port 8080 and
reverse-proxies `/api/*` to `backend:8080`; the backend keeps the REST API. Exactly
four services (backend, postgres, redis, frontend) start with one `docker compose up`
on Windows/PowerShell, exposing only `localhost` ports (UC-E2E-001).

### 2. Data ingestion from opencode (Q-001)

The backend reads opencode session and message data from **local files**: JSON files under a
configurable root `OPENCODE_DATA_DIR` (environment variable; default — the opencode local
share directory on the host, bind-mounted read-only into the container). A scheduled
ingestion job runs **every 60 seconds** (well inside the R-026 target of ≤ 5 minutes
from activity end to visibility), guarded by a Redis lock so only one worker runs.
The service **never writes** to opencode and performs **no external network calls**.
A `source_status` (available / unavailable / stale) is computed per cycle and exposed
through the health endpoint and the UI banner (S-01 error/banner states).

### 3. Domain model: task mapping, statuses, duration (Q-002, Q-008)

**1 task = 1 opencode session** (natural key `session_id`; stable public id `task_id` —
the source-provided `task_id` takes precedence, e.g. seed/e2e `TASK-001`..`TASK-005`;
otherwise derived `TASK-<short hash of session_id>`, uniqueness enforced).
The `tasks` table stores name (first user message
or session title), `started_at`, `finished_at`, `status`, and a link back to the source
files (R-002, audit). **Duration = `finished_at` − `started_at`** (R-009); when
`finished_at` is absent the last event time in the session is used as the end time, and
the task is not treated as finished. **Final statuses** are derived from the session end
state: `Завершена` (normal completion), `Прервана` (aborted/interrupted), `Не завершена`
(session still open or end state missing); active sessions show as `Не завершена` until
closed (R-006). Steps are the session messages in chronological order (R-003).
Proposed/chosen solutions are stored in a `solutions` table (`proposed` / `chosen`
flag) with mandatory links to source message ids (R-004, R-005): **preferred source is
structured fields** in session data when present; otherwise the local LLM extracts them
from the session context and the extraction (model, prompt version, source messages) is
stored with the record for audit (Q-008).

### 4. Token accounting (Q-003)

Token usage is read from the **per-message and per-session usage fields** in opencode
data (model, input tokens, output tokens, cache tokens, time). Values are normalized into
a `token_usage` entity keyed by `(message_id, model)`; when only session-level totals are
present they are stored as session-level rows and not double-counted. Aggregations
(SUM of input/output/cache per model, overall and per selected period) are computed in
SQL over `token_usage` with a Redis read-through cache; this feeds the per-model widgets
(R-012, R-013, S-05). Missing usage fields yield zero/absent rows, never fabricated numbers.
The same rows feed the S-01 `models` array (R-014): total tokens desc, model name
asc; an empty array means no usage and the UI shows «Нет данных». No schema change
is needed.

### 5. AI search and solution extraction (Q-007, Q-008)

AI search uses **local Ollama only** (base URL `http://localhost:11434`, reachable inside
the compose network; configurable via `OLLAMA_BASE_URL`): an **embedding model** produces
vectors for tasks (title + steps + solutions digest, stored in a Postgres `vector` column
via the `pgvector` extension) and for the user query; candidate top-K (K ≥ 5, R-011) are
re-ranked by an optional **LLM** which also produces the per-result snippet/relevance
explanation (R-010, R-011, S-03). **Graceful degradation**: if Ollama is unavailable or a
call times out, the API returns a structured error (`AI_SEARCH_UNAVAILABLE`) and the UI
shows the explicit S-03 error state — the service itself stays up and list/calendar/token
features keep working. The same LLM is used for solution extraction (section 3); all LLM
calls are synchronous, bounded (default 15 s budget per R-023), and localhost-only (R-027).

### 6. Platform, locality, and security (Q-004, Q-006)

**Single-user, localhost only** (Q-004): no authentication, no user accounts, no
multi-tenancy; the app binds to `127.0.0.1`. **Russian UI only** (Q-006): all UI copy,
API error messages surfaced in the UI, and default statuses are in Russian; no i18n
framework in the first release. No secrets are stored in code, config, or logs (R-027).
Config is passed via environment variables (`OPENCODE_DATA_DIR`, `OLLAMA_BASE_URL`,
`INGEST_INTERVAL_SECONDS` default 60, `SEARCH_TOP_K` default 5) with safe localhost defaults.

### 7. Reliability and idempotent ingestion

Ingestion is **idempotent and restart-safe** (R-025): every upsert is keyed on natural
identifiers — `tasks(session_id)`, `steps(message_id)`, `token_usage(message_id, model)`,
`solutions(session_id, source_ref)` — using `INSERT ... ON CONFLICT DO UPDATE` inside a
single Postgres transaction per session batch; re-syncing the same session updates fields
instead of creating duplicates. Partial sync failures roll back per batch (no half-records).
Data survives container restarts because Postgres volumes are persisted. Retention is
**unlimited** (Q-005); the only deletion path is the **manual "Удалить историю" operation**
(S-06, R-029): a confirmed `DELETE /api/history` clears `tasks`, `steps`, `solutions`,
`token_usage` in one transaction (no partial deletion), and ingestion resumes from the
source files on the next cycle.

### 8. Observability and data lifecycle

The backend exposes a single `GET /api/health` endpoint covering liveness and readiness
(`status`, `checks.postgres`, `checks.redis`, `source_status`, `ollama_status`,
`last_ingest_at`); `checks.postgres`/`checks.redis` = `"ok"` when healthy (not
top-level `"up"`)
(R-028, UC-E2E-001). Structured JSON logs
(Spring Boot, no session content in logs beyond ids). Every UI surface handles the
typical failure modes explicitly: opencode source unavailable/stale → S-01 banner
"Источник данных (opencode) недоступен"; Ollama unavailable → S-03 error
"AI-поиск недоступен: LLM не отвечает" (R-028).

### 9. Module decomposition (for coder, R-02 handoff)

- `ingestion` — file reader (JSON), parser/normalizer, scheduler + Redis lock, upserts.
- `domain` — `Task`, `Step`, `Solution`, `TokenUsage` entities, status derivation,
  duration computation, history deletion service.
- `search` — Ollama client (embeddings + LLM), vector index maintenance, ranking.
- `stats` — token aggregation queries, period filters, cache.
- `api` — REST controllers (`/api/tasks`, `/api/tasks/{id}`, `/api/search`,
  `/api/calendar`, `/api/tokens`, `/api/history`, `/api/health`), error mapping.
- `web` — Vue 3 SPA (S-01..S-06), built and served by the `frontend` nginx container,
  with `/api` proxied to the backend.

## Alternatives

| Option | Pros | Cons | Why rejected |
|---|---|---|---|
| opencode SDK / HTTP API (local server) | structured data, event push possible | depends on opencode running and on an API/SDK version we do not control; adds a runtime coupling the requirement (C-3) forbids | file polling is fully autonomous, read-only, and works regardless of opencode process state (Q-001 default) |
| File watching (OS events) instead of polling | lower latency | unreliable across bind mounts / Docker on Windows, bursty | 60 s polling already meets R-026 (≤ 5 min) with far less complexity |
| Microservices (separate ingestion/search/api services) | independent scaling | overkill for single-user localhost; more Docker/ops surface, more failure modes | modular monolith keeps one `docker compose up`, one deploy, one set of health endpoints (R-020..R-022) |
| Dedicated vector DB (e.g. Qdrant/Milvus) | purpose-built search | extra service and data duplication; scale (10^3–10^5 rows) trivially handled by Postgres | Postgres + `pgvector` keeps one storage engine, one backup, one migration path (R-020, R-024) |
| Pure Postgres FTS/`pg_trgm` without embeddings | zero extra dependency | weak semantic matching, fails R-010 acceptance ("semantically matching query finds the task") | embeddings + optional LLM ranking satisfy R-010/R-011 while staying local (R-027) |
| Server-rendered Vue (Nuxt) or static-only UI | simpler pipeline | no SSR benefit for a localhost SPA; Nuxt adds a second runtime | Vue 3 SPA served by the backend is the simplest stack honoring R-021 |
| Regex/heuristic extraction of proposed/chosen solutions | fast, no LLM needed | brittle across opencode output formats; cannot satisfy R-004/R-005 acceptance reliably | local LLM extraction with source-message links is auditable and format-tolerant; structured fields are still preferred when present (Q-008) |

## Consequences

- **Positive:** one-command local stack (R-022); read-only, network-isolated ingestion (R-027,
  C-3); idempotent, restart-safe history with no duplicates (R-025); visible degradation
  instead of silent empty lists (R-028, UC-E2E-002/007); single storage engine for search
  and data (R-020, R-024).
- **Negative / risks:** (1) opencode JSON file format is not a public contract — parser
  must be defensive and version-tolerant, with unknown fields ignored and per-file parse
  errors logged, never fatal; (2) LLM extraction quality varies — mitigated by
  preferring structured fields, keeping source links, and marking extracted solutions
  with their provenance; (3) 60 s polling is a bounded approximation of real time —
  acceptable per R-026; (4) unlimited retention grows the vector table — mitigated by
  bounded top-K search and periodic re-embedding only for changed tasks.
- **What must change (components, interfaces, data):** new Postgres schema
  (`tasks`, `steps`, `solutions`, `token_usage`, `vector` column, `ingest_state`),
  Ollama endpoints (embeddings, chat) on localhost, REST contracts below, Vue SPA
   screens S-01..S-06, docker-compose with 4 services (backend, postgres, redis,
   frontend) + read-only host bind mount.

## Contracts and diagrams

**REST API (backend ↔ Vue SPA, JSON, UTF-8, Russian error texts):**

| Endpoint | Purpose | Key response fields / behavior |
|---|---|---|
| `GET /api/tasks` (page, `from`, `to`, `status`) | task list, S-01 | `{ items: [{task_id, name, started_at, finished_at, status, duration_s, tokens_total, models}] }`; `models` is ordered by total tokens desc, model name asc; empty array means no usage; status ∈ `Завершена/Прервана/Не завершена` |
| `GET /api/tasks/{id}` | task detail, S-02 | steps (chronological, with timestamps), solutions (`proposed`/`chosen` + source message refs), token totals per model |
| `POST /api/search` (body `{query, limit}`) | AI search, S-03 | top-K ≥ 5 `{task_id, snippet, score}`; 503 `AI_SEARCH_UNAVAILABLE` when Ollama is down |
| `GET /api/calendar?view=day\|week\|month\|year&date=YYYY-MM-DD` | calendar, S-04 | tasks on dates by start/interval, with duration; `year` returns per-month task/duration/token stats |
| `GET /api/tokens?period=all\|date\|from&to` | token stats, S-05 | per model: input/output/cache, period filters, empty → "Нет данных" |
| `DELETE /api/history` (body `{confirm: true}`) | manual deletion, S-06 | requires explicit confirmation in the request body; atomic deletion in one transaction; source files untouched and may be re-imported on later polling (UC-E2E-008 A1) |
| `GET /api/health` | observability, R-028 | `{status, checks:{postgres,redis}, source_status, ollama_status, last_ingest_at}` (`checks.*` = `"ok"`) |

Errors: stable machine codes (`SOURCE_UNAVAILABLE`, `AI_SEARCH_UNAVAILABLE`,
`INVALID_PERIOD`, `NOT_FOUND`) plus a Russian `message` for direct UI display; HTTP 4xx/5xx
as appropriate.

**Data formats:** ingestion input = opencode JSON files under `OPENCODE_DATA_DIR`
(session + message objects with `id`, `started_at`/`finished_at`, usage fields
`model`, `input`, `output`, `cache`, optional structured solution fields); output =
normalized schema in Postgres. **Ollama contract (localhost only):**
`POST /api/embed` (body `{"model":"<embedding-model>","input":"<text>"}` →
`{"embeddings":[[…1024 floats…]]}`; query/task text → vector), `POST /api/chat`
(ranking + snippet, bounded 15 s). **E2E environment:** `docker compose up` on a clean host with a fixture
`OPENCODE_DATA_DIR` (known dataset from `docs/analysis/e2e-use-cases-ai-task-tracker.md`)
mounted read-only; Ollama running locally for UC-E2E-004/008 and stopped for
UC-E2E-002/007 degradation checks.

**Diagrams (created in R-02, ADR-0002):** component diagram
`docs/architecture/ai-task-tracker-components.puml`, sequence for ingestion and AI search
`docs/architecture/ai-task-tracker-sequences.puml`, rendered SVGs alongside.

## Traceability

Decision sections: 1 — architecture/deployment; 2 — ingestion; 3 — domain model;
4 — tokens; 5 — AI search/extraction; 6 — platform/security; 7 — reliability/retention;
8 — observability; 9 — decomposition; C — contracts.

| Requirement | Decision section |
|---|---|
| R-001 | 2, 7 |
| R-002 | 3 |
| R-003 | 3 |
| R-004 | 3, 5 |
| R-005 | 3, 5 |
| R-006 | 3 |
| R-007 | 1, C |
| R-008 | 3, C |
| R-009 | 3 |
| R-010 | 5 |
| R-011 | 5, C |
| R-012 | 4 |
| R-013 | 4, C |
| R-014 | 4, C |
| R-020 | 1 |
| R-021 | 1 |
| R-022 | 1 |
| R-023 | 5 |
| R-024 | 7 |
| R-025 | 7 |
| R-026 | 2 |
| R-027 | 6, 5 |
| R-028 | 8, C |
| R-029 | 7 |
| R-030 | 6 |

| e2e / UX artifact | Architecture area |
|---|---|
| UC-E2E-001 | 1, 2, C (first launch, list) |
| UC-E2E-002 | 2, 8 (source unavailable banner) |
| UC-E2E-003 | 3, C (list + detail) |
| UC-E2E-004 | 5 (AI search) |
| UC-E2E-005 | 3, C (calendar, duration) |
| UC-E2E-006 | 4, C (token stats) |
| UC-E2E-007 | 2, 5, 8 (degradation) |
| UC-E2E-008 | 7, C (history deletion) |
| S-01 | 1, 2, 8 |
| S-02 | 3 |
| S-03 | 5 |
| S-04 | 3 |
| S-05 | 4 |
| S-06 | 7 |
