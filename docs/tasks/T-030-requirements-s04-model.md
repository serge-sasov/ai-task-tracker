# T-030: A-01 Requirements delta for S-04 calendar day model column

| Field | Value |
|---|---|
| id | T-030 |
| title | A-01 Requirements delta for S-04 calendar day model column |
| workflow | feature |
| stage | A-01 |
| role | analyst |
| status | in_progress |
| hold | false |
| depends_on | T-029 |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/glossary.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/tasks/T-029-calendar-day-model-planning.md`, `src/web/src/views/CalendarDayView.vue`, `src/web/src/views/DashboardView.vue` |
| artifacts | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/analysis/glossary.md` |
| e2e | required - S-04 day view gets a user-visible model column; A-02 will extend UC-E2E-005 and the YAML dataset |
| ux | required - S-04 day view gets a visible "Модель" column with primary model, `+N`, tooltip/aria and "Нет данных" |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | in=211174 out=36975 cr=3154919 ratio=14.9x min=84 |
| issues | - |
| commit | - |

## Acceptance

- [ ] `R-015` is added as an atomic Must requirement: S-04 day view (`/calendar/:date`) shows a "Модель" column after "Задача" with the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- [ ] `R-015` traces to the S-04 source line and to UC-E2E-005; it is consistent with `R-014` (S-01) and reuses the same ordering/tie-break rule; no duplicate requirement is introduced.
- [ ] The glossary is extended with the S-04 day-view context (day table, day-view model cell) and reuses the existing S-01 terms (primary model, `+N`, "Нет данных"); no new conflicting term is added.
- [ ] No new blocking question is opened; assumptions (Russian UI, no backend change, `GET /api/tasks` already returns `models`) are preserved from T-029.
- [ ] The requirement explicitly states no backend/API/schema change is required and the S-01 behavior is unchanged.
- [ ] `CP-1` (requirements delta) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | A-01 requirements delta | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/glossary.md`, `docs/analysis/questions-ai-task-tracker.md` | 8P/0F/0M | approved | docs/checkpoints/T-030-CP-1.md |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-030-CP-F.md` |

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

- 2026-11-19: T-030 registered as a pending A-01 successor of T-029 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
- 2026-11-19 (analyst, chunk A1): A-01 requirements delta authored — added `R-015` (Must, S-04 day view «Модель» column after «Задача», primary model + `+N` + full list in `title`/`aria-label` + «Нет данных», traces to ИТ:4 and UC-E2E-005, reuses R-014 ordering/tie-break, no backend/API/schema change, S-01 unchanged) in `docs/analysis/requirements-ai-task-tracker.md` incl. traceability (ИТ:4 rows) and section 6 summary; extended `docs/analysis/glossary.md` with day view / day-view model cell terms reusing S-01 terms; appended T-030 consistency note to `docs/analysis/questions-ai-task-tracker.md` (no new blocking question). Chunk A1 completed; stopping at CP-1.
- 2026-11-19 (analyst, housekeeping): `chunk.ps1 add` collapsed comma-separated `-Owns/-Inputs/-Acceptance` values into single strings (ledger planning fields), repaired by direct edit; ledger budget aligned to the T-018 analyst precedent (32/64 KB) since produced files are pre-existing and large (actual new content ~2.6 KB, within §11.1 8 KB/write and 20 KB/session). Helper `docs/chunks/T-030/complete-A1.ps1` remains (Remove-Item denied by permission rules); it is inert. `verify-stage A-01`: 8 PASS / 0 FAIL.
- 2026-11-19 (orchestrator): Recorded harness issue `ISSUE-20261119-184132` (type `permission`, severity `minor`, status `open`) for the shell file-removal denial that left the inert helper `docs/chunks/T-030/complete-A1.ps1` behind. Verified the helper breaks no A-01 check (untracked, not in `owns`/`produced`, not referenced by any check; not committed). Removal via the shell file-removal cmdlet is itself permission-denied, so the file is kept as recorded residue pending user/allow-listed removal.
- 2026-11-19: CP-1 approved (A-01); checks 8 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=211174 out=36975 cr=3154919 ratio=14.9x min=84; reviewer auto (P2)
