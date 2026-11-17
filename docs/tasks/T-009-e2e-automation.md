# T-009: I-02 E2E automation and runner

| Field | Value |
|---|---|
| id | T-009 |
| title | I-02 E2E automation and runner |
| workflow | feature |
| stage | I-02 |
| role | coder |
| status | done |
| hold | false |
| depends_on | T-008, T-003, T-007 |
| inputs | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/architecture/contracts-ai-task-tracker.md` |
| artifacts | `tests/e2e/**`, `scripts/e2e.ps1`, `scripts/validate-e2e-data.py` |
| e2e | required - runs the YAML dataset against the local stack |
| ux | not applicable (automation only) |
| chunks | ledger `docs/chunks/T-009.json` (single source of truth for per-chunk state); runner skeleton, dataset validator, three UC batches, fixtures and cleanup, final full run |
| budget | `max_write_kb` / `max_session_kb` / `context_stop_pct` / `max_turns` / `max_input_tokens` / `max_wall_min`; effective values in the ledger `budget` object |
| base_commit | - |
| usage | in=4539299 out=1137324 cr=110482991 ratio=24.3x min=2657 |
| issues | - |
| commit | 88acc15 |

## Acceptance

- [ ] Tests are generated from the YAML dataset and run against an isolated local/staging stack only.
- [ ] The runner emits machine-readable results per `UC-E2E-NNN` and cleans up test data.
- [ ] No chunk reads more than its own UC batch and the runner API.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | runner and validator | `scripts/e2e.ps1`, `scripts/validate-e2e-data.py` | 9P/0F/0M | approved | docs/checkpoints/T-009-CP-1.md |
| CP-2 | UC batches and full run | `tests/e2e/**` | 10P/0F/0M | approved | docs/checkpoints/T-009-CP-2.md |
| CP-F | final acceptance | all task artifacts | 10P/0F/0M | approved | docs/checkpoints/T-009-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- 2026-11-17: blocker resolved. BUG-002 verified (T-013/T-014); BUG-003 (Flyway/PG16, ports, wiring, JSON shape, token periods) fixed by T-015 and verified by T-016.

## Log

- 2026-11-14: card created during the 3.0.0 rollout (T-046) so the board row has a card and a chunk ledger before the task starts.
- 2026-11-16: T-009 started (I-02); board set in_progress; first chunk C1 dispatched.
- 2026-11-16: C3 done (UC-E2E-001..003, I-02). Evidence: pytest --collect-only tests/e2e/uc_e2e_001_003 => 27 tests, exit 0; pytest --collect-only tests/e2e => exit 0 (full suite green, stack down); forbidden tokens `production.example`/`prod-api` absent; `cleanup` present in UC-E2E-001..003 files. C1/C2 conftest fixtures reused, not modified. Live assertions inside `stack`-marked tests; collection-safe. Deferred: no live run (base commit does not build, mvn deferred to C7).
- 2026-11-16: Variant 3 re-plan (user-approved). C7 held pending. C1/C3/C4 re-opened: (C1) inert per-UC emitter replaced by pytest_runtest_logreport aggregation, sessionfinish collect-only guard, runner no-abort, README sync; (C3) package conftest stack fixture with seed+wait, UC-002/003 missing fixture, contract-faithful health status, skip-on-stack-down; (C4) skip-on-stack-down + Ollama-down skip. BUG-002 (Dockerfile source-path defect) triaged P1; bugfix tasks T-013 (coder) and T-014 (QA) created. C7 resumes after the C1/C3/C4 fixes and BUG-002 verification, then runs the full suite live and records per-UC results.
- 2026-11-17: C7 full run GREEN on the local stack: 49 passed, 4 skipped (Ollama-dependent AI-search), 0 failed, 0 errors; per-UC results in tests/e2e/results/ (summary: 8/8 pass). Chunks 7/7 done.
- 2026-11-17: CP-1 approved (I-02); checks 9 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=4531810 out=1135533 cr=106581809 ratio=23.5x min=2654; reviewer auto (P2)
- 2dc17b4: CP-1 snapshot
- 2026-11-17: CP-2 approved (I-02); checks 10 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=4532332 out=1135651 cr=107137159 ratio=23.6x min=2654; reviewer auto (P2)
- 3378dd8: CP-2 snapshot
- 2026-11-17: CP-F awaiting_review (I-02); checks 10 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=4533741 out=1135839 cr=108249337 ratio=23.9x min=2654; reviewer user
- ac0757f: CP-F snapshot
- 2026-11-17: CP-F approved (I-02); checks 10 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=4539299 out=1137324 cr=110482991 ratio=24.3x min=2657; reviewer auto (explicit -AutoApprove)
- 88acc15: CP-F snapshot
