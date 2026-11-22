# ISSUE-20261122-022225: e2e.ps1 detached run path invokes python without -m pytest so tests/e2e has no __main__

| Field | Value |
|---|---|
| id | ISSUE-20261122-022225 |
| status | resolved |
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

Fixed. `Invoke-Run` now builds `$detachedArgs = @('-m','pytest') + $pytestArgs`, asserts the detached args start with `-m pytest`, and passes `$detachedArgs` to `run-monitored.ps1 start`. A `-DryRun` switch prints both command lines without starting the stack. During verification, `--uc` was found to filter only the results JSON, not test execution; `tests/e2e/conftest.py` now has a `pytest_collection_modifyitems` hook that deselects tests outside the requested UC ids (37 deselected for UC-E2E-003).

## Solution / workaround

Implemented in `scripts/e2e.ps1`: the detached branch uses `-m pytest` explicitly, and `-DryRun` prints the exact foreground/detached lines (with an invariant check). `tests/e2e/conftest.py` applies `--uc` at collection time so a focused run does not execute every use case.

## Verification

- `.\scripts\e2e.ps1 run -DryRun -UC UC-E2E-003` prints both lines starting with `python -m pytest tests/e2e ...`.
- `harness/run-state/e2e-run/target.json` records arguments `["-m","pytest","tests/e2e",...]` for the detached child.
- Detached run against the isolated local stack: `24 passed, 37 deselected in 58.68s`, `state=exited`, `exit_code=0`; log has no `__main__` error.
- `-Wait` path remains `& $PY -m pytest $pytestArgs` (unchanged).

## Prevention / follow-up

Implemented: the `-DryRun` invariant prevents the foreground/detached argument divergence, and the `--uc` collection hook keeps focused verification honest. The detached status/log is checked via `.\scripts\e2e.ps1 status`.

## Links

T-037 (chunk C2); docs/tasks/T-037-e2e-s04-model.md; scripts/e2e.ps1 (Invoke-Run); tests/e2e/conftest.py (`--uc` hook); tests/e2e/results/e2e-run.log; related issue ISSUE-20261118-153622.

## Timeline

- 2026-11-22T02:22:25.6948061+03:00: detected
- 2026-11-22T18:48:30+03:00: fixed detached args and added `-DryRun`; found `--uc` only filtered results
- 2026-11-22T19:12:13+03:00: resolved - detached UC-E2E-003 run passed 24/24 with 37 deselected, exit 0
