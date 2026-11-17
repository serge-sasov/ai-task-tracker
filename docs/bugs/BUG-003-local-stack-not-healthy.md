# BUG-003: Local stack cannot reach a functional healthy state

| Field | Value |
|---|---|
| id | BUG-003 |
| title | Local stack cannot reach a functional healthy state (Flyway/PG16, port, wiring, JSON shape, token periods) |
| status | verified |
| severity | P1 |
| reported_at | 2026-11-17T15:10:00+03:00 |
| reporter | lead (during T-009 C7 full run) |
| affected | backend (pom, Spring wiring, JPA mapping, JSON serialization), docker-compose host ports, local stack |
| requirements | not applicable (runtime/infrastructure; blocks all UC-E2E-* verification) |
| use_cases | UC-E2E-001..UC-E2E-008 (all) |
| tasks | T-015 (fix), T-016 (QA) |
| duplicate_of | - |
| commit | T-015 CP-2 |
| issues | ISSUE-20261117-131059-C1-conftest-destroyed (harness, unrelated) |

## Observed behavior

After the BUG-002 Dockerfile fix the image built and the application booted, but the
local stack still could not serve the contract surfaces. The following defects
were observed in sequence during the first real T-009 e2e runs:

1. Backend crashed at startup: `FlywayException: Unsupported Database:
   PostgreSQL 16.16` (`flyway-core 10.10.0` without the PostgreSQL module).
2. Host port 8080 was held by an unrelated local service; the compose mapping
   could not bind (and 8081 conflicted too).
3. Ingest failed every cycle: `tasks_status_check` rejected `COMPLETED` because
   `TaskEntity.status` used `@Enumerated(EnumType.STRING)` (enum name) while the
   DB constraint stores the canonical RU values.
4. `GET /api/tasks` returned 500: `could not determine data type of parameter
   $1` (nullable JPQL parameters `(:x is null or ...)`).
5. Spring context failed to start: `IngestService` required a `java.nio.file.Path`
   bean that was not defined; `IngestScheduler` required an `int` interval without
   `@Value`.
6. Redis was unreachable from the container: `application.yml` used the Boot 2.x
   key `spring.redis.*` instead of Boot 3 `spring.data.redis.*`.
7. The API serialized camelCase (`taskId`, `durationS`, `inputTokens`) while the
   contracts/OpenAPI require snake_case (`task_id`, `duration_s`, `input_tokens`).
8. `GET /api/tokens` returned 500 for `period=all` (`LocalDateTime.MIN/MAX` out of
   PostgreSQL range), ignored `date`/`from`/`to` when `period` was absent, and the
   invalid-period message did not match the contract.

## Expected behavior

`docker compose up` yields a healthy isolated local stack; `/api/health` returns
200; the ingest exposes five seeded tasks; the API returns contract-shaped JSON;
the full e2e suite runs green (Ollama-dependent AI-search may skip). Source of
truth: `docs/architecture/contracts-ai-task-tracker.md` §2/§3/§4.1–4.7 and the
API-001..003 OpenAPI specs.

## Reproduction

1. `docker compose -f docker-compose.yml up -d` on the base revision.
2. Seed `data/e2e-opencode` from `fixtures.sessions`.
3. Observe the Flyway crash; after fixing it, the ingest constraint error; then
   the task-list 500 and camelCase responses; `GET /api/tokens` 500.
4. Static regression: `python -m pytest tests/regression/test_stack_health_prereqs.py`
   fails on the base revision (no Flyway PG module, hardcoded backend port).

- Frequency: always.
- Environment: local Docker stack, PostgreSQL 16.16 (pgvector:pg16), Redis 7.
- Evidence: T-009 run logs (`e2e-live-2..7`), T-014 review, T-016 review.

## Impact and triage

- Severity rationale: P1 — the local stack is unusable for the e2e automation and
  any local run; no data loss or security impact.
