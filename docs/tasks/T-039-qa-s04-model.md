# T-039: Q-02 QA, acceptance and verdict for S-04

| Field | Value |
|---|---|
| id | T-039 |
| title | Q-02 QA, acceptance and verdict for S-04 |
| workflow | feature |
| stage | Q-02 |
| role | qa |
| status | in_progress |
| hold | false |
| depends_on | T-038, T-037, T-036, T-035, T-033 |
| inputs | `docs/qa/e2e-plan-s04-model.md`, `tests/e2e/**`, `src/web/src/views/CalendarDayView.vue`, `src/web/src/components/ModelCell.vue`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/analysis/requirements-ai-task-tracker.md` |
| artifacts | `docs/qa/e2e-report-s04-model.md`, `docs/qa/review-T-039.md` |
| e2e | required - the S-04 e2e results and verdict are the acceptance evidence |
| ux | required - the UX spec cell states and accessibility text are part of the review |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | in=312905 out=64703 cr=5106899 ratio=16.3x min=156 |
| issues | - |
| commit | - |

## Acceptance

- [ ] `docs/qa/e2e-report-s04-model.md` records the execution of the S-04 e2e test(s) against the built frontend (rebuilt image or DOM smoke), with PASS/FAIL per assertion and the actual observed cell states (primary, `+N`, full list, "Нет данных").
- [ ] `docs/qa/review-T-039.md` contains a text/code review of the shared model-cell component and its use in both `DashboardView.vue` and `CalendarDayView.vue`, confirming the S-01 behavior is unchanged and the S-04 column matches the UX spec and `R-015`.
- [ ] The S-01 regression (UC-E2E-003 and the S-01 unit/component tests) is re-run and reported green; any regression is recorded as a defect with severity.
- [ ] A final verdict (accept / reject) is stated with the evidence references; if accepted, the acceptance criteria of T-029 are confirmed met.
- [ ] Any defect found is recorded with a linked `BUG-XXX` (created by the orchestrator) and is not silently absorbed into the feature.
- [ ] `CP-2` (e2e report and verdict) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | Q-02 e2e report and verdict | `docs/qa/e2e-report-s04-model.md`, `docs/qa/review-T-039.md` | 8P/0F/0M | approved | docs/checkpoints/T-039-CP-2.md |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-039-CP-F.md` |

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

- 2026-11-19: T-039 registered as a pending Q-02 successor of T-038/T-037/T-036/T-035/T-033 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
- 2026-11-22: started via `/harness-run` — status `in_progress`; board invariant verified (no other active task; dependencies T-038/T-037/T-036/T-035/T-033 all `done`); stage Q-02, role qa.
- 2026-11-22: `scope_class` and `budget_profile` to be classified at chunk planning (`harness/scripts/chunk.ps1 init`); QA role granularity per `harness/process.md` 11.1; self-delegation `d2` only; budget profile from `harness/budgets.json`.
- 2026-11-22: Q-02 chunk plan done (`chunk.ps1 init` + C1/C2 added with briefs). **C1 done**: S-04 e2e 7/7 PASS, S-01 regression e2e 24/24 PASS, S-01 unit/component 8/8 PASS (final combined run with `E2E_KEEP_STACK=1`; evidence `tests/e2e/results/UC-E2E-005.json`, `UC-E2E-003.json`); report `docs/qa/e2e-report-s04-model.md` written; no defects. CP-2 reported; C2 (code review + verdict) pending next session.
- 2026-11-22: **C2 done**: code/text review of `ModelCell.vue`, `DashboardView.vue` (S-01 unchanged), `CalendarDayView.vue` (S-04, R-015) + cross-view consistency; no product defects (2 informational notes); **verdict ACCEPT**; `docs/qa/review-T-039.md` written; `verify-stage.ps1 -Stage Q-02` = 8 PASS/0 FAIL; chunk ledger 2/2 done. **CHECKPOINT CP-2 reached** — ready for lead preflight + stage-done.
- 2026-11-22: CP-2 approved (Q-02); checks 8 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=312905 out=64703 cr=5106899 ratio=16.3x min=156; reviewer auto (P2)
