# Architecture: ai-task-tracker

| Field | Value |
|---|---|
| Task | T-006, chunks R-01c / R-01d |
| Stage | R-01 |
| Inputs | requirements, questions, e2e-use-cases, e2e-test-data, screens, ui-spec, ADR-0001 |
| Status | proposed |

## Scope and applicability

Local, single-user service that tracks AI coding tasks: ingests opencode session
files, exposes tasks with their steps (R-003), proposed/chosen solutions (R-004,
R-005), final status (R-006) and per-model token usage (R-012, R-013), plus AI
search (R-010), calendar (R-008) and history deletion (R-029). In scope:
R-001..R-013, R-020..R-030; screens S-01..S-06; use cases UC-E2E-001..UC-E2E-008.
Out of scope: authentication, multi-user roles, remote/cloud operation, creating or
editing tasks from the UI.

## Context and constraints

Stack (R-020): Java 21 + Spring Boot 3 backend, PostgreSQL 16 with `pgvector`,
Redis 7. Frontend (R-021): Vue 3 SPA. One-command local stack via `docker compose`
(R-022); ports bound to `127.0.0.1` and only localhost endpoints, incl. Ollama at
`http://localhost:11434` (R-027). Performance: lists < 3 s, AI search < 15 s
(R-023); scale to ~50k events with pagination (R-024). Ingestion is a 60 s poll of
read-only opencode JSON files (R-001, R-026); re-sync is idempotent (R-025).
Domain: 1 task = 1 opencode session (R-002); duration = `finished_at - started_at`
(R-009); token totals come only from source `usage`, never estimated (R-012,
R-013). Error messages and UI copy are Russian (R-030); liveness/readiness and
degradation are observable (R-028).

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
| web (nginx + Vue 3 SPA) | S-01..S-06 UI, REST client, polling |
| api (Spring REST) | REST endpoints API-001..API-004, RU error envelope |
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

## Scenarios

| SCN | Name | UC | APIs |
|---|---|---|---|
| SCN-001 | stack up + initial empty | UC-E2E-001 | API-001, API-002, API-003, API-004 |
| SCN-002 | source unavailable state | UC-E2E-002 | API-004 |
| SCN-003 | list + detail | UC-E2E-003 | API-001 |
| SCN-004 | AI search | UC-E2E-004 | API-003 |
| SCN-005 | calendar views | UC-E2E-005 | API-002 |
| SCN-006 | token statistics | UC-E2E-006 | API-002 |
| SCN-007 | restart + idempotent re-sync | UC-E2E-007 | API-001, API-004 |
| SCN-008 | delete history | UC-E2E-008 | API-001 |

## Data model

PostgreSQL 16: `tasks` (`task_id` PK, `session_id` UNIQUE, `name`, `started_at`,
`finished_at`, `status`, `embedding vector(1024)`); `steps` (`message_id`, `at`,
`text`, UNIQUE(`task_id`,`message_id`)); `solutions` (`kind` proposed/chosen,
`text`, `source_message_id`); `token_usage` (`model`, input/output/cache tokens,
`scope`); `ingest_state` (watermark + `parse_errors`). Invariants: `finished_at`
NULL ⇒ `Не завершена`; `Завершена` ⇒ `finished_at` NOT NULL; no token double
counting. Full DDL, mapping and invariants: `contracts-ai-task-tracker.md` §2–3.

## Class model

Backend Spring beans: `TaskService`, `SolutionService`, `CalendarService`,
`TokenStatsService`, `SearchService`, `IngestionPoller`, `OllamaClient`,
`HealthService` and JPA repositories for the five tables; DTOs `TaskListResponse`,
`TaskDetailResponse`, `SearchRequest`/`SearchResult`, `CalendarResponse`,
`TokenStatsResponse`, `ErrorEnvelope`. Frontend: Pinia store, typed API client and
S-01..S-06 screen components. Class diagram is produced in R-02 (T-007).

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
R-012/R-013 (tokens), R-028 (health), R-029 (deletion), R-023 (timings), R-024
(pagination).

## Diagram index

| Diagram | Stage | File |
|---|---|---|
| component | R-02 (T-007) | component-ai-task-tracker.puml |
| sequence SCN-001 | R-02 (T-007) | sequence-scn-001-ai-task-tracker.puml |
| sequence SCN-002 | R-02 (T-007) | sequence-scn-002-ai-task-tracker.puml |
| sequence SCN-003 | R-02 (T-007) | sequence-scn-003-ai-task-tracker.puml |
| sequence SCN-004 | R-02 (T-007) | sequence-scn-004-ai-task-tracker.puml |
| sequence SCN-005 | R-02 (T-007) | sequence-scn-005-ai-task-tracker.puml |
| sequence SCN-006 | R-02 (T-007) | sequence-scn-006-ai-task-tracker.puml |
| sequence SCN-007 | R-02 (T-007) | sequence-scn-007-ai-task-tracker.puml |
| sequence SCN-008 | R-02 (T-007) | sequence-scn-008-ai-task-tracker.puml |
| data model | R-02 (T-007) | data-model-ai-task-tracker.puml |
| class | R-02 (T-007) | class-ai-task-tracker.puml |
| OpenAPI API-001..API-004 | R-01 (this stage) | openapi-api-001-ai-task-tracker.yaml, openapi-api-002-ai-task-tracker.yaml, openapi-api-003-ai-task-tracker.yaml, openapi-api-004-ai-task-tracker.yaml |

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
SCN-008↔UC-E2E-008.
