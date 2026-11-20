# Architecture: ai-task-tracker

| Field | Value |
|---|---|
| Task | T-006, T-022, T-034 (R-01); T-035 (R-02) |
| Stage | R-01 + R-02 (T-035 appended, S-04 interfaces/data model) |
| Inputs | requirements, questions, e2e-use-cases, e2e-test-data, screens, ui-spec, ADR-0001 |
| Status | proposed |

## Scope and applicability

Local, single-user service that tracks AI coding tasks: ingests opencode session
files, exposes tasks with their steps (R-003), proposed/chosen solutions (R-004,
R-005), final status (R-006) and per-model token usage (R-012, R-013), the
primary model and full model list in S-01 (R-014), the shared model cell in the
S-04 day view (R-015), plus AI search (R-010),
calendar (R-008) and history deletion (R-029). In scope:
R-001..R-014, R-020..R-030; screens S-01..S-06; use cases UC-E2E-001..UC-E2E-008.
Out of scope: authentication, multi-user roles, remote/cloud operation, creating or
editing tasks from the UI. R-015 (S-04 model column) is UI-only: `GET /api/tasks`
(API-001) already returns the ordered `models` array (R-014), so no backend, API or
schema change is required and the S-01 behavior (R-014) is preserved by reusing the
same cell (ADR-0001 §10).

## Context and constraints

Stack (R-020): Java 21 + Spring Boot 3 backend, PostgreSQL 16 with `pgvector`,
Redis 7. Frontend (R-021): Vue 3 SPA. One-command local stack via `docker compose`
(R-022); ports bound to `127.0.0.1` and only localhost endpoints, incl. Ollama at
`http://localhost:11434` (R-027). Performance: lists < 3 s, AI search < 15 s
(R-023); scale to ~50k events with pagination (R-024). Ingestion is a 60 s poll of
read-only opencode JSON files (R-001, R-026); re-sync is idempotent (R-025).
Domain: 1 task = 1 opencode session (R-002); duration = `finished_at - started_at`
(R-009); token totals come only from source `usage`, never estimated (R-012,
R-013). The S-01 model list is derived from the same `token_usage` rows as
`tokens_total`; it is ordered by total tokens desc with a model-name tie-break and
needs no extra query or migration (R-014). Error messages and UI copy are Russian
(R-030); liveness/readiness and degradation are observable (R-028).

## Decisions

Fixed by `adr-ai-task-tracker.md` (ADR-0001 / ADR-0002): a **modular monolith**
(one backend JVM, no microservices); PostgreSQL 16 + `pgvector` as system of record;
local Ollama for task-digest embeddings and search re-ranking; Redis 7 for the
ingestion lock/watermark; poll-based ingestion; Vue 3 SPA behind nginx proxying
`/api/*`. This document fixes the component split, the scenario catalog (SCN), the
API catalog (API) and the OpenAPI links; it does not re-litigate the ADR.

## Components

| Component | Responsibility |
|---|---|
| web (nginx + Vue 3 SPA) | S-01..S-06 UI, REST client, polling; S-01 model cell (primary + `+N`, «Нет данных») |
| api (Spring REST) | REST endpoints API-001..API-004, RU error envelope; task-list `models` aggregation (R-014) |
| ingestion (poller) | parse opencode JSON, idempotent upsert, re-embed |
| search (AI) | pgvector recall + Ollama re-rank + RU snippets |
| store (JPA / PG16) | tasks, steps, solutions, token_usage, ingest_state |
| cache (Redis 7) | ingestion lock + watermark |
| llm (Ollama client) | localhost:11434, bounded 15 s |
| obs (health) | `/api/health`, `source_status`, structured logs |

## Services

No microservices. The components above are modules of one backend process deployed
in a single `backend` container (ADR-0001); there is no inter-service network
contract beyond the SPA → backend REST boundary. The SVC catalog is therefore
empty and no swim-lane diagrams are required.

## Responsibility zones

### Components

