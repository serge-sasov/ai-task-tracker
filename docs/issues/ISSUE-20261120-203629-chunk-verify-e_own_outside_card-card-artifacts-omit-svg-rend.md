# ISSUE-20261120-203629: chunk verify E_OWN_OUTSIDE_CARD: card artifacts omit .svg renders

| Field | Value |
|---|---|
| id | ISSUE-20261120-203629 |
| status | workaround |
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

The card's `artifacts` row enumerates the `.puml` sources (and the `swimlane-svc-*.puml`
waived diagram) but not their `.svg` render outputs. The chunk validator's ownership
check compares `owns` against the literal `artifacts` glob set, so the legitimate `.svg`
deliverables are reported as "outside the card". This is a card-authoring gap, not an
ownership violation: the SVGs are required deliverables and are inside the role write
area (`docs/architecture/**`).

## Solution / workaround

Workaround (recorded, not a code fix): the `.svg` globs are kept in `C2.owns` because the
card's own acceptance mandates them; the `E_OWN_OUTSIDE_CARD` failures are recognized as
false positives against an incomplete `artifacts` row. All substantive checks (syntax,
render, references, traceability) pass. The orchestrator should treat these two lines as
a known card-artifact defect for T-035 and not block the checkpoint on them.

Note on alternatives: removing the `.svg` from `owns` to satisfy `E_OWN_OUTSIDE_CARD`
instead triggers `E_PRODUCED_OUTSIDE` (the re-rendered `.svg` is a required deliverable
recorded in `produced`, and `produced` must stay inside `owns`). The two checks are
mutually exclusive while the card's `artifacts` row omits the `.svg`, so the ledger keeps
the truthful ownership (own the SVG it renders) and reports the card's under-listing as
the single defect.

## Verification

- `check_syntax` (plantuml-render) passes for `class-ai-task-tracker.puml` and
  `data-model-ai-task-tracker.puml`; both render to fresh `.svg` via `render_file`.
- `class-ai-task-tracker.svg` (90.6 KB) and `data-model-ai-task-tracker.svg` (19.5 KB)
  exist next to their `.puml`; both referenced in the architecture description.
- Card acceptance items 1–6 are satisfied (see C1/C2/C3 briefs and produced files).
- `chunk.ps1 verify -Task T-035`: with the truthful `owns` (includes `.svg`) the only
  residual error is the documented `E_OWN_OUTSIDE_CARD` (card omits `.svg`); all other
  checks (schema, hash, role area, coverage, no overlap) pass. This is the expected
  signature of this card defect, not a chunk error.

## Prevention / follow-up

When authoring an R-02 card that requires re-rendered diagrams, include the `*.svg`
renders in the `artifacts` row (e.g. `docs/architecture/class-*.puml/.svg`,
`docs/architecture/data-model-*.puml/.svg`) so the chunk-ownership check matches the
required deliverables. Suggest updating the card template / `harness-orchestrator`
card-authoring guidance accordingly.

## Links

- Task card: `docs/tasks/T-035-contracts-s04-model.md` (artifacts line 14; Acceptance line 29)
- Ledger: `docs/chunks/T-035.json` (chunk C2 `owns`)
- Artifacts: `docs/architecture/class-ai-task-tracker.puml/.svg`,
  `docs/architecture/data-model-ai-task-tracker.puml/.svg`

## Timeline

- 2026-11-20T20:36:29.2264141+03:00: detected
