# ISSUE-20261122-022225: e2e.ps1 detached run path invokes python without -m pytest so tests/e2e has no __main__

| Field | Value |
|---|---|
| id | ISSUE-20261122-022225 |
| status | open |
| severity | major |
| type | e2e |
| detected_at | 2026-11-22T02:22:25.6948061+03:00 |
| source | T-037 |
| role | harness-orchestrator |
| location | scripts/e2e.ps1:369 (detached start) vs scripts/e2e.ps1:343 (Wait path) |
| environment | OS: Windows (win32); cwd: C:\study\ai\research\ai-task-tracker; Python 3.12.10 via pyenv-win; run-monitored.ps1 detached child. |

## Symptoms

scripts\e2e.ps1 run (the default detached path) exits immediately: e2e-run.log ends with python.exe: can't find '__main__' module in '...\tests\e2e' and [exited] exit_code=1. Cause in code: the detached branch calls run-monitored.ps1 start -FilePath python -Arguments <pytestArgs> where pytestArgs is a directory path (tests/e2e -q --rootdir=. -p no:cacheprovider ...) without -m pytest, i.e. it runs python <directory> instead of python -m pytest <directory>. The -Wait foreground path works because it runs `& $PY -m pytest $pytestArgs` (line 343). Related: ISSUE-20261118-153622 (different e2e runner root causes: compose plugin, backslash module path).

## Reproduction

1. 
2. 
3. 

## Expected / actual behavior

- Expected: `.\scripts\e2e.ps1 run` (detached, default) starts pytest against `tests/e2e` and reports state via `status`/`abort`.
- Actual: the detached child exits immediately with `can't find '__main__' module in '...tests\e2e'` (exit_code=1) because the command line handed to `run-monitored.ps1` is `python tests/e2e -q --rootdir=. -p no:cacheprovider ...` instead of `python -m pytest tests/e2e ...`. The `-Wait` path, which runs `& $PY -m pytest $pytestArgs`, works.

## Cause

Detached path builds the same positional pytestArgs array (target directory first) as the Wait path, but the Wait path prepends -m pytest in the invocation while the detached path passes the raw array as python.exe arguments. python <directory> cannot resolve __main__, so the child exits 1 before pytest starts.

## Solution / workaround

Workaround: use the foreground path - .\scripts\e2e.ps1 run -Wait (verified working). Suspected fix (not applied): in scripts/e2e.ps1 Invoke-Run, build `$detachedArgs = @('-m','pytest') + $pytestArgs` and pass that to `run-monitored.ps1 start -Arguments` so the detached child runs python -m pytest tests/e2e ...

## Verification

-Wait run succeeded (31 passed for UC-E2E-005 + UC-E2E-003 with a healthy seeded stack). Detached run reproduces the failure deterministically: tests/e2e/results/e2e-run.log last lines show [start] python tests/e2e -q --rootdir=. -p no:cacheprovider, the __main__ error, and [exited] exit_code=1.

## Prevention / follow-up

Fix Invoke-Run detached branch to prepend -m pytest (see Solution). Add a cheap self-test or dry-run check that the exact command line handed to run-monitored.ps1 is executable (or at least starts with -m pytest) so the two paths cannot diverge again. Track in T-037 follow-up; do not close this issue until the detached path is verified end-to-end.

## Links

T-037 (chunk C2); docs/tasks/T-037-e2e-s04-model.md; scripts/e2e.ps1 (Invoke-Run, lines ~338-376); tests/e2e/results/e2e-run.log; related issue ISSUE-20261118-153622 (e2e runner, different root cause, status workaround).

## Timeline

- 2026-11-22T02:22:25.6948061+03:00: detected
