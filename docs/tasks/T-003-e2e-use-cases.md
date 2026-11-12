# T-003: E2E use cases and YAML dataset (A-02)

| Field | Value |
|---|---|
| id | T-003 |
| title | E2E use cases and YAML dataset (A-02) |
| workflow | feature |
| stage | A-02 |
| role | analyst |
| status | awaiting_review |
| depends_on | T-002 (done) |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/analysis/glossary.md`, `requirements/исходные требования.txt` |
| artifacts | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` |
| e2e | required — this stage produces the e2e contract (use cases + YAML dataset) |
| ux | not applicable — A-02 is an analysis stage with no UI design; UX is owned by U-01/U-02 stages |
| issues | ISSUE-20261112-094931 |
| commit | d9ca867 |

## Acceptance

- [ ] Every functional requirement (R-NNN) from `docs/analysis/requirements-ai-task-tracker.md` is covered by at least one UC-E2E-NNN or has an explicit out_of_scope question/waiver.
- [ ] Every use case defines: source requirements, actor, priority, preconditions, trigger, main/alternate/error flows, test data, expected result, postconditions, cleanup, traceability (per `harness/process.md` section 7).
- [ ] YAML dataset (machine-readable test contract): schema, environment placeholders (no secrets), use-case entries, steps, data, expected values, cleanup.
- [ ] Traceability table R-NNN -> UC-E2E-NNN in the use-cases doc.

## Checkpoints

| ID | Stage | Artifacts | Status | Review file |
|---|---|---|---|---|
| CP-2 | A-02 e2e use cases and dataset | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` | awaiting_review | `docs/checkpoints/T-003-CP-2.md` |
| CP-F | final acceptance | all task artifacts | pending | `docs/checkpoints/T-003-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- none

## Log

- 2026-11-11: T-003 started (A-02 e2e use cases and dataset); card created; T-003 -> in_progress; board and diagram updated.
- 2026-11-11 19:12-23:20: analyst session `ses_e349e85afffepgtl3SJOIZMdPi` produced the artifacts after three failed attempts and a manual fourth attempt: 19:34 `SSE read timed out` (~50 min single generation, no artifact); 20:25 `finish=length` at 16 384 output tokens (single `write` of the whole Markdown, no file); 21:02 `SSE read timed out` (~58 min, no artifact); 22:01-23:07 success via skeleton + per-UC edits and YAML in 3 parts; 23:08 context 63 901/65 536 (97.5%), second compaction; 23:20 aborted. Totals: input 136 497, output 70 443, cache read 910 312, 2 compactions. Root cause: no generation budget/decomposition and no role self-sub-sessions; see `ISSUE-20261112-094931` (harness repo `docs/issues/`).
- 2026-11-11: CP-2 reached: analyst artifacts produced (e2e use cases + YAML dataset, verified complete by lead; the role session was cut by client SSE timeouts after the artifacts were written; per process.md §11 resumable, not a failure). T-003 -> awaiting_review; checkpoint file `docs/checkpoints/T-003-CP-2.md` created; board and diagram updated; snapshot committed.
