# T-003: E2E use cases and YAML dataset (A-02)

| Field | Value |
|---|---|
| id | T-003 |
| title | E2E use cases and YAML dataset (A-02) |
| workflow | feature |
| stage | A-02 |
| role | analyst |
| status | done |
| depends_on | T-002 (done) |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/analysis/glossary.md`, `requirements/исходные требования.txt` |
| artifacts | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` |
| e2e | required — this stage produces the e2e contract (use cases + YAML dataset) |
| ux | not applicable — A-02 is an analysis stage with no UI design; UX is owned by U-01/U-02 stages |
| issues | ISSUE-20261112-094931 |
| commit | - |

## Acceptance

- [x] Every functional requirement (R-NNN) from `docs/analysis/requirements-ai-task-tracker.md` is covered by at least one UC-E2E-NNN or has an explicit out_of_scope question/waiver.
- [x] Every use case defines: source requirements, actor, priority, preconditions, trigger, main/alternate/error flows, test data, expected result, postconditions, cleanup, traceability (per `harness/process.md` section 7).
- [x] YAML dataset (machine-readable test contract): schema, environment placeholders (no secrets), use-case entries, steps, data, expected values, cleanup.
- [x] Traceability table R-NNN -> UC-E2E-NNN in the use-cases doc.

## Checkpoints

| ID | Stage | Artifacts | Status | Review file |
|---|---|---|---|---|
| CP-2 | A-02 e2e use cases and dataset | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` | approved | `docs/checkpoints/T-003-CP-2.md` |
| CP-F | final acceptance | all task artifacts | approved | `docs/checkpoints/T-003-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- none

## Log

- 2026-11-11: T-003 started (A-02 e2e use cases and dataset); card created; T-003 -> in_progress; board and diagram updated.
- 2026-11-11 19:12-23:20: analyst session `ses_e349e85afffepgtl3SJOIZMdPi` produced the artifacts after three failed attempts and a manual fourth attempt: 19:34 `SSE read timed out` (~50 min single generation, no artifact); 20:25 `finish=length` at 16 384 output tokens (single `write` of the whole Markdown, no file); 21:02 `SSE read timed out` (~58 min, no artifact); 22:01-23:07 success via skeleton + per-UC edits and YAML in 3 parts; 23:08 context 63 901/65 536 (97.5%), second compaction; 23:20 aborted. Totals: input 136 497, output 70 443, cache read 910 312, 2 compactions. Root cause: no generation budget/decomposition and no role self-sub-sessions; see `ISSUE-20261112-094931` (harness repo `docs/issues/`).
- 2026-11-11: CP-2 reached: analyst artifacts produced (e2e use cases + YAML dataset, verified complete by lead; the role session was cut by client SSE timeouts after the artifacts were written; per process.md §11 resumable, not a failure). T-003 -> awaiting_review; checkpoint file `docs/checkpoints/T-003-CP-2.md` created; board and diagram updated; snapshot committed.
- 2026-11-12: **CP-2 approved by user** (explicit chat decision, 2026-11-12; recorded in `docs/checkpoints/T-003-CP-2.md` — supersedes the value carried in via chore commit ac538f3). Finalized at CP-F: `docs/checkpoints/T-003-CP-F.md` created (A-02 final acceptance; 30 R-NNN covered 24 direct + 6 indirect; 8 UC-E2E with full template fields; YAML `harness-e2e/1` deterministic/secret-free with 8 IDs 1:1; token totals cross-checked 12850/5410/18260; CP-F approved by user in chat). T-003 -> `done` (board/card/activity green #90EE90, SVG re-rendered). Two NON-BLOCKING anchor cross-reference nits (UC-E2E-001 `#fixtures.compose` vs `#environment`; UC-E2E-006 `#fixtures.token_usage` vs `#fixtures.token_totals`) logged as follow-up for the ADR/I-01 stage (analyst write area). See `ISSUE-20261112-094931`. T-004 (U-01 UX flows and screens) unblocked — activation only by command.
