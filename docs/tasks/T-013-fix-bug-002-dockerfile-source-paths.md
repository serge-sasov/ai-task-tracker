# T-013: Fix BUG-002 - Dockerfile source-path mismatch

| Field | Value |
|---|---|
| id | T-013 |
| title | Fix BUG-002 - Dockerfile source-path mismatch (B-01/B-02) |
| workflow | bugfix |
| stage | B-01 / B-02 |
| role | coder |
| status | pending |
| hold | false (true forces human review at every checkpoint) |
| depends_on | T-009 (held at C7) |
| bug | BUG-002 |
| severity | P1 |
| profile | - |
| inputs | `docs/bugs/BUG-002-dockerfile-source-paths.md`, `Dockerfile`, `backend/pom.xml`, `docker-compose.yml` |
| artifacts | `Dockerfile`, `tests/regression/**` |
| e2e | not applicable (build infrastructure; verified by compose build + `/api/health`; the full suite is T-009 C7) |
| ux | not applicable (build infrastructure only) |
| chunks | ledger `docs/chunks/T-013.json` (single source of truth for per-chunk state, `owns`, and usage) |
| budget | `max_write_kb` / `max_session_kb` / `context_stop_pct` / `max_turns` / `max_input_tokens` / `max_wall_min` (defaults: code 12/40/65); the ledger `budget` object holds the effective values |
| base_commit | 90c22f7 |
| usage | - |
| issues | - |
| commit | pending |

## Bug summary

- Observed: `mvn package` exits 1 on the local stack; zero e2e results captured; the backend jar is not produced.
- Expected: `mvn package` succeeds; the built jar is copied to the runtime stage; the local stack comes up.
- Reproduction: `docker compose -f docker-compose.yml build backend`; the build stage fails because the pom `sourceDirectory`/`resources` resolve outside the copied tree and spring-boot repackage finds no main class.
- Root cause: (filled by coder at CP-1) the `Dockerfile` build stage copies `backend/pom.xml` to the image root and runs `mvn package` with CWD=image root, so `backend/pom.xml`'s `sourceDirectory=${project.basedir}/../src/main/java` resolves to `/src/main/java` (outside the image) instead of `/app/src/main/java`.

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
| CP-1 | reproduction/root cause | `tests/regression/**` + card log | - | pending | `docs/checkpoints/T-013-CP-1.md` |
| CP-2 | fix/regression | `Dockerfile` + `tests/regression/**` | - | pending | `docs/checkpoints/T-013-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-013-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- 2026-11-16: card created during BUG-002 triage (B-00); reproduction and root-cause analysis are the CP-1 deliverables; base commit 90c22f7.
