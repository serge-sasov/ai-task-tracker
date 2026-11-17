# T-013: Fix BUG-002 - Dockerfile source-path mismatch

| Field | Value |
|---|---|
| id | T-013 |
| title | Fix BUG-002 - Dockerfile source-path mismatch (B-01/B-02) |
| workflow | bugfix |
| stage | B-01 / B-02 |
| role | coder |
| status | in_progress |
| hold | false (true forces human review at every checkpoint) |
| depends_on | T-009 (held at C7) |
| bug | BUG-002 |
| severity | P1 |
| profile | - |
| inputs | `docs/bugs/BUG-002-dockerfile-source-paths.md`, `Dockerfile`, `backend/pom.xml`, `docker-compose.yml` |
| artifacts | `Dockerfile`, `tests/regression/**`, `docs/tasks/T-013-*.md` |
| e2e | not applicable (build infrastructure; verified by compose build + `/api/health`; the full suite is T-009 C7) |
| ux | not applicable (build infrastructure only) |
| chunks | ledger `docs/chunks/T-013.json` (single source of truth for per-chunk state, `owns`, and usage) |
| budget | `max_write_kb` / `max_session_kb` / `context_stop_pct` / `max_turns` / `max_input_tokens` / `max_wall_min` (defaults: code 12/40/65); the ledger `budget` object holds the effective values |
| base_commit | 90c22f7 |
| usage | n/a |
| issues | - |
| commit | pending |

## Bug summary

- Observed: `mvn package` exits 1 on the local stack; zero e2e results captured; the backend jar is not produced.
- Expected: `mvn package` succeeds; the built jar is copied to the runtime stage; the local stack comes up.
- Reproduction: `docker compose -f docker-compose.yml build backend`; the build stage fails because the pom `sourceDirectory`/`resources` resolve outside the copied tree and spring-boot repackage finds no main class.
- Root cause: the `Dockerfile` build stage used `WORKDIR /app` and copied sources to `/app/src/main/java`, while `backend/pom.xml` resolves `${project.basedir}/../src/main/java` to `/src/main/java` (outside the copied tree), so no classes/resources were compiled and spring-boot repackage found no main class.

## Acceptance

- [ ] The regression test fails on the base revision (90c22f7).
- [ ] The root cause is documented in this card.
- [ ] The minimal fix passes the regression test.
- [ ] Relevant existing tests pass (collection-safe; no live run required at CP-1).
- [ ] No unrelated behavior or files changed.
- [ ] QA verifies fail-before/pass-after independently (T-014).

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | reproduction/root cause | `tests/regression/**` + card log | 6P/0F/0M | approved | docs/checkpoints/T-013-CP-1.md |
| CP-2 | fix/regression | `Dockerfile` + `tests/regression/**` | - | pending | `docs/checkpoints/T-013-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-013-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- 2026-11-16: card created during BUG-002 triage (B-00); reproduction and root-cause analysis are the CP-1 deliverables; base commit 90c22f7.
- 2026-11-17 CP-1: reproduction test tests/regression/test_dockerfile_source_paths.py::test_dockerfile_copies_sources_where_pom_expects FAILS on the base revision (expected /src/main/java from the pom; Dockerfile copied to /app/src/main/java). Root cause confirmed (WORKDIR vs pom basedir mismatch).
- 2026-11-17 CP-2: minimal fix (WORKDIR /app/backend; COPY src/main/java ../src/main/java; COPY src/main/resources ../src/main/resources; runtime jar /app/backend/target/*.jar). Regression: 2 passed. Real build: docker compose -f docker-compose.yml build backend exit 0 (image ai-task-tracker-backend:latest built).
- 2026-11-17: CP-1 approved (B-01); checks 6 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (P2)
