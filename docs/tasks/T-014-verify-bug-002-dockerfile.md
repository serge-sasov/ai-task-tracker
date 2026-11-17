# T-014: Verify BUG-002 - Dockerfile fix (B-03)

| Field | Value |
|---|---|
| id | T-014 |
| title | Verify BUG-002 - Dockerfile source-path fix (B-03) |
| workflow | bugfix |
| stage | B-03 |
| role | qa |
| status | awaiting_review |
| hold | false (true forces human review at every checkpoint) |
| depends_on | T-013 |
| bug | BUG-002 |
| severity | P1 |
| profile | - |
| inputs | `docs/bugs/BUG-002-dockerfile-source-paths.md`, `docs/tasks/T-013-fix-bug-002-dockerfile-source-paths.md`, `Dockerfile`, `backend/pom.xml` |
| artifacts | `docs/qa/review-T-014.md` |
| e2e | not applicable (build infrastructure; QA verifies `docker compose build backend` + `/api/health`; the full suite is T-009 C7) |
| ux | not applicable (build infrastructure only) |
| chunks | - (no ledger; QA verification is a single deliverable) |
| budget | - |
| base_commit | 90c22f7 |
| usage | n/a |
| issues | - |
| commit | pending |

## Bug summary

- Observed: `mvn package` exits 1 on the local stack; zero e2e results captured.
- Expected: `mvn package` succeeds; the local stack comes up; e2e results are captured.
- Reproduction: `docker compose -f docker-compose.yml build backend`; observe the backend build stage fail on the base revision.
- Root cause: (see T-013 CP-1) `Dockerfile` build-stage CWD shift breaks `backend/pom.xml`'s `sourceDirectory` relative path.

## Acceptance

- [ ] Independent fail-before evidence: base revision (90c22f7) backend build fails.
- [ ] Independent pass-after evidence: after T-013 fix, `docker compose up --build` succeeds and the backend jar is present.
- [ ] Regression test `tests/regression/test_dockerfile_source_paths.py` passes.
- [ ] No collateral changes (only `Dockerfile` and the regression test touched).
- [ ] Verdict recorded in `docs/qa/review-T-014.md`.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | verification plan | verification plan (in card log) | - | pending | `docs/checkpoints/T-014-CP-1.md` |
| CP-F | final acceptance | `docs/qa/review-T-014.md` | 4P/0F/0M | awaiting_review | docs/checkpoints/T-014-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- 2026-11-16: card created during BUG-002 triage (B-00); verification is the B-03 deliverable; the review file `docs/qa/review-T-014.md` is created at CP-F.
- 2026-11-17: CP-F awaiting_review (B-03); checks 4 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer user
