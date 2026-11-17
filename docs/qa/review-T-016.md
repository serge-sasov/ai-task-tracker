# T-016 - QA verification review (BUG-003 / local stack functional health)

| Field | Value |
|---|---|
| task | T-016 (B-03) |
| bug | BUG-003 |
| role | qa (harness-qa, evidence from the C7 live run) |
| base_commit | 90c22f7 |
| fix commits | T-015 CP-1 a65a58c, CP-2 db91a97 |
| reviewed_at | 2026-11-17 |
| scope | Independent fail-before/pass-after, stack health, ingest, full e2e, collateral check |

## Scope

Independently verify the T-015 fixes for **BUG-003** (the local stack could not
become functionally healthy after the BUG-002 Dockerfile fix). Out of scope: the
BUG-002 fix itself (verified by T-014) and UI/browser flows.

## Fail-before evidence

- Static regression on the base revision (`90c22f7` content):
  `python -m pytest tests/regression/test_stack_health_prereqs.py`
  → **2 failed** (no `flyway-database-postgresql`; backend port hardcoded 8080).
- Runtime (T-009 C7 runs before the fix, logs `e2e-live-2..4`):
  - `FlywayException: Unsupported Database: PostgreSQL 16.16`;
  - ingest constraint `tasks_status_check` rejecting `COMPLETED`;
  - `GET /api/tasks` → 500 (`could not determine data type of parameter $1`);
  - camelCase response fields (`taskId`, `durationS`, `inputTokens`);
  - `GET /api/tokens?period=all` → 500; `period=<date>` → 400; `from>to` → 500.

## Pass-after evidence

- Regression: `python -m pytest tests/regression/ -q` → **4 passed**
  (`test_stack_health_prereqs.py` 2 + `test_dockerfile_source_paths.py` 2).
- Build: `docker compose -f docker-compose.yml build backend` → exit 0
  (`ai-task-tracker-backend:latest`).
- Stack: `/api/health` → **200** with `postgres=ok`, `redis=ok`
  (`status=degraded` because Ollama is not reachable from the container; the
  contract requires `ok` only when Ollama is up).
- Ingest: after seeding `data/e2e-opencode`, `GET /api/tasks` → `total=5`
  (TASK-001..005) with canonical RU statuses; `GET /api/tokens?period=all` and
  `?from=2026-11-10&to=2026-11-11` both return 12850 in / 5410 out / 18260 total
  and the same per-model rows.
- Full e2e (T-009 C7 official run):
  `python -m pytest tests/e2e -q --rootdir=.`
  → **49 passed, 4 skipped, 0 failed, 0 errors** in 324s. The 4 skips are the
  Ollama-dependent AI-search cases (explicit reason). `tests/e2e/results/`
  aggregate summary: `total=8, pass=8, fail=0, error=0, skipped=0`.

## Collateral changes

`git diff --stat 90c22f7..HEAD` after T-015 CP-2 contains only: `backend/pom.xml`,
`Dockerfile`, `docker-compose.yml`, `src/main/**` (the listed fix files),
`tests/regression/**`, and docs/chunks/checkpoints/evidence/plan/bugs/tasks.
No unrelated behavior or product modules changed.

## Residual risks

1. `IngestAutoConfiguration.taskLock` reads the legacy key
   `spring.data-redis.host` (typo), so `InMemoryTaskLock` is used even though
   Redis is healthy. Single-instance e2e is unaffected; follow-up (P3) to wire
   `RedisTaskLock`.
2. Ollama is only reachable on the host, not from the compose container by
   default, so the AI-search tests skip with a reason. Optional future work:
   `OLLAMA_BASE_URL=http://host.docker.internal:11434` and a configured model.

## Verdict

**PASS** — fail-before reproduced (static + runtime), pass-after regression and
the full live e2e are green (49 passed, 4 documented Ollama skips, 0 failed).
The BUG-002 fix remains cleared; BUG-003 is verified with the documented
residual risks above.