| Component | Owns | Does not own |
|---|---|---|
| web (nginx + Vue 3 SPA) | S-01..S-06 UI, REST client, polling, shared model cell rendering for S-01 (R-014) and S-04 day view (R-015): primary + `+N`, tooltip/aria, «Нет данных» | task aggregation, ingestion, storage, search ranking |
| api (Spring REST) | API-001..API-004, RU error envelope, task-list `models` aggregation and ordering (R-014) | opencode file parsing, vector recall, database engine |
| ingestion (poller) | opencode JSON parsing, idempotent upsert, re-embed scheduling | REST response shaping, S-01 rendering, token aggregation |
| search (AI) | pgvector recall, Ollama re-rank, RU snippets | task list and detail endpoints, token accounting |
| store (JPA / PG16) | tasks, steps, solutions, token_usage, ingest_state persistence | HTTP contracts, UI behavior, model ordering policy |
| cache (Redis 7) | ingestion lock and watermark | task data storage, REST responses, UI state |
| llm (Ollama client) | local embedding and chat calls with a 15 s bound | persistence, list aggregation, UI rendering |
| obs (health) | `/api/health`, `source_status`, structured logs | task business logic, model aggregation, UI |

### Modules

| Module | Parent | Owns | Does not own |
|---|---|---|---|
| MOD-001 | api (Spring REST) | task query service, `TaskListItem.models` ordering, REST DTO mapping | opencode parsing, vector recall, database engine |
| MOD-002 | web (nginx + Vue 3 SPA) | S-01 task table and S-04 day view layout, shared model cell (S-01 R-014 + S-04 R-015): primary + `+N`, tooltip/aria, no-usage state | API aggregation, ingestion, storage |
| MOD-003 | ingestion (poller) | opencode session parsing, usage-row normalization, idempotent upsert | REST DTOs, UI state, model ordering |
| MOD-004 | store (JPA / PG16) | task, step, solution, token_usage repositories | HTTP contracts, UI rendering, ingestion scheduling |
| MOD-005 | search (AI) | vector recall, Ollama re-rank, snippet generation | task list aggregation, token statistics, persistence |

### Classes

| Class | Parent | Owns | Does not own |
|---|---|---|---|
| CLS-001 | MOD-001 | TaskService task-list query, total-token ordering and `models` projection (R-014) | ingestion parsing, UI rendering, persistence engine |
| CLS-002 | MOD-001 | TaskListItem DTO including the ordered `models` array | aggregation logic, database queries, UI state |
| CLS-003 | MOD-002 | S-01 task table: row layout, status, duration, navigation to S-02 | model-cell rendering (CLS-005), API aggregation, ingestion, storage |
| CLS-004 | MOD-003 | TokenUsage parsing and normalization per model | DTO shape, model ordering policy, UI rendering |
| CLS-005 | MOD-002 | shared model cell (S-01 R-014 + S-04 R-015): `models` projection — primary + `+N`, full ordered list in `title`/`aria-label`, «Нет данных» empty state | S-01/S-04 row and table layout, task loading, API aggregation, ingestion |

## Scenarios

| SCN | Name | UC | APIs |
|---|---|---|---|
| SCN-001 | stack up + initial empty | UC-E2E-001 | API-001, API-002, API-003, API-004 |
| SCN-002 | source unavailable state | UC-E2E-002 | API-004 |
| SCN-003 | list + detail (primary model + `+N`, full list, «Нет данных») | UC-E2E-003 | API-001 |
| SCN-004 | AI search | UC-E2E-004 | API-003 |
| SCN-005 | calendar views | UC-E2E-005 | API-002 |
| SCN-006 | token statistics | UC-E2E-006 | API-002 |
| SCN-007 | restart + idempotent re-sync | UC-E2E-007 | API-001, API-004 |
| SCN-008 | delete history | UC-E2E-008 | API-001 |
| SCN-009 | S-04 day-view model column (shared cell, primary + `+N`, «Нет данных») | UC-E2E-005 | API-001 |

