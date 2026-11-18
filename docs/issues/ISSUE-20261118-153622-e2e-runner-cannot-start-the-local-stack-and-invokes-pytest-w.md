# ISSUE-20261118-153622: E2E runner cannot start the local stack and invokes pytest with an invalid module path

| Field | Value |
|---|---|
| id | ISSUE-20261118-153622 |
| status | workaround |
| severity | major |
| type | e2e |
| detected_at | 2026-11-18T15:36:22.2525251+03:00 |
| source | T-025 |
| role | harness-coder |
| location | scripts/e2e.ps1 + environment (Docker CLI compose plugin, pytest module path) |
| environment | OS: Windows (win32); cwd: C:\study\ai\research\ai-task-tracker; Docker CLI without the compose plugin; pytest (Python 3). |

## Symptoms

Two failures in scripts/e2e.ps1 run -UC UC-E2E-003 (and the runner in general) block a live e2e run. (a) Starting the stack fails: the local Docker CLI reports unknown docker command: compose (the Compose binary present is not the expected docker compose plugin). (b) The runner invokes python -m pytest with a backslash path (tests\e2e), which fails with: No module named tests\e2e. A direct forward-slash invocation (python -m pytest tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py) collects and runs: offline 1 passed, 23 skipped.

## Reproduction

1. From the repo root run the approved runner: `.\scripts\e2e.ps1 run -UC UC-E2E-003`.
2. Observe (a): the stack start step fails with `unknown docker command: "compose"` because the local Docker CLI does not expose the `docker compose` plugin.
3. Observe (b): the runner's pytest step fails with `No module named tests\e2e` (backslash module path).
4. Workaround that works: `python -m pytest tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py -v` -> 1 passed, 23 skipped (offline).

## Expected / actual behavior

- Expected: `scripts\e2e.ps1 run -UC UC-E2E-003` starts the local stack and runs the UC-E2E-003 pytest suite, writing `tests/e2e/results/UC-E2E-003.json`.
- Actual: the runner cannot start the stack (no `docker compose` plugin) and invokes pytest with an invalid backslash module path, so the live run cannot complete; only a manual direct pytest invocation collects and skips cleanly.

## Cause

Not fixed. Two independent problems. (1) Environment: the Docker CLI does not expose the docker compose subcommand as a plugin, so the runner docker compose up step fails. (2) Runner: the pytest target is passed with a Windows backslash path, which Python reads as an escaped module name and raises No module named tests\e2e. The runner should pass pytest a forward-slash module or directory path.

## Solution / workaround

Workaround (no change to scripts/e2e.ps1 or the test __init__ files in T-025): run the use-case tests directly with a forward-slash path: python -m pytest tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py -v. Offline this yields 1 passed (the aggregation-math test) and 23 skipped (stack-dependent tests skip cleanly). A live run still requires fixing the Docker Compose plugin and the runner pytest path; root cause not fixed in this task.

## Verification

Direct pytest collected and ran the UC-E2E-003 suite: test_models_by_task_aggregation_math_offline passed; the five live test_list_models_field_order_and_empty_state cases (TASK-001..005) and other list/detail tests were skipped with the reason local stack unhealthy (live E2E requires docker compose up). python -m py_compile passed for both changed files; the YAML validator reported 0 errors.

## Prevention / follow-up

Follow-up task (not created in T-025): fix scripts/e2e.ps1 to (1) detect the available Compose form (docker compose plugin vs standalone docker-compose) and (2) pass pytest a forward-slash module or directory path. Record the Compose-plugin environment requirement in the e2e runbook. Q-02 live e2e may be blocked until both are resolved.

## Links

T-025 (I-02); tests/e2e/uc_e2e_001_003/shared.py; tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py; tests/e2e/results/UC-E2E-003.json; base_commit 240a2fc.

## Timeline

- 2026-11-18T15:36:22.2525251+03:00: detected
