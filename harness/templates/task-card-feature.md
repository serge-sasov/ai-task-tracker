# T-XXX: Short feature task title

| Field | Value |
|---|---|
| id | T-XXX |
| title | Full task title |
| workflow | feature |
| stage | P-00 / A-01 / A-02 / U-01 / U-02 / R-01 / R-02 / I-01 / I-02 / Q-01 / Q-02 / D-01 / F-01 |
| role | orchestrator / analyst / ux-designer / architect / coder / qa / doc-writer |
| status | pending |
| hold | false (true forces human review at every checkpoint) |
| manual_carryover | false (true carries CP-2 manual items to CP-F only when artifacts are byte-identical; CP-F is still user-reviewed) |
| scope_class | delta / new-screen / cross-cutting |
| budget_profile | `default` / `U-02/new-screen` / `U-02/delta` / ... (from `harness/budgets.json`) |
| depends_on | T-YYY, T-ZZZ |
| inputs | comma-separated input artifact paths |
| artifacts | comma-separated output artifact paths; include derived renders required by acceptance (for example both `.puml` and `.svg`) |
| e2e | required / not applicable (reason) |
| ux | required / not applicable (reason) |
| chunks | ledger path and granularity: `docs/chunks/T-XXX.json` (single source of truth for per-chunk state, `owns`, and usage); the plan is written with `harness/scripts/chunk.ps1` before authoring |
| budget | `budget_profile` selects per-type + authored-delta limits from `harness/budgets.json` (`max_write_kb`, `max_session_kb`, `context_stop_pct`, `max_turns`, `max_input_tokens`, `max_wall_min`); the ledger `budget` object holds the effective values |
| base_commit | commit hash the first checkpoint diff is measured from |
| usage | measured token usage (filled by stage-done; e.g. in=0 out=0 cr=0 ratio=0x min=0) |
| issues | ISSUE-... (links to docs/issues/ if problems occurred) |
| commit | latest commit hash (snapshot or closure) |

For R-01/R-02, `inputs` must list the current requirements, e2e use cases (when
in scope), UX specs, and the architecture description; trace checks read
`card.inputs`.

## Acceptance

- [ ] Criterion 1 (verifiable).
- [ ] Criterion 2.
- [ ] Criterion 3.

## Launch protocol (anti-hang)

- Start with a 5-line plan and the first tool call; do not spend more than 60 seconds or 2k output tokens in analysis before writing the first artifact.
- Foreground commands must finish within 60 seconds; the bash timeout is at most 120 seconds.
- Start `docker compose up/build`, live pytest, and long builds with `harness/scripts/run-monitored.ps1 start`; poll `status`/`tail`/`wait -WaitSec 30`. The project e2e runner `run` is detached by default and already uses the wrapper; use its `status`/`abort`.
- Never use `Start-Process -Wait`, `Out-File`, `Tee-Object`, or dot-source a dispatching script without `-NoExit`; react to the first exit code/log tail, not to a timeout.
- If no artifact is produced within 15 minutes or after 2 failed attempts, stop and mark the chunk `failed`/`blocked` with the error; do not repeat the same blocking command.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | stage-specific | `docs/...` | - | pending | `docs/checkpoints/T-XXX-CP-1.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-XXX-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`,
`rejected`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- YYYY-MM-DD: what was done, files, commands, results; checkpoint commits and user decisions.
