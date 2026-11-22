# ISSUE-20261120-203629: chunk verify E_OWN_OUTSIDE_CARD: card artifacts omit .svg renders

| Field | Value |
|---|---|
| id | ISSUE-20261120-203629 |
| status | resolved |
| severity | minor |
| type | process |
| detected_at | 2026-11-20T20:36:29.2264141+03:00 |
| source | T-035 |
| role | architect |
| location | docs/chunks/T-035.json (C2.owns) |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

`chunk.ps1 verify -Task T-035` returns `INVALID` with two `E_OWN_OUTSIDE_CARD`
failures: `owns 'docs/architecture/class-*.svg' is outside the card artifacts` and
the same for `docs/architecture/data-model-*.svg`. All three chunks are `done` and
all card acceptance items are met; the failure is purely a coverage/ownership check
against the card's `artifacts` row.

## Reproduction

1. `docs/tasks/T-035-contracts-s04-model.md` `artifacts` row lists
   `class-*.puml`, `data-model-*.puml`, `swimlane-svc-*.puml` — but no `*.svg`.
2. The same card's Acceptance (line 29) and the architect role rules require the
   diagram `.svg` to be re-rendered and pass `svg-fresh`.
3. The chunk for the diagrams therefore must own the `.svg` globs (they are produced
   files in the role write area), which the coverage check then flags as outside the
   card artifacts.

## Expected / actual behavior

- Expected: a chunk may own the rendered `.svg` next to a `.puml` it edits, since the
  card requires those SVGs to exist and be fresh.
- Actual: `E_OWN_OUTSIDE_CARD` fails the chunk verify because the `artifacts` row omits
  the `.svg` files.

## Cause

Fixed at card level: `docs/tasks/T-035-contracts-s04-model.md` `artifacts` now lists `docs/architecture/data-model-*.svg` and `docs/architecture/class-*.svg` next to their `.puml` sources (card log 2026-11-20). The chunk validator now sees the SVG deliverables inside the card artifacts, so the truthful `owns` no longer triggers `E_OWN_OUTSIDE_CARD`.

## Solution / workaround

Implemented: the T-035 card `artifacts` row was corrected to include the `.svg` renders it mandates. `chunk-validate.py docs/chunks/T-035.json` now returns `valid: true` with only legacy `W_BUDGET_WRITE` warnings. No validator change was needed; the ownership check was correct.

## Verification

- `python harness/scripts/chunk-validate.py docs/chunks/T-035.json --root . --json --no-hash`: `valid: true`, `errors: []`, warnings are legacy final-size budget only.
- `docs/tasks/T-035-contracts-s04-model.md` line 14 lists `data-model-*.svg` and `class-*.svg`; card log 2026-11-20 records the fix and the re-run.
- `class-ai-task-tracker.svg` (90.6 KB) and `data-model-ai-task-tracker.svg` (19.5 KB) exist next to their `.puml` and are referenced in the architecture description.

## Prevention / follow-up

Implemented: `harness/templates/task-card-feature.md` now requires derived renders in `artifacts`, and `harness/agents/harness-orchestrator.md` tells the orchestrator to list both `.puml` and `.svg` when acceptance requires a render. This prevents the card-authoring gap from recurring.

## Links

- Task card: `docs/tasks/T-035-contracts-s04-model.md` (artifacts line 14; Acceptance line 29)
- Ledger: `docs/chunks/T-035.json` (chunk C2 `owns`)
- Artifacts: `docs/architecture/class-ai-task-tracker.puml/.svg`,
  `docs/architecture/data-model-ai-task-tracker.puml/.svg`

## Timeline

- 2026-11-20T20:36:29.2264141+03:00: detected
- 2026-11-20: T-035 card `artifacts` fixed to include the `.svg` renders; CP-2 re-verified
- 2026-11-22T18:48:30+03:00: resolved - `chunk-validate.py` T-035 returns `valid: true`; template and orchestrator prevention added
