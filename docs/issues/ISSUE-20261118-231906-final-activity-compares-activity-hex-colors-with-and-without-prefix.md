# ISSUE-20261118-231906: final-activity compares activity hex colors with and without the # prefix

| Field | Value |
|---|---|
| id | ISSUE-20261118-231906 |
| status | resolved |
| severity | major |
| type | tool |
| detected_at | 2026-11-18T23:19:06+03:00 |
| source | T-028 CP-F |
| role | harness-orchestrator |
| location | C:\study\ai\research\ai-task-tracker\harness\scripts\verify-artifacts.ps1 (Check-ActivityDerived, ~line 600-604) |
| environment | Windows PowerShell 5.1; local-harness 3.0.0 |

## Symptoms

`stage-done.ps1 -Task T-028 -Checkpoint CP-F` reported `final-activity` FAIL even though the
board and the activity diagram are fully consistent. Every `done` task "failed" with the same
mismatch:

```
[FAIL] final-activity  T-001 color=90EE90 expected=#90EE90; T-002 color=90EE90 expected=#90EE90; ... (T-001..T-027 all identical)
```

The board, card and activity all agree (all completed tasks are `#90EE90`; T-028 is `#FFE082`),
yet the check reported a FAIL for every green node, blocking the F-01 closure with `FAIL=1`.

## Reproduction

1. Any board where at least one task is `done` (color `#90EE90`) and `docs/activity.puml` holds
   `:T-XXX ...; <<#90EE90>>`.
2. Run a stage whose check set includes `final-activity` (type `activity-derived`), e.g. F-01.
3. `Check-ActivityDerived` reports `FAIL ... color=90EE90 expected=#90EE90` for every `done` row.

The failure is uniform and independent of the actual data, so it masks real board/activity
inconsistencies.

## Expected / actual behavior

- Expected: `final-activity` passes when the activity color matches the status color, regardless
  of whether the stored hex carries a leading `#`.
- Actual: the captured hex (no `#`) was compared against `Get-BoardStatusColor`'s value (with
  `#`), so `"90EE90" -ne "#90EE90"` was always true and the check always failed.

## Cause

In `Check-ActivityDerived` (`harness/scripts/verify-artifacts.ps1`), the color is captured from
the activity line with the regex `<<#([0-9A-Fa-f]{6})>>`, so the capture group is the bare hex
`90EE90` (the `#` is outside the group). The expected value came from `Get-BoardStatusColor`,
which returns the hex **with** a leading `#` (e.g. `#90EE90`). The two were compared directly
with `-ne`, so the `#` prefix made every comparison unequal.

## Solution / workaround

Normalize both sides before comparing: strip an optional leading `#` and compare
case-insensitively. In `Check-ActivityDerived`:

```powershell
$expected = Get-BoardStatusColor $row.status
$expectedHex = ([string]$expected).TrimStart('#').ToUpperInvariant()
...
$m = [regex]::Match($line, '<<#([0-9A-Fa-f]{6})>>')
...
if ($m.Groups[1].Value.ToUpperInvariant() -ne $expectedHex) { ... }
```

The captured hex already has no `#`, so only the expected value needs normalizing. No expected
colors or board data were changed. The comparison is now fair and the failure no longer masks
real board/activity mismatches.

## Verification

- `verify-artifacts.ps1 -SelfTest`: `SELF-TEST PASSED` (34 cases), including `hash-match changed:
  FAIL` and `hash-match skips harness-managed: NOT_APPLICABLE`.
- `stage-done.ps1 -Task T-028 -Checkpoint CP-F -Force`: `final-activity` now `PASS`; CP-F result
  `3 PASS, 1 MANUAL, 0 UNCHECKED` -> `awaiting_review` (the remaining `MANUAL` is
  `final-traceability-manual`, which always waits for the user).

## Prevention / follow-up

When adding or changing `Get-BoardStatusColor` or the activity color format, keep the
`activity-derived` comparison format-agnostic (normalize both sides). Consider a dedicated
self-test case for `activity-derived` color equality to catch this class of prefix mismatch
automatically (not added here to keep the change minimal and avoid editing the harness further
without a lead request).

## Links

T-028 F-01 / CP-F; `harness/scripts/verify-artifacts.ps1` (Check-ActivityDerived);
`docs/checkpoints/T-028-CP-F.checks.json`.

## Timeline

- 2026-11-18T23:19:06+03:00: detected during T-028 CP-F (`final-activity` FAIL, uniform across T-001..T-027).
- 2026-11-18T23:19:06+03:00: root-caused to the `#` prefix mismatch; fixed by normalizing the expected color; self-test passed; CP-F re-run now reports `final-activity` PASS.
