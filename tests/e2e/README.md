# E2E runner — ai-task-tracker (T-009, stage I-02)

Deterministic end-to-end tests generated from the machine-readable dataset
`docs/analysis/e2e-test-data-ai-task-tracker.yaml` (schema `harness-e2e/1`).
Tests run against an **isolated local stack only** (`127.0.0.1` / `localhost`),
never production.

## Requirements

- Windows PowerShell 5.1+
- Python 3.12+ with `PyYAML` and `pytest` installed (no other deps; `urllib`
  only — `requests` is intentionally not used)
- Docker Desktop running (local stack)

## Run

From the repo root:

```powershell
# List the use cases from the YAML (no stack required)
.\scripts\e2e.ps1 list

# Bring up the isolated local stack (clean slate, rebuild optional)
.\scripts\e2e.ps1 up

# Run the full suite against the local stack, results -> tests/e2e/results/
.\scripts\e2e.ps1 run

# Or: list + run in one command
.\scripts\e2e.ps1 all

# Restrict to one or more use cases
.\scripts\e2e.ps1 run -UC UC-E2E-001 -UC UC-E2E-002

# Tear the local stack down
.\scripts\e2e.ps1 down
```

Environment variables: `E2E_BASE_URL` (default `http://127.0.0.1:8080`),
`E2E_OLLAMA_URL` (default `http://127.0.0.1:11434`),
`E2E_STACK_TIMEOUT` (default `300s`), `E2E_HEALTH_TIMEOUT` (default `30s`).

### Direct pytest

```powershell
python -m pytest tests/e2e -q --rootdir=.
python -m pytest tests/e2e --collect-only -q            # collection only; writes nothing
python -m pytest tests/e2e -q --uc UC-E2E-007           # filter
```

## How tests are generated

The use-case modules under `tests/e2e/uc_e2e_*` are pytest tests derived from
the YAML dataset. `conftest.py` is a pytest plugin that:

1. registers the `--dataset`, `--results-dir` and `--uc` options;
2. aggregates the **real pytest outcomes** per use case in
   `pytest_runtest_logreport`: the UC id is derived from the test file basename
   (`test_uc_e2e_NNN_*` -> `UC-E2E-NNN`);
3. on `pytest_sessionfinish`, writes one JSON result per UC plus the aggregate
   `e2e-results.json` into `--results-dir` (default `tests/e2e/results/`).

`--collect-only` writes nothing and never touches the local stack.

> The earlier `pytest_collect_directory` collector was removed: pytest does not
> consult a directory's own conftest when collecting that directory ("a
> directory cannot collect itself"), so it could never fire. Results are now
> derived from the actual test reports.

## Result schema

`tests/e2e/results/e2e-results.json` (aggregate):

```json
{
  "scope": "ai-task-tracker",
  "dataset": "e2e-test-data-ai-task-tracker.yaml",
  "generated_at": "2026-11-16T...Z",
  "results": [ /* one per UC */ ],
  "summary": {
    "total": 8, "pass": 0, "fail": 0, "error": 0, "skipped": 0, "not_run": 0
  }
}
```

Per-UC file (`tests/e2e/results/UC-E2E-NNN.json`):

```json
{
  "uc_id": "UC-E2E-002",
  "title": "...",
  "actor": "...",
  "priority": "P0",
  "source_requirements": ["R-001", "R-002"],
  "expected_postcondition": "...",
  "status": "pass | fail | error | skipped | not_run",
  "counts": { "total": 7, "pass": 7, "fail": 0, "error": 0, "skipped": 0 },
  "tests": [
    { "nodeid": "tests/e2e/uc_e2e_001_003/test_uc_e2e_002_ingest.py::test_...",
      "status": "passed", "duration_ms": 42, "detail": "" }
  ],
  "duration_ms": 420,
  "errors": [],
  "timestamp": "2026-11-16T...Z",
  "base_url": "http://127.0.0.1:8080"
}
```

UC status derivation: any `error` -> `error`; else any `failed` -> `fail`; else
all `skipped` -> `skipped`; else `pass`; no tests -> `not_run`.

## Isolation and cleanup

- The runner uses **only** `docker compose` against the local
  `docker-compose.yml` on `127.0.0.1`. All containers bind loopback.
- `scripts/e2e.ps1 run` brings the stack up from a clean slate
  (`docker compose down -v` then `up -d`) when the health check fails; if the
  stack cannot be started it logs a warning and still runs pytest, so results
  are recorded (stack-dependent tests skip or error honestly).
- On a real `pytest` run, `pytest_sessionfinish` tears the stack down with
  `docker compose down -v` (local volumes only). **Production is never touched.**
  `--collect-only` performs no teardown.

## Results are committed as run evidence

`tests/e2e/results/` is owned by chunk C7 ("Full run and result capture") and is
committed as the machine-readable run evidence for T-009. Do not hand-edit
result files; regenerate them by running the suite.