## Data model

PostgreSQL 16: `tasks` (`task_id` PK, `session_id` UNIQUE, `name`, `started_at`,
`finished_at`, `status`, `embedding vector(1024)`); `steps` (`message_id`, `at`,
`text`, UNIQUE(`task_id`,`message_id`)); `solutions` (`kind` proposed/chosen,
`text`, `source_message_id`); `token_usage` (`model`, input/output/cache tokens,
`scope`); `ingest_state` (watermark + `parse_errors`). Invariants: `finished_at`
NULL ⇒ `Не завершена`; `Завершена` ⇒ `finished_at` NOT NULL; no token double
counting. The task-list `models` array is derived from `token_usage` (total tokens
desc, model name asc); it adds no column or migration (R-014). Full DDL, mapping
and invariants: `contracts-ai-task-tracker.md` §2–3.

## Class model

Backend Spring beans: `TaskService` (task-list query and `models` ordering,
R-014), `SolutionService`, `CalendarService`, `TokenStatsService`, `SearchService`,
`IngestionPoller`, `OllamaClient`, `HealthService` and JPA repositories for the
five tables; DTOs `TaskListResponse`, `TaskListItem.models`,
`TaskDetailResponse`, `SearchRequest`/`SearchResult`, `CalendarResponse`,
`TokenStatsResponse`, `ErrorEnvelope`. Frontend: Pinia store, typed API client and
S-01..S-06 screen components; the shared model cell (CLS-005 `ModelCell`) renders
the model column in S-01 (R-014) and S-04 (R-015). Class diagram is
produced in R-02 (T-023).

## REST API

Base URL `http://localhost:8080` (nginx → `backend:8080`), JSON UTF-8, pagination
`page`/`size`, Russian error envelope (contracts §5).

| API | Endpoints | OpenAPI spec |
|---|---|---|
| API-001 | GET /api/tasks, GET /api/tasks/{task_id}, DELETE /api/history | openapi-api-001-ai-task-tracker.yaml |
| API-002 | GET /api/calendar, GET /api/tokens | openapi-api-002-ai-task-tracker.yaml |
| API-003 | POST /api/search | openapi-api-003-ai-task-tracker.yaml |
| API-004 | GET /api/health | openapi-api-004-ai-task-tracker.yaml |

Covers R-007 (list/detail), R-008 (calendar), R-010/R-011 (search + snippets),
R-012/R-013/R-014 (tokens and the S-01 model list), R-015 (S-04 model column
consumes the same API-001 `models` field, no new endpoint), R-028 (health),
R-029 (deletion), R-023 (timings), R-024 (pagination).

## Diagram index

| Diagram | Stage | File |
|---|---|---|
| component | R-01 (T-022, T-034) | components-ai-task-tracker.puml |
| component (legacy alias, superseded by components-ai-task-tracker.puml) | R-02 (T-007) | component-ai-task-tracker.puml |
| sequence SCN-001 | R-02 (T-007) | sequence-scn-001-ai-task-tracker.puml |
| sequence SCN-002 | R-02 (T-007) | sequence-scn-002-ai-task-tracker.puml |
| sequence SCN-003 | R-01 (T-022) | sequence-scn-003-ai-task-tracker.puml |
| sequence SCN-004 | R-02 (T-007) | sequence-scn-004-ai-task-tracker.puml |
| sequence SCN-005 | R-02 (T-007) | sequence-scn-005-ai-task-tracker.puml |
| sequence SCN-006 | R-02 (T-007) | sequence-scn-006-ai-task-tracker.puml |
| sequence SCN-007 | R-02 (T-007) | sequence-scn-007-ai-task-tracker.puml |
| sequence SCN-008 | R-02 (T-007) | sequence-scn-008-ai-task-tracker.puml |
| sequence SCN-009 | R-01 (T-034) | sequence-scn-009-ai-task-tracker.puml |
| data model | R-02 (T-023, T-035) | data-model-ai-task-tracker.puml |
| class | R-02 (T-023, T-035) | class-ai-task-tracker.puml |
| OpenAPI API-001..API-004 | R-01/R-02 (T-022/T-023) | openapi-api-001-ai-task-tracker.yaml, openapi-api-002-ai-task-tracker.yaml, openapi-api-003-ai-task-tracker.yaml, openapi-api-004-ai-task-tracker.yaml |

