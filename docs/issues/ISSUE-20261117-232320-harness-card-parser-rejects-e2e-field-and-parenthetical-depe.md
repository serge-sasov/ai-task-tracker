# ISSUE-20261117-232320: Harness card parser rejects e2e field and parenthetical depends_on

| Field | Value |
|---|---|
| id | ISSUE-20261117-232320 |
| status | resolved |
| severity | major |
| type | tool |
| detected_at | 2026-11-17T23:23:20.1843101+03:00 |
| source | T-017 P-00 |
| role | harness-orchestrator |
| location | C:\study\ai\research\ai-task-tracker\harness\scripts\harness-lib.ps1:166; C:\study\ai\research\ai-task-tracker\harness\scripts\verify-artifacts.ps1:623 |
| environment | Windows PowerShell 5.1; local-harness 3.0.0 |

## Symptoms

verify-stage P-00 failed with card-fields 'missing: e2e' for all cards even though the e2e row is present; deps-acyclic reported unknown deps for T-003/T-004/T-005/T-013/T-015 because depends_on values contain notes such as 'T-002 (done)' and 'T-009 (held at C7)'.

## Reproduction

1. In Windows PowerShell 5.1, dot-source `harness/scripts/harness-lib.ps1` and call `Get-CardFields` on `docs/tasks/T-016-verify-bug-003-local-stack.md`; `$fields.ContainsKey('e2e')` returns `$false` even though the card has an `| e2e | ... |` row.
2. Run the P-00 `verify-stage.ps1` checks; `card-fields` reports `missing: e2e` for every card.
3. Run `deps-acyclic` over the board; cards T-003, T-004, T-005, T-013 and T-015 report unknown dependencies because their `depends_on` values contain notes such as `T-002 (done)` and `T-009 (held at C7)`.

## Expected / actual behavior

- Expected: `e2e` and other digit-bearing field names are parsed from card tables; dependency notes in parentheses are ignored when resolving IDs; both checks pass.
- Actual: `card-fields` FAILs with `missing: e2e` on all cards; `deps-acyclic` FAILs with `depends on unknown T-002 (done)`-style errors for five historical cards.

## Cause

Get-CardFields matched field names with [A-Za-z_]+ only, so the digit in 'e2e' prevented the field from being parsed. Check-DepsAcyclic split depends_on without removing parenthetical notes.

## Solution / workaround

Changed the card field key regex to [A-Za-z0-9_]+ and made Check-DepsAcyclic strip parenthetical notes before splitting. Added self-test cases 'card-fields e2e parsed' and 'deps-acyclic parenthetical'.

## Verification

verify-artifacts.ps1 -SelfTest: SELF-TEST PASSED, including both new cases. Direct Get-CardFields on T-016 returns e2e='required - the full T-009 suite is the acceptance evidence' and ux='not applicable (backend/infrastructure)'.

## Prevention / follow-up

Regression self-tests in verify-artifacts.ps1 cover digit-bearing field names and annotated depends_on values.

## Links

T-017 P-00; harness-lib.ps1; verify-artifacts.ps1; commit to follow.

## Timeline

- 2026-11-17T23:23:20.1843101+03:00: detected
