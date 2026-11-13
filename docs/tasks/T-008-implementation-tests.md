# T-008: I-01 Implementation and unit/integration tests

| Field | Value |
|---|---|
| id | T-008 |
| title | I-01 Implementation and unit/integration tests |
| workflow | feature |
| stage | I-01 |
| role | coder |
| status | in_progress |
| hold | false (true forces human review at every checkpoint) |
| depends_on | T-007, T-005 |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/ux/user-flows-ai-task-tracker.md`, `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md`, `docs/architecture/contracts-ai-task-tracker.md`, `docs/architecture/openapi-api-001-ai-task-tracker.yaml`, `docs/architecture/openapi-api-002-ai-task-tracker.yaml`, `docs/architecture/openapi-api-003-ai-task-tracker.yaml`, `docs/architecture/openapi-api-004-ai-task-tracker.yaml`, `docs/architecture/class-ai-task-tracker.puml`, `docs/architecture/data-model-ai-task-tracker.puml` |
| artifacts | `src/`, `tests/`, `Dockerfile`, `docker-compose*.yml`, `Makefile` |
| e2e | required — I-01 delivers the runnable app the I-02 e2e automation will exercise (UC-E2E-001..UC-E2E-008); the YAML dataset `docs/analysis/e2e-test-data-ai-task-tracker.yaml` is the data contract |
| ux | required — the Vue 3 SPA implements screens S-01..S-06 per `docs/ux/ui-spec-ai-task-tracker.md` (RU copy, states) |
| chunks | 6 sequential, one module/file group per chunk (non-overlapping): (1) project skeleton — build config (Java 21 + Spring Boot 3), application entry, configuration, `Dockerfile`, `docker-compose*.yml` (backend, postgres16+pgvector, redis7, frontend nginx; ports bound to 127.0.0.1), `Makefile` (up/down/test/logs), `.env.example`; (2) `store` + `domain` modules — JPA entities for `tasks`, `steps`, `solutions`, `token_usage`, `ingest_state` with `vector(1024)`, repositories, status derivation / duration invariants (contracts §2) + unit tests; (3) `ingestion` module — 60 s poller, defensive JSON parser, idempotent upsert, Redis lock/watermark (contracts §3) + unit tests; (4) `api` module — controllers for API-001 (GET /api/tasks, GET /api/tasks/{id}, DELETE /api/history) + API-004 (GET /api/health), RU error envelope (contracts §5) + slice/integration tests; (5) `api` + `search`/`stats` modules — API-002 (GET /api/calendar, GET /api/tokens) + API-003 (POST /api/search) with Ollama client (localhost, bounded 15 s, 503 AI_SEARCH_UNAVAILABLE) + tests; (6) `web` module — Vue 3 SPA screens S-01..S-06 (Pinia store, typed API client, RU copy) + build wiring in compose; final build + full test run green via Makefile |
| budget | code (coder): max_write_kb 12 / max_session_kb 40 / context_stop_pct 65 (per `harness/process.md` §11.1) |
| base_commit | 2f8fc39 |
| usage | - (filled by stage-done) |
| issues | - |
| commit | - |

## Acceptance

- [ ] Backend builds and all unit/integration tests pass locally (Makefile `test` target) — Java 21 + Spring Boot 3, PostgreSQL 16 + pgvector, Redis 7 per ADR-0001/contracts.
- [ ] REST endpoints API-001..API-004 implemented per the OpenAPI specs and contracts §4–5 (RU error envelope, pagination, timings R-023/R-024).
- [ ] Ingestion: 60 s poll, idempotent re-sync (R-025), Redis lock/watermark, defensive parsing (ADR consequences).
- [ ] Vue 3 SPA screens S-01..S-06 implemented per UI spec, RU copy (R-030).
- [ ] One-command local stack: `docker compose up` brings up backend+postgres+redis+frontend with ports on 127.0.0.1 (R-022, R-027); `Makefile` provides up/down/test/logs.
- [ ] No TODO/FIXME placeholders, no secrets in code/config (checks no-todo, no-secrets).

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | I-01 implementation and tests | `src/`, `tests/`, `Dockerfile`, `docker-compose*.yml`, `Makefile` | - | pending | docs/checkpoints/T-008-CP-1.md |
| CP-F | final acceptance | all task artifacts | - | pending | docs/checkpoints/T-008-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- 2026-11-13: card created for I-01; T-008 started (in_progress); base_commit 2f8fc39 (HEAD at start).
