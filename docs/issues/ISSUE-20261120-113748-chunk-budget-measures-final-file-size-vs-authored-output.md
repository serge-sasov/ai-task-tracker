# ISSUE-20261120-113748: chunk-budget measures final file size vs authored output

| Field | Value |
|---|---|
| id | ISSUE-20261120-113748 |
| status | open |
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

`chunk-budget` (verify-artifacts.ps1 Check-ChunkLedger, `rule=budget`, and the parallel `chunk-validate.py`) measures each produced file's FINAL SIZE ON DISK (Get-Item Length) and compares it to the per-write/per-session budget, summing it toward the session budget.

harness/process.md 11.1 defines those limits as "Max per write/edit" and "Max new content per role session" -- i.e. a cap on AUTHORED model output, not on the resulting file size.

Git evidence (base_commit 4e3f839, the ledger base, vs HEAD after the 3 chunks):
- ui-spec-ai-task-tracker.md: base 20946 B -> head 22304 B, delta = +1358 B (1.3 KB) authored
- wireframes-ai-task-tracker.puml: base 9062 B -> head 9880 B, delta = +818 B (0.8 KB) authored
- wireframes-ai-task-tracker.svg: base 63099 B -> head 71879 B, delta = +8780 B (8.6 KB) -- DERIVED artifact rendered by the plantuml MCP, not authored text
- uc-e2e-005.html: base 16089 B -> head 18442 B, delta = +2353 B (2.3 KB) authored
Every authored delta is far under the 12 KB/write and 40 KB/session limits; the FAIL is driven by pre-existing file size (ui-spec already >12 KB before this task) and by counting the rendered SVG toward authored output.

Consequence: any task that EXTENDS an existing artifact already larger than the flat budget, or that produces a rendered diagram (SVG) as a declared artifact, will `chunk-budget: FAIL` regardless of actual authored volume. This is a check-design / registry gap, not a role budget breach.

Cause: mismatch between the measurement (final file size on disk) and the intended budget (authored output per write / per session) in the `chunk-budget` check (verify-artifacts.ps1 `Check-ChunkLedger` rule=budget + chunk-validate.py). The ledger also stores a single flat budget (max_write_kb=12, max_session_kb=40) that does not reflect 11.1's per-type budgets (md/puml 8/20; html 12/40; code 12/40), and the budget cannot distinguish authored text from a rendered/derived artifact.

## Solution / workaround

PROPOSED -- do not implement; this is for the user's ruling on T-033 CP-2 and a follow-up:
- Measure authored delta (git diff base_commit..HEAD bytes added for files owned by the chunk) instead of final file size, OR exclude rendered/derived artifacts (*.svg) from the budget, OR make the budget per-artifact-type.
- Until fixed, the lead treats this specific FAIL as a check-design gap (evidence above) rather than a role breach, and defers the CP-2 approval decision to the user.
- Related prior issue ISSUE-20261113-115617 (R-01 contracts exceeded session budget) was the same class of "total size vs session budget" tension and was resolved by splitting/accepting; this issue generalises it and adds the derived-SVG and pre-existing-file-size dimensions.

## Verification

- docs/checkpoints/T-033-CP-2.checks.json (chunk-budget FAIL; other 17 PASS / 1 MANUAL / 1 NOT_APPLICABLE)
- docs/chunks/T-033.json (3 chunks done, attempts=1, usage 0)
- git diff 4e3f839..HEAD shows the authored deltas above

## Prevention / follow-up

Add a follow-up task (type process, owner orchestrator/lead) to fix the `chunk-budget` rule to measure authored output and/or exclude derived artifacts; until then, log the disposition of any `chunk-budget: FAIL` in the affected card. Do NOT mark resolved until the check is fixed.

## Links

- docs/tasks/T-033-ux-prototype-s04-model.md
- docs/checkpoints/T-033-CP-2.checks.json
- docs/chunks/T-033.json
- harness/scripts/verify-artifacts.ps1 (Check-ChunkLedger)
- harness/scripts/chunk-validate.py
- ISSUE-20261113-115617

## Timeline

- 2026-11-20T11:37:48.4360867+03:00: detected
