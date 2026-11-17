# T-016: Verify BUG-003 - local stack functional health (B-03)

| Field | Value |
|---|---|
| id | T-016 |
| title | Verify BUG-003 - local stack functional health (B-03) |
| workflow | bugfix |
| stage | B-03 |
| role | qa |
| status | done |
| hold | false |
| depends_on | T-015 |
| bug | BUG-003 |
| severity | P1 |
| profile | - |
| inputs | `docs/bugs/BUG-003-local-stack-not-healthy.md`, `docs/tasks/T-015-fix-bug-003-local-stack.md`, `docs/checkpoints/T-015-CP-1.md`, `docs/checkpoints/T-015-CP-2.md` |
| artifacts | `docs/qa/review-T-016.md` |
| e2e | required - the full T-009 suite is the acceptance evidence |
| ux | not applicable (backend/infrastructure) |
| chunks | - (no ledger; QA verification is a single deliverable) |
| budget | - |
| base_commit | 90c22f7 |
| usage | n/a |
| issues | - |
| commit | 3bc860c |

## Bug summary

- Observed: the local stack could not become functionally healthy (Flyway PG16,
  port conflicts, ingest constraint, `/api/tasks` 500, missing beans, Boot-3 Redis
  keys, camelCase JSON, `/api/tokens` 500).
- Expected: healthy stack, 5 ingested tasks, contract-shaped JSON, green e2e.
- Reproduction: see BUG-003; static regression on the base revision.
- Root cause: see BUG-003 "Root cause".

## Acceptance

- [x] Independent fail-before evidence: `test_stack_health_prereqs.py` fails on
  the base revision.
- [x] Independent pass-after evidence: regression passes; stack healthy; full e2e
  green (Ollama skips allowed).
- [x] Regression test passes.
- [x] No collateral changes.
- [x] Verdict recorded in `docs/qa/review-T-016.md`.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | verification plan | verification plan (in card log) | 4P/0F/0M | approved | docs/checkpoints/T-016-CP-1.md |
| CP-F | final acceptance | `docs/qa/review-T-016.md` | 5P/0F/0M | approved | docs/checkpoints/T-016-CP-F.md |

## Blockers

- none

## Log

- 2026-11-17: verification plan: (1) fail-before static regression on base
  `90c22f7`; (2) pass-after regression + `docker compose build backend` + health;
  (3) ingest 5 tasks; (4) full T-009 e2e; (5) collateral diff. Verdict in
  `docs/qa/review-T-016.md`.
- 2026-11-17: CP-1 approved (B-03); checks 4 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (P2)
- ccbbc2a: CP-1 snapshot
- 2026-11-17: CP-F awaiting_review (B-03); checks 5 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer user
- 090a6ce: CP-F snapshot
- 2026-11-17: CP-F approved (B-03); checks 5 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (explicit -AutoApprove)
- 3bc860c: CP-F snapshot
