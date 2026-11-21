# Harness command execution guard

Date: 2026-11-21
Status: implemented (B + A); e2e runner hardening included
Issue: ISSUE-20261121-224809
Owner: harness maintenance

## Problem

Phase 3, T-037 (S-04 / I-02), blocked on commands instead of reacting to their
result. Session evidence from the opencode database:

- 345 bash calls; 17 longer than 60 s, 11 longer than 300 s, 9 longer than
  600 s; 2 h 41 min total in long calls; maximum 38.6 min.
- `. .\scripts\e2e.ps1 > $null 2>&1` waited 38.6 min; `e2e.ps1 run` waited
  15.4 and 15.0 min; `e2e.ps1 up` waited 8.3 and 10 min; `Start-Process -Wait`
  waited 10 min.
- `run005.txt`, `up_log.txt`, and `out_run3.txt` stayed empty (11-18 bytes)
  because `Out-File`/`Tee-Object` buffered the output until the pipeline ended.
- T-036 and T-035 had zero bash calls above 60 s. The failure mode is specific
  to the I-02 runner and to foreground/blocking command patterns.

## Decision

Adopt variant B + A:

- B: a monitored-run wrapper plus fail-fast, detached e2e execution.
- A: mandatory process rules for command execution in `harness/process.md` 11.4,
  the role agents, the skill, and the card templates.

T-037 keeps its scope and ledger; its card and brief were updated with the new
command rule so a resume uses the guard.

## Implementation

### Monitored-run wrapper

`harness/scripts/run-monitored.ps1` actions:

- `start` launches a hidden host and returns in under 2 s with the host PID,
  log path, and status path;
- `host` owns the child runner, writes timestamped stdout/stderr and a
  heartbeat, and enforces the job timeout;
- `status` reports state, target PID, elapsed, log size, exit code, and the
  last line;
- `wait -WaitSec 30` is bounded and returns 124 on timeout;
- `stop` kills the process tree and records `stopped`;
- `tail` shows the last N lines; `list` shows recorded runs;
- `-SelfTest` covers fast failure, a sleeper, bounded wait, and tree stop.

`harness/scripts/run-monitored-child.ps1` runs the target with argument arrays
and writes the target exit code to an exit file, avoiding the PowerShell
`Start-Process -PassThru` null `ExitCode` issue.

State and logs live under `harness/run-state/` (gitignored).

### Process rules

`harness/process.md` 11.4 and 13 require:

- foreground commands <= 60 s; bash timeout <= 120 s;
- long jobs through `run-monitored.ps1` and polling;
- no `Start-Process -Wait`, `Out-File`, `Tee-Object`, or dot-sourced dispatcher
  without `-NoExit`;
- react to the first exit code/log tail; no blind retry after two failures;
- report command, PID, log, status, exit code, elapsed, and the last 20 lines.

### E2E runner

`scripts/e2e.ps1`:

- detects `docker compose` or `docker-compose`;
- fails fast when Docker/Compose is unavailable;
- has a bounded backend + frontend health wait (default 90 s) and prints
  `docker compose ps` plus service logs on failure;
- no longer runs `down -v` on every `up`; `-Reset` is explicit; `-Build`/`-Force`
  rebuilds images;
- `run` starts pytest detached by default through `run-monitored.ps1`;
- `status` and `abort` are the monitoring commands; `-Wait` is the explicit
  foreground mode for CI/QA short checks.

`tests/e2e/conftest.py` uses a 2 s health probe and 90 s stack health bound;
`E2E_KEEP_STACK=1` keeps the stack between iterative runs. The runner README
documents the detached workflow. The doctor's Ollama probe no longer aborts on
native stderr, so the new checks are reported.

## Acceptance

- immediate failure is visible in under 5 s;
- `start` returns in under 2 s; `wait` is bounded; `stop` kills the tree;
- a T-037-like run has no foreground call above 60 s and no call above 300 s;
- `run-monitored.ps1 -SelfTest`, `doctor.ps1`, and the verification engine
  self-test pass;
- T-037 artifacts are not rewritten; only its rule text changes.

## Rollout

- Project harness source: `harness/process.md`, agents, skill, templates,
  scripts, `install.ps1`, `init.ps1`, `doctor.ps1`, `version.json`.
- Global agents (`~/.config/opencode/agents/harness-{coder,qa,md,orchestrator}.md`)
  received the command rule and the `run-monitored.ps1` allow pattern; the
  wrapper and child runner were copied to the global harness scripts.
- The global harness version is 3.2.0. A new opencode session/restart is needed
  for agent prompt changes to take effect.
