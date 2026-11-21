# ISSUE-20261121-224809: T-037 blocking command waits and silent e2e health loops

| Field | Value |
|---|---|
| id | ISSUE-20261121-224809 |
| status | resolved |
| severity | major |
| type | process |
| detected_at | 2026-11-21T22:48:09.5973010+03:00 |
| source | T-037 / phase 3 I-02 |
| role | harness-coder |
| location | scripts/e2e.ps1; harness/process.md 11.4; harness/scripts/run-monitored.ps1 |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

T-037 C1 stayed in_progress with zero produced artifacts for about six hours. The session had 345 bash calls, 17 above 60 s, 11 above 300 s, 9 above 600 s, 160.9 min total in long calls and a 38.6 min maximum. e2e.ps1 looped for 15 minutes on a dead health check, Start-Process -Wait blocked for 10 minutes, and Out-File/Tee-Object left empty logs.

## Reproduction

1. Run `.\scripts\e2e.ps1 up` with the backend health endpoint down; observe the 15-minute health loop and the `WARN` continuation.
2. Run `. .\scripts\e2e.ps1 run -UC UC-E2E-005` or `Start-Process ... -Wait`; observe the tool call blocking until timeout and the empty redirected log.
3. Run `.\scripts\e2e.ps1 run` after a failed stack start; observe pytest starting anyway and the long module-level stack probes.

## Expected / actual behavior

- Expected: a failed start or health probe returns an exit code and diagnostics within the bounded wait; a long run is detached and monitored; logs grow incrementally.
- Actual: the health loop swallowed failures for 15 minutes, pytest ran after stack failure, `Start-Process -Wait` blocked, and `Out-File`/`Tee-Object` hid progress.

## Cause

No command wall-clock rule; long jobs ran in the foreground; e2e.ps1 swallowed stack-start failures and continued to pytest; the health wait was 15 minutes with a silent catch; tool timeout did not kill the process tree; timeout escalation replaced fail-fast.

## Solution / workaround

Added harness/scripts/run-monitored.ps1 and run-monitored-child.ps1; process.md 11.4 and 13 define foreground limits, detached monitoring, no blocking patterns, first-result checks and two-attempt blocking; e2e.ps1 now detects Compose, fails fast, has a bounded health wait and detached run/status/abort; conftest uses a 2 s stack probe.

## Verification

run-monitored.ps1 -SelfTest passes (fast exit 7, 60 s sleeper, bounded wait 124, stop kills the target). e2e.ps1 list parses 8 UCs; status reports no detached run cleanly. T-036/T-035 had zero long calls; the T-037 rule text now requires the guard.

## Prevention / follow-up

Use the wrapper and status polling for every long command; keep the 60/120 s foreground limits; do not add unconditional down -v or 15-minute health loops; doctor checks the wrapper and lead allow pattern.

## Links

docs/tasks/T-037-e2e-s04-model.md; docs/chunks/T-037/C1.brief.md; docs/plans/harness-command-guard.md; scripts/e2e.ps1; tests/e2e/conftest.py

## Timeline

- 2026-11-21T22:48:09.5973010+03:00: detected
