# T-XXX <chunk>: session handoff

| Field | Value |
|---|---|
| task | T-XXX |
| chunk | C1 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| reason | context compaction / transport timeout / session limit |
| state | failed / in_progress |

## Goal

One sentence: what this chunk must produce.

## Done

- paths already produced and verified
- state of the chunk (`chunk.ps1 status`)

## Next

- the next concrete action for a fresh session
- the acceptance bullet still open

## Input pointers

- `path/to/input.md` lines N-M (do not paste content)
- existing file/function to edit

## Stop rule

Stop at the chunk boundary or when the budget/context limit is reached; record
the state with `chunk.ps1 complete|fail -Handoff docs/chunks/T-XXX/<id>.handoff.md`.

## Return

At most ten lines: produced paths, state, and any issue id. No artifact text.