## Interfaces and data model (R-02, T-035 — S-04 model cell)

- **No new REST API** for R-015: `GET /api/tasks` (API-001) already exposes the
  ordered `models` array (R-014) and the S-04 day view consumes the same field.
  Re-validated at R-02 (T-035): all four OpenAPI specs parse as valid YAML,
  OpenAPI 3.0.3 (`x-harness-api-id` API-001..API-004); `TaskListItem.models` is
  `array<string>` in `openapi-api-001-ai-task-tracker.yaml`. No spec file was
  created or edited.
- **Data model:** no schema change for R-014/R-015 — `models` is derived from the
  existing `token_usage` table (total tokens desc, model name asc); the derivation
  is noted in `data-model-ai-task-tracker.puml`.
- **Class model:** `ModelCell` (CLS-005, MOD-002) is registered in
  `class-ai-task-tracker.puml` (frontend package) as the single owner of the
  `models` → display mapping (primary + `+N`, full list in `title`/`aria-label`,
  «Нет данных»); `Screens` (S-01, S-04) and `TaskListItem.models` reference it.
  See §Responsibility zones, Classes (zones stay exclusive: CLS-003 owns S-01 row
  layout only, the S-04 day-view layout owns the table only, CLS-005 owns the cell
  rules).
- **Swimlanes:** unchanged — the SVC catalog is empty (see §Services: "No
  microservices"), so no swimlane diagram exists or is required for this change.
- **Module handoff for the coder:** `contracts-ai-task-tracker.md` §9 — S-04
  module boundary (shared `ModelCell.vue` CLS-005; `CalendarDayView.vue` reuses
  it; API contract unchanged) and the file/test list (S-01 R-014 stays green,
  UC-E2E-005 `test_day_view_model_column`).

## Traceability

| Req | Covered by |
|---|---|
| R-001 | ingestion component, SCN-007 |
| R-002 | tasks.session_id (Data model) |
| R-003 | steps table, API-001 detail |
| R-004 | solutions (proposed), API-001 |
| R-005 | solutions (chosen), API-001 |
| R-006 | status enum (contracts §1) |
| R-007 | API-001, S-01/S-02 |
| R-008 | API-002 calendar, S-04 |
| R-009 | duration invariant, `duration_s` |
| R-010 | API-003, search component |
| R-011 | API-003 top-K + snippet |
| R-012 | token_usage, API-002 |
| R-013 | API-002, S-05 |
| R-014 | API-001 `models`, SCN-003, S-01 |
| R-015 | shared ModelCell CLS-005 (MOD-002), SCN-009, S-01/S-04 views, API-001 `models` |
| R-020 | backend stack (Context) |
| R-021 | Vue 3 SPA (Context) |
| R-022 | docker compose (ADR-0001) |
| R-023 | REST API timings |
| R-024 | pagination + scale profile |
| R-025 | idempotent ingestion (contracts §3) |
| R-026 | 60 s poll, `last_ingest_at` |
| R-027 | locality + read-only source |
| R-028 | API-004, error envelope |
| R-029 | DELETE /api/history |
| R-030 | RU messages (Context) |

Use-case trace: SCN-001↔UC-E2E-001, SCN-002↔UC-E2E-002, SCN-003↔UC-E2E-003,
SCN-004↔UC-E2E-004, SCN-005↔UC-E2E-005, SCN-006↔UC-E2E-006, SCN-007↔UC-E2E-007,
SCN-008↔UC-E2E-008, SCN-009↔UC-E2E-005.
