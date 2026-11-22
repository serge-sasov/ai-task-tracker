# ISSUE-20261120-113748: chunk-budget measures final file size vs authored output

| Field | Value |
|---|---|
| id | ISSUE-20261120-113748 |
| status | resolved |
| severity | major |
| type | process |
| detected_at | 2026-11-20T11:37:48.4360867+03:00 |
| source | harness (lead) |
| role | ux-designer |
| location | C:\study\ai\research\ai-task-tracker\harness\scripts\verify-artifacts.ps1 (Check-ChunkLedger rule=budget) + C:\study\ai\research\ai-task-tracker\harness\scripts\chunk-validate.py |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

Task T-033 (feature, stage U-02, role ux-designer) checkpoint CP-2 (snapshot commit 0560916, checks file docs/checkpoints/T-033-CP-2.checks.json) reports `chunk-budget: FAIL` (FAIL=1 UNCHECKED=0 MANUAL=1 PASS=17 NOT_APPLICABLE=1), so P2 cannot auto-approve and the task is stuck in `awaiting_review` even though all semantic acceptance criteria and the other 17 checks pass.

FAIL detail:
- T-033-c1: docs/ux/ui-spec-ai-task-tracker.md 21.8 KB > 12 KB write
- T-033-c2: docs/ux/wireframes-ai-task-tracker.svg 70.2 KB > 12 KB write
- T-033-c2: 79.8 KB > 40 KB session
- T-033-c3: docs/ux/prototype/ai-task-tracker/uc-e2e-005.html 18 KB > 12 KB write

## Reproduction

1. 
2. 
3. 

## Expected / actual behavior

- Expected:
- Actual:

## Cause

Fixed in commit `bd2b736` (harness: reduce process overhead, H-01..H-09). `harness/budgets.json` now defines `default_mode: authored-delta`, excludes derived paths (`**/*.svg`, lockfiles, build outputs), and provides per-type limits and profiles. `chunk-validate.py` supports `mode=authored-delta`, `verify-artifacts.ps1` `Check-ChunkLedger` delegates to the authored-delta rule when the ledger requests it, and `harness/process.md` 11.1 documents both modes (`authored-delta` for new ledgers, `legacy-final-size` for historical ledgers).

## Solution / workaround

Implemented by `bd2b736`: measure authored delta instead of final file size, exclude derived artifacts (`*.svg`), and apply per-type budgets from `harness/budgets.json`. The flat 12/40 KB defaults remain only as a fallback for old ledgers. New ledgers (T-038, T-039, T-040) record `mode=authored-delta`.

## Verification

- `docs/checkpoints/T-038-CP-1.checks.json`, `T-038-CP-F.checks.json`, `T-039-CP-2.checks.json`, and `T-039-CP-F.checks.json`: `chunk-budget` PASS with detail `authored-delta`.
- `python harness/scripts/chunk-validate.py docs/chunks/T-035.json --root . --json --no-hash`: `valid: true`, only `W_BUDGET_WRITE` warnings from the legacy ledger (final-size mode), no errors.
- T-035, T-038, and T-039 checkpoints closed without a budget FAIL.

## Prevention / follow-up

Implemented: new ledgers use `authored-delta` and `harness/budgets.json`; keep derived renders out of authored budgets via the `exclude` list. Historical ledgers keep `legacy-final-size`; do not reinterpret their warnings as authored-output breaches.

## Links

- docs/tasks/T-033-ux-prototype-s04-model.md
- docs/checkpoints/T-033-CP-2.checks.json
- docs/chunks/T-033.json
- harness/scripts/verify-artifacts.ps1 (Check-ChunkLedger)
- harness/scripts/chunk-validate.py
- ISSUE-20261113-115617

## Timeline

- 2026-11-20T11:37:48.4360867+03:00: detected
- 2026-11-20: fixed by `bd2b736` (authored-delta, `harness/budgets.json`, `*.svg` exclusion, per-type budgets)
- 2026-11-22T18:48:30+03:00: resolved - T-038/T-039 checkpoints report `chunk-budget` PASS `authored-delta`; T-035 validator returns `valid: true` with legacy warnings only
