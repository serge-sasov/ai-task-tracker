# T-033: U-02 Wireframes, prototype and UI spec for S-04 model column

| Field | Value |
|---|---|
| id | T-033 |
| title | U-02 Wireframes, prototype and UI spec for S-04 model column |
| workflow | feature |
| stage | U-02 |
| role | ux-designer |
| status | pending |
| hold | false |
| depends_on | T-032 |
| inputs | `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/index.html`, `docs/ux/prototype/ai-task-tracker/uc-e2e-005.html`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/tasks/T-032-ux-flows-s04-model.md` |
| artifacts | `docs/ux/wireframes-ai-task-tracker.puml`, `docs/ux/wireframes-ai-task-tracker.svg`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/uc-e2e-005.html` |
| e2e | required - the prototype page for UC-E2E-005 must render the S-04 model column states |
| ux | required - the S-04 model cell (primary, `+N`, tooltip/aria, "Нет данных") must be specified and prototyped |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [ ] `docs/ux/ui-spec-ai-task-tracker.md` is extended with the S-04 day-view "Модель" cell specification: primary model, `+N` badge, `title`/`aria-label` full ordered list, and "Нет данных", with the same tokens/labels as the S-01 cell.
- [ ] The prototype page `docs/ux/prototype/ai-task-tracker/uc-e2e-005.html` is updated to render the S-04 day table with the model column in all four states, and the page links and structure remain valid (proto-structure, proto-links, proto-offline checks).
- [ ] The wireframes `wireframes-ai-task-tracker.puml` are extended with the S-04 day view and re-rendered to `wireframes-ai-task-tracker.svg`; the diagram passes syntax and SVG checks.
- [ ] The UI spec traces the S-04 model cell to UC-E2E-005 and `R-015`, and reuses the S-01 model-cell accessibility text; no conflicting token is introduced.
- [ ] The prototype is the single-user Russian variant and does not depend on external assets (offline check passes).
- [ ] `CP-2` (wireframes, prototype, UI spec) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | U-02 wireframes, prototype, UI spec | `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/wireframes-ai-task-tracker.puml`, `docs/ux/wireframes-ai-task-tracker.svg`, `docs/ux/prototype/ai-task-tracker/uc-e2e-005.html` | - | pending | `docs/checkpoints/T-033-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-033-CP-F.md` |

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

- 2026-11-19: T-033 registered as a pending U-02 successor of T-032 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
