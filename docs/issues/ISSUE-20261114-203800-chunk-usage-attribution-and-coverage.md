# ISSUE-20261114-203800: chunk usage attribution and coverage gaps found in the T-041 pilot

| Field | Value |
|---|---|
| id | ISSUE-20261114-203800 |
| status | resolved |
| severity | minor |
| type | tool |
| detected_at | 2026-11-14T20:38:00+03:00 |
| source | T-041 pilot (harness 3.0.0-dev) |
| role | orchestrator / coder |
| location | `harness/scripts/chunk-validate.py`, `harness/scripts/chunk.ps1`, `harness/scripts/harness-lib.ps1` |
| environment | opencode 1.18.33, Windows 10, local harness 3.0.0-dev in ai-task-tracker |

## Symptoms

1. `chunk-validate.py` reported `E_COVERAGE` for a correct fine-grained plan:
   the card declares `src/**` and `tests/**`, but chunk `owns` are exact
   packages, so the union never equals the coarse glob.
2. `chunk.ps1 usage` reported `no sessions` for chunks that had run, and after the
   first fix it attributed the entire root session (all 1.3M input tokens) to one
   chunk.
3. A chunk session created from the harness host project is stored with
   `directory = C:/study/ai/research/harness`, so a project filter on
   `ai-task-tracker` excluded it.

## Reproduction

1. Create a ledger whose `owns` are package globs for a card with `src/**`.
2. Run `chunk-validate.py` -> `E_COVERAGE` for every existing file.
3. Run a chunk whose session title has no `T-XXX.<chunk>` token, then
   `chunk.ps1 usage -Task T-XXX` -> `no sessions`.
4. Name the session `T-XXX.C5a` and re-run -> the root session's tokens are
   charged to the chunk.

## Cause

- Coverage compared glob-to-glob instead of glob-to-real-file.
- Usage was aggregated by root session title and by project directory, while a
  chunk session can be a child of a differently-titled root in another project.

## Solution

- Chunk-ledger coverage now walks the repository files: every existing file that
  matches a card artifact must be owned by a top-level chunk, and no chunk may own
  a path outside the card artifacts. Pre-ledger work is declared in the ledger
  `baseline` array (`chunk.ps1 init -Baseline`); baseline files are exempt.
- Usage resolves the **nearest ancestor** title that carries `T-XXX` / `T-XXX.<chunk>`
  per session, aggregates by that identity, and supports `-Session <id>` for a
  subtree and `-AllProjects` when the session lives in another directory.
- Convention: a chunk session title must start with `T-XXX.<chunk>` so attribution
  works without an explicit session id.

## Verification

- `chunk-validate.py --self-test` -> 13 cases, 0 failures (added the baseline case
  and the real-file coverage case).
- `chunk.ps1 usage -Task T-008 -AllProjects` reports
  `C5a in=81527 out=47715 cr=3992331 min=21 ratio=49x` for the C5a subtree, and
  `usage -Id C4fix -Session ses_e25145a13ffeVLdK5y0394w4Lc` reports
  `in=184650 out=49964 cr=6596097 min=26 ratio=35.7x`.
- `chunk-validate.py docs/chunks/T-008.json` -> VALID with 7 chunks.

## Prevention / follow-up

- `chunk-ledger.md` documents `baseline`, the coverage rule, and the
  `T-XXX.<chunk>` title convention.
- ADR-006 decision 5 updated to the real-file coverage semantics.
- The pilot metrics file replaces `cr/in` with turns and cache-read per turn as
  the primary criteria, because `cr/in` rises when a chunk gets smaller.

## Links

- `docs/chunks/T-008.json`, `docs/chunks/T-008/*.brief.md`
- Harness repo: `docs/metrics/T-041-pilot.md`, ADR-006, `harness/templates/chunk-ledger.md`
- Harness repo: `ISSUE-20261114-193500` (depth change requires a restart)

## Timeline

- 2026-11-14T20:03:00+03:00: coverage false positive found while validating the T-008 plan.
- 2026-11-14T20:20:00+03:00: usage attribution returned `no sessions` for C5a.
- 2026-11-14T20:35:00+03:00: validator and attribution fixed, both chunks measured.