- Affected users/data/security: local developers/CI; no production impact.
- Workaround: none (the fixes are minimal and required).
- Duplicate check: none. BUG-001 (API-004 spec divergence) and BUG-002 (Dockerfile
  source paths) are related but distinct.
- Escalation to feature-style work: no (defect fixes inside existing components).

## Root cause

- Flyway 10 modularised database support; only `flyway-core` was declared.
- Host ports were hardcoded; another local service holds 8080/8081.
- JPA enum stored the enum name instead of the canonical RU value.
- Nullable JPQL parameters have no SQL type for PostgreSQL.
- Missing Spring beans (`Path`, scheduler interval) — context wiring never booted
  in a test.
- Spring Boot 3 moved Redis properties to `spring.data.redis.*`.
- No global snake_case Jackson naming strategy.
- Token period resolution ignored `date`/`from`/`to` when `period` was blank, used
  `LocalDateTime.MIN/MAX` as bounds, and dropped cache tokens.

## Fix summary

- `backend/pom.xml`: add `org.flywaydb:flyway-database-postgresql`.
- `docker-compose.yml`: backend port `${E2E_BACKEND_PORT:-8080}`, frontend port
  `${E2E_FRONTEND_PORT:-8081}`.
- `src/main/java/.../domain/TaskStatusConverter.java` (new) + `TaskEntity`:
  persist `TaskStatus.value()` (RU) via an `AttributeConverter`.
- `TaskRepository`: replace the nullable JPQL filter with typed derived queries
  (default method).
- `IngestAutoConfiguration`: add the `Path` bean from `TrackerProperties`.
- `IngestScheduler`: bind the interval with `@Value`.
- `application.yml`: `spring.data.redis.*`; `spring.jackson.property-naming-strategy: SNAKE_CASE`.
- `TokenService`: resolve `period`/`date`/`from`+`to` per §4.6; bounded `findAll`;
  sum cache tokens; align the invalid-period message.
- `EmptyQueryException`: message «Введите запрос.» per §4.4.

## Verification

- Static regression `tests/regression/test_stack_health_prereqs.py`: 2 failed on
  the base revision, 2 passed after the fix (with `test_dockerfile_source_paths.py`
  → 4 passed total).
- Real stack: `docker compose build backend` exit 0; `/api/health` 200
  (`postgres=ok`, `redis=ok`); ingest exposes 5 tasks (RU statuses).
- Full e2e (T-009 C7): **49 passed, 4 skipped (Ollama from the container), 0
  failed, 0 errors**; 8/8 UC results `pass` in `tests/e2e/results/`.
- Residual: the in-process `TaskLock` is `InMemoryTaskLock` because
  `IngestAutoConfiguration` reads the legacy key `spring.data-redis.host`; Redis
  itself is healthy. Follow-up (P3): wire `RedisTaskLock` or fix the key.

## Postmortem (P0/P1)

- Cause: the T-008 implementation was verified by unit tests that construct the
  classes directly; the Spring context and the real stack were never booted, so
  wiring/config/contract-shape defects accumulated.
- Detection: first real e2e run (T-009 C7).
- Response: fix the defects under BUG-003 (T-015) and verify with the live suite
  (T-016/T-014).
- Prevention: keep the C7 live run as the acceptance gate; add a context-boot and
  stack-health check to the QA stage.

## Links

- Tasks: T-015 (fix), T-016 (QA); related T-013/T-014 (BUG-002).
- Reviews: `docs/qa/review-T-016.md`, `docs/qa/review-T-014.md`.
- ADRs/contracts: `docs/architecture/contracts-ai-task-tracker.md`,
  `docs/architecture/openapi-api-00{1,2,3}-ai-task-tracker.yaml`.
- Harness issues: `docs/issues/ISSUE-20261117-131059-C1-conftest-destroyed.md`.

## Log

- 2026-11-17: reported during T-009 C7; triaged P1; fix task T-015 and QA T-016
  created.
- 2026-11-17: fixed and verified by the full live e2e run; status `verified`.
