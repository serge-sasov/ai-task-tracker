# T-015: Fix BUG-003 - local stack functional health

| Field | Value |
|---|---|
| id | T-015 |
| title | Fix BUG-003 - local stack functional health (Flyway/PG16, wiring, JSON shape, tokens) |
| workflow | bugfix |
| stage | B-02 |
| role | coder |
| status | in_progress |
| hold | false |
| depends_on | T-009 (held at C7) |
| bug | BUG-003 |
| severity | P1 |
| profile | - |
| inputs | `docs/bugs/BUG-003-local-stack-not-healthy.md`, `backend/pom.xml`, `src/main/**`, `docker-compose.yml`, `tests/regression/**` |
| artifacts | `backend/pom.xml`, `src/main/**`, `docker-compose.yml`, `tests/regression/**` |
| e2e | required - the full T-009 suite is the acceptance gate |
| ux | not applicable (backend/infrastructure) |
| chunks | ledger `docs/chunks/T-015.json` |
| budget | `max_write_kb` / `max_session_kb` / `context_stop_pct` / `max_turns` / `max_input_tokens` / `max_wall_min` (effective values in the ledger) |
| base_commit | 90c22f7 |
| usage | n/a |
| issues | - |
| commit | db91a97 |

## Bug summary

- Observed: the stack booted but could not become functionally healthy (Flyway PG16
  crash, port conflicts, ingest constraint failure, `/api/tasks` 500, missing beans,
  Boot-3 Redis keys, camelCase JSON, `/api/tokens` 500).
- Expected: healthy stack, 5 ingested tasks, contract-shaped JSON, green e2e.
- Reproduction: static regression `tests/regression/test_stack_health_prereqs.py`
  fails on the base revision; the runtime defects are listed in BUG-003.
- Root cause: see BUG-003 "Root cause".

## Acceptance

- [x] The regression test fails on the base revision (2 of 2 failed).
- [x] The root cause is documented (BUG-003).
- [x] The minimal fix passes the regression test (4 passed with BUG-002 test).
- [x] Relevant existing tests pass (`mvn -f backend/pom.xml -DskipTests package`).
- [x] No unrelated behavior changed.
- [ ] QA verifies fail-before/pass-after independently (T-016).

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | reproduction/root cause | `tests/regression/**` + card log | 8P/0F/0M | approved | docs/checkpoints/T-015-CP-1.md |
| CP-2 | fix/regression | `backend/pom.xml`, `src/main/**`, `docker-compose.yml` | 9P/0F/0M | approved | docs/checkpoints/T-015-CP-2.md |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-015-CP-F.md` |

## Blockers

- none

## Log

- 2026-11-17 CP-1: `python -m pytest tests/regression/test_stack_health_prereqs.py`
  РІвЂ вЂ™ 2 failed on the base revision (Flyway PostgreSQL module missing; backend port
  hardcoded 8080). Root cause documented in BUG-003.
- 2026-11-17 CP-2: fixes applied (pom Flyway module; compose port overrides;
  TaskStatus converter; typed repository queries; Path bean; scheduler `@Value`;
  `spring.data.redis.*`; snake_case Jackson; TokenService periods/bounds/cache;
  empty-query message). Regression РІвЂ вЂ™ 4 passed. Real stack РІвЂ вЂ™ `/api/health` 200,
  ingest 5 tasks. Full e2e (T-009 C7) РІвЂ вЂ™ 49 passed, 4 skipped (Ollama), 0 failed.
- 2026-11-17: CP-1 approved (B-02); checks 8 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (P2)
- a65a58c: CP-1 snapshot
- 2026-11-17: CP-2 approved (B-02); checks 9 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (P2)
- db91a97: CP-2 snapshot
