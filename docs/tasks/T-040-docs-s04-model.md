# T-040: D-01 Documentation for S-04 model column

| Field | Value |
|---|---|
| id | T-040 |
| title | D-01 Documentation for S-04 model column |
| workflow | feature |
| stage | D-01 |
| role | doc-writer |
| status | pending |
| hold | false |
| depends_on | T-039, T-036, T-035, T-033 |
| inputs | `README.md`, `docs/guides/*`, `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md`, `docs/qa/e2e-report-s04-model.md`, `docs/ux/ui-spec-ai-task-tracker.md` |
| artifacts | `README.md`, `docs/guides/*` |
| e2e | not applicable (reason: documentation only; the e2e evidence is owned by Q-02/T-039) |
| ux | not applicable (reason: documentation only; the UX spec is owned by U-02/T-033) |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [ ] The user-facing documentation (README or `docs/guides/*`) is updated with the S-04 day-view "Модель" column: location (`/calendar/:date`), the cell states (primary model, `+N`, tooltip/`aria-label` full list, "Нет данных"), and the note that the data comes from the existing `GET /api/tasks` `models` field.
- [ ] The documentation scope is the S-04 delta over the T-012 baseline D-01: if T-012 still owns the README, T-040 edits only the S-04 delta sections and does not rewrite the baseline content.
- [ ] The relevant ADR for the shared model-cell reuse is finalized (decision, alternatives, consequence) and linked from the documentation.
- [ ] The documentation is consistent with the UX spec, the architecture description, and the QA verdict (T-039); no contradiction is introduced.
- [ ] The documentation build/lint (if any) passes and the new sections are linked from the table of contents.
- [ ] `CP-1` (documentation draft) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | D-01 documentation draft | `README.md`, `docs/guides/*` | - | pending | `docs/checkpoints/T-040-CP-1.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-040-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Launch protocol (anti-hang)

- Start with a 5-line plan and the first tool call; do not spend more than 60 seconds or 2k output tokens in analysis before writing the first artifact.
- Read only the card inputs and the specific line ranges; do not scan the whole repository or re-read completed artifacts.
- For chunked stages, run `chunk.ps1 init/add/plan` before authoring; execute one chunk per session; stop at the chunk boundary.
- Budgets: Markdown 8 KB/write and 20 KB/session; code 12 KB/write and 40 KB/session; stop at 60% context (65% for code); QA final message <=10 lines.
- If no artifact is produced within 15 minutes or after 2 attempts, stop and escalate: mark the chunk `failed`/`blocked` with the error; do not repeat the same oversized call.
- At the start of every role session, emit only `START T-XXX <stage> chunk <id>` and proceed with the first tool call; avoid long free-form reasoning.

## Blockers

- (none)

## Log

- 2026-11-19: T-040 registered as a pending D-01 successor of T-039/T-036/T-035/T-033 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
