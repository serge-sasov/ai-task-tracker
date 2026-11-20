# ISSUE-20261121-005209: T036 chunk card scope

| Field | Value |
|---|---|
| id | ISSUE-20261121-005209 |
| status | resolved |
| severity | major |
| type | checkpoint |
| detected_at | 2026-11-21T00:52:09.7400535+03:00 |
| source | T-036 CP-1 ledger gate |
| role | orchestrator |
| location | docs/tasks/T-036-implement-s04-model.md (artifacts) + docs/chunks/T-036.json (owns/produced) |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

stage-done aborted at the chunk-ledger gate: chunk-ownership-no-overlap and chunk-coverage FAIL because the card artifacts listed repo-wide tests/** and an unmodified types.ts, while the chunk owns/produced covered the 8 frontend files

## Reproduction

1. Card `artifacts` declared `src/web/src/api/types.ts` (unmodified, still tracked) plus repo-wide `tests/**`.
2. Chunk C1 `owns` listed 6 of the 8 produced files and `produced` listed 8 files + `C1.brief.md`.
3. Run `verify-stage.ps1 -Task T-036 -Stage I-01` / `stage-done.ps1 -Task T-036 -Checkpoint CP-1` → the chunk-ledger gate runs `chunk-validate.py`.

## Expected / actual behavior

- Expected: CP-1 closes with `FAIL=0, UNCHECKED=0`; `chunk-ownership-no-overlap` and `chunk-coverage` PASS.
- Actual (before fix): 55+ errors — `E_COVERAGE` (unowned `tests/**` files + uncovered `types.ts`), `E_OWN_OUTSIDE_CARD` (owns outside artifacts), `E_PRODUCED_OUTSIDE` (produced outside owns).

## Cause

Two independent scope inconsistencies:
1. Card `artifacts` were not narrowed to the frontend-only intent (listed repo-wide `tests/**` and unmodified `types.ts`), so `chunk-ownership-no-overlap` / `chunk-coverage` could not reconcile with the chunk's owns.
2. The role passed the planning file `docs/chunks/T-036/C1.brief.md` into `-Produced`, so `produced` (9 paths) was a strict superset of `owns` (8 paths) — `E_PRODUCED_OUTSIDE`. The `produced` set is a machine field owned by `chunk.ps1`, and the canonical form (per done task T-024) is `produced == owns`; the brief is a planning file and must never appear in `produced`/`owns`.

## Solution / workaround

1. Option A (planning fields only): narrow card `artifacts` to the 8 produced frontend files (drop `tests/**` and `types.ts`, keep `types.ts` in `inputs`) and set chunk `owns` to the same 8-file set.
2. Re-record the machine field via its owning script (not a hand-edit): `chunk.ps1 complete -Task T-036 -Id C1 -Force -Produced "<8 owned files>" -Note "..."`. This resets `produced` to exactly the 8 owned files (brief excluded), matching the T-024 canonical form `produced == owns`. `chunk.ps1 status` shows C1 `done`; `chunk.ps1 verify` exits 0.

## Verification

- `chunk.ps1 verify -Task T-036` → `VALID T-036 (1 chunks)` (exit 0; only a `W_BUDGET_WRITE` warning on `package-lock.json` 98.3 KB, no `E_*`).
- `chunk.ps1 show -Task T-036 -Id C1` → `produced` holds exactly the 8 owned files, brief absent.
- Gate: `verify-stage.ps1 -Task T-036 -Stage I-01` must close CP-1 with `FAIL=0, UNCHECKED=0` (`chunk-ownership-no-overlap` and `chunk-coverage` PASS).

## Prevention / follow-up

- At planning keep card `artifacts` and chunk `owns` identical to the actually-produced files; never list repo-wide globs or unmodified files in artifacts for a frontend-only task.
- State it in the card launch protocol and the role brief: `-Produced` = the `owns` set only (the canonical T-024 form `produced == owns`); the brief/planning file is never included in `produced`. Re-record via `chunk.ps1 complete -Force`, never by hand-editing the ledger JSON.

## Links

T-036 card, docs/chunks/T-036.json, T-036 CP-1

## Timeline

- 2026-11-21T00:52:09.7400535+03:00: detected
- 2026-11-21T00:52:09+03:00: card scope narrowed (Option A) per T-036 card log; `produced` re-recorded via `chunk.ps1 complete -Force` to the T-024 canonical form (8 owned files, brief excluded); `chunk.ps1 verify` exits 0 — status set to `resolved`
