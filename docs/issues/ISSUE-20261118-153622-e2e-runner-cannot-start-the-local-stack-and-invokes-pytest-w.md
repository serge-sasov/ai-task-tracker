# ISSUE-20261118-153622: E2E runner cannot start the local stack and invokes pytest with an invalid module path

| Field | Value |
|---|---|
| id | ISSUE-20261118-153622 |
| status | resolved |
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

Fixed in `scripts/e2e.ps1`: `Get-ComposeCommand` probes `docker compose version` and falls back to standalone `docker-compose`, and `Invoke-Run` passes the pytest target as forward-slash `tests/e2e`. Both original root causes (no compose-form detection and the backslash module path) are addressed.

## Solution / workaround

Implemented in `scripts/e2e.ps1` (T-037/T-039): `Get-ComposeCommand` detects the available Compose form (`docker compose` plugin or standalone `docker-compose`), and `Invoke-Run` passes `tests/e2e` with a forward slash to pytest. Live runs on 2026-11-22 completed against the isolated local stack: T-039 S-04 7/7 PASS and S-01 regression 24/24 PASS.

## Verification

- 2026-11-22: T-039 live e2e against the built frontend: S-04 7/7 PASS, S-01 regression 24/24 PASS, unit 8/8 PASS (`docs/qa/e2e-report-s04-model.md`).
- T-037 `-Wait` run: 31 passed with the seeded stack (see ISSUE-20261122-022225).
- `docs/checkpoints/T-039-CP-2.checks.json` and `T-039-CP-F.checks.json` are green.

## Prevention / follow-up

Implemented: compose-form detection is permanent in `Get-ComposeCommand`; the runner uses a forward-slash pytest target. The foreground/detached argument divergence is covered by ISSUE-20261122-022225 (`-DryRun` plus a `-m pytest` invariant).

## Links

T-025 (I-02); tests/e2e/uc_e2e_001_003/shared.py; tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py; tests/e2e/results/UC-E2E-003.json; base_commit 240a2fc.

## Timeline

- 2026-11-18T15:36:22.2525251+03:00: detected
- 2026-11-22T18:48:30+03:00: resolved - compose detection and forward-slash target verified by T-037/T-039 live runs
