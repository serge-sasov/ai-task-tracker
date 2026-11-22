# -*- coding: utf-8 -*-
"""E2E runner engine for ai-task-tracker (T-009, I-02).

This module is a pytest plugin (``tests/e2e/conftest.py``). It:

* loads the machine-readable dataset ``docs/analysis/e2e-test-data-*.yaml``
  (schema ``harness-e2e/1``);
* gates the isolated local stack (health polling helpers used by the UC test
  modules; the stack lifecycle is driven by ``scripts/e2e.ps1``);
* aggregates the REAL pytest outcomes of the ``test_uc_e2e_NNN_*`` tests into
  one machine-readable result per ``UC-E2E-NNN`` and writes them, plus an
  aggregate, into ``--results-dir`` on ``pytest_sessionfinish``.

The previous ``pytest_collect_directory``-based per-UC collector was inert by
construction (a directory's own conftest is not consulted when pytest collects
that directory, so it could never fire). Results are therefore derived from the
actual test reports instead of a synthetic item per use case.

Standard library only (urllib, json, subprocess, ...). No third-party deps, no
secrets, no external networks. All traffic is to 127.0.0.1 / localhost.
"""

from __future__ import annotations

import datetime as _dt
import json
import os
import re
import subprocess
import time
from collections import Counter, defaultdict
from pathlib import Path

import pytest
import yaml

# --------------------------------------------------------------------------- #
# Paths and constants
# --------------------------------------------------------------------------- #

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_DATASET = ROOT.parent / "docs" / "analysis" / "e2e-test-data-ai-task-tracker.yaml"
RESULTS_DIR = Path(__file__).resolve().parent / "results"
COMPOSE_FILE = ROOT.parent / "docker-compose.yml"

DEFAULT_BASE_URL = os.environ.get("E2E_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
DEFAULT_OLLAMA_URL = os.environ.get("E2E_OLLAMA_URL", "http://127.0.0.1:11434").rstrip("/")
HEALTH_PATH = "/api/health"
HEALTH_TIMEOUT_SECONDS = int(os.environ.get("E2E_HEALTH_TIMEOUT", "10"))

STACK_HEALTH_TIMEOUT_SECONDS = int(os.environ.get("E2E_STACK_TIMEOUT", "90"))
STACK_HEALTH_POLL_SECONDS = 2


# --------------------------------------------------------------------------- #
# Small stdlib HTTP helpers (no third-party deps)
# --------------------------------------------------------------------------- #

def _http_request(method, url, timeout=30.0):
    """Return (status, text_body). urllib only; never raises on HTTP/socket errors."""
    import urllib.request
    import urllib.error

    req = urllib.request.Request(url, method=method, headers={"Accept": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, resp.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as exc:
        try:
            body = exc.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        return exc.code, body
    except (urllib.error.URLError, OSError, ValueError):
        return 0, ""


def json_request(path_or_url, timeout=HEALTH_TIMEOUT_SECONDS):
    """GET a base-relative path or full URL; return (status, parsed_json|{' _raw'})."""
    url = path_or_url if str(path_or_url).startswith("http") else f"{DEFAULT_BASE_URL}{path_or_url}"
    status, body = _http_request("GET", url, timeout=timeout)
    try:
        return status, (json.loads(body) if body.strip() else {})
    except Exception:
        return status, {"_raw": body[:400]}


def _load_dataset(dataset_path):
    data = yaml.safe_load(Path(dataset_path).read_text(encoding="utf-8-sig"))
    if not isinstance(data, dict):
        raise ValueError("dataset top-level must be a mapping")
    if data.get("schema") != "harness-e2e/1":
        raise ValueError(f"unsupported schema: {data.get('schema')!r}")
    return data


# --------------------------------------------------------------------------- #
# Stack lifecycle (isolated local stack only)
# --------------------------------------------------------------------------- #

def _docker(args, timeout=60):
    try:
        proc = subprocess.run(["docker"] + args, capture_output=True, text=True, timeout=timeout)
    except subprocess.TimeoutExpired:
        return False
    except FileNotFoundError:
        return False
    return proc.returncode == 0


def stack_is_up(base_url):
    try:
        status, _ = _http_request("GET", f"{base_url}{HEALTH_PATH}", timeout=2)
    except Exception:
        return False
    return status == 200


def start_stack(base_url):
    """Clean slate then bring the local stack up; poll health."""
    if not _docker(["info", "--format", "on"], timeout=15):
        raise RuntimeError("Docker is not available or daemon unreachable")
    _docker(["compose", "-f", str(COMPOSE_FILE), "down", "-v"])
    if not _docker(["compose", "-f", str(COMPOSE_FILE), "up", "-d"]):
        raise RuntimeError("docker compose up failed")
    deadline = time.monotonic() + STACK_HEALTH_TIMEOUT_SECONDS
    while time.monotonic() < deadline:
        if stack_is_up(base_url):
            return True
        time.sleep(STACK_HEALTH_POLL_SECONDS)
    return False


def _teardown_stack():
    _docker(["compose", "-f", str(COMPOSE_FILE), "down", "-v"], timeout=120)


# --------------------------------------------------------------------------- #
# Per-UC result aggregation (pytest report hook)
# --------------------------------------------------------------------------- #

UC_ID_RE = re.compile(r"test_uc_e2e_(\d{3})_")
_UC_REPORTS = defaultdict(list)  # uc_id -> list of per-test rows


def uc_id_from_nodeid(nodeid):
    """Map a test nodeid to its UC id via the ``test_uc_e2e_NNN_`` basename."""
    path = str(nodeid).split("::", 1)[0]
    match = UC_ID_RE.search(Path(path).name)
    return "UC-E2E-%s" % match.group(1) if match else None


def _short_repr(report):
    text = getattr(report, "longreprtext", "") or str(getattr(report, "longrepr", "") or "")
    text = text.strip()
    if not text:
        return ""
    lines = [line for line in text.splitlines() if line.strip()]
    return (lines[-1] if lines else text)[:400]


def pytest_runtest_logreport(report):
    """Accumulate setup/call/teardown reports into per-UC test rows."""
    uid = uc_id_from_nodeid(report.nodeid)
    if not uid:
        return
    rows = _UC_REPORTS[uid]
    row = next((r for r in rows if r["nodeid"] == report.nodeid), None)
    if row is None:
        row = {"nodeid": report.nodeid, "status": "skipped", "duration_ms": 0, "detail": ""}
        rows.append(row)

    row["duration_ms"] += int((getattr(report, "duration", 0) or 0) * 1000)
    outcome = getattr(report, "outcome", "")
    when = getattr(report, "when", "")

    if when == "setup":
        if outcome == "failed":
            row["status"] = "error"
            row["detail"] = _short_repr(report)
        elif outcome == "skipped":
            row["status"] = "skipped"
            row["detail"] = _short_repr(report)
    elif when == "call":
        if outcome == "passed":
            row["status"] = "passed"
        elif outcome == "failed":
            row["status"] = "failed"
            row["detail"] = _short_repr(report)
        elif outcome == "skipped":
            row["status"] = "skipped"
            row["detail"] = _short_repr(report)
    elif when == "teardown" and outcome == "failed":
        row["status"] = "error"
        row["detail"] = _short_repr(report)


def _uc_status(rows):
    statuses = [r["status"] for r in rows]
    if not statuses:
        return "not_run"
    if "error" in statuses:
        return "error"
    if "failed" in statuses:
        return "fail"
    if all(s == "skipped" for s in statuses):
        return "skipped"
    return "pass"


def _uc_counts(rows):
    counts = Counter(r["status"] for r in rows)
    return {
        "total": len(rows),
        "pass": counts.get("passed", 0),
        "fail": counts.get("failed", 0),
        "error": counts.get("error", 0),
        "skipped": counts.get("skipped", 0),
    }


def _dataset_use_cases(session):
    dataset_path = Path(str(session.config.getoption("--dataset")))
    try:
        data = _load_dataset(dataset_path)
    except Exception:
        return [], dataset_path
    return [uc for uc in data.get("use_cases", []) if isinstance(uc, dict)], dataset_path


def build_results(session):
    """Build one result dict per UC from the collected test reports."""
    use_cases, _ = _dataset_use_cases(session)
    wanted = session.config.getoption("--uc") or []
    results = []
    for uc in use_cases:
        uid = uc.get("id")
        if not uid or (wanted and uid not in wanted):
            continue
        rows = list(_UC_REPORTS.get(uid, []))
        results.append({
            "uc_id": uid,
            "title": uc.get("title"),
            "actor": uc.get("actor"),
            "priority": uc.get("priority"),
            "source_requirements": uc.get("source_requirements"),
            "expected_postcondition": uc.get("expected_postcondition"),
            "status": _uc_status(rows),
            "counts": _uc_counts(rows),
            "tests": rows,
            "duration_ms": sum(r["duration_ms"] for r in rows),
            "errors": [r["detail"] for r in rows
                       if r["status"] in ("failed", "error") and r["detail"]],
            "timestamp": _dt.datetime.now(_dt.timezone.utc).isoformat(),
            "base_url": DEFAULT_BASE_URL,
        })
    if not results and _UC_REPORTS:
        for uid in sorted(_UC_REPORTS):
            if wanted and uid not in wanted:
                continue
            rows = list(_UC_REPORTS[uid])
            results.append({
                "uc_id": uid, "title": None, "actor": None, "priority": None,
                "source_requirements": None, "expected_postcondition": None,
                "status": _uc_status(rows), "counts": _uc_counts(rows), "tests": rows,
                "duration_ms": sum(r["duration_ms"] for r in rows),
                "errors": [r["detail"] for r in rows
                           if r["status"] in ("failed", "error") and r["detail"]],
                "timestamp": _dt.datetime.now(_dt.timezone.utc).isoformat(),
                "base_url": DEFAULT_BASE_URL,
            })
    results.sort(key=lambda r: r["uc_id"] or "")
    return results


def _summary(results):
    counts = Counter(r["status"] for r in results)
    return {
        "total": len(results),
        "pass": counts.get("pass", 0),
        "fail": counts.get("fail", 0),
        "error": counts.get("error", 0),
        "skipped": counts.get("skipped", 0),
        "not_run": counts.get("not_run", 0),
    }


def write_all_results(session):
    """Write one JSON per UC plus the aggregate into --results-dir."""
    results_dir = Path(str(session.config.getoption("--results-dir")))
    _, dataset_path = _dataset_use_cases(session)
    results = build_results(session)
    results_dir.mkdir(parents=True, exist_ok=True)

    payload = {
        "scope": "ai-task-tracker",
        "dataset": dataset_path.name,
        "generated_at": _dt.datetime.now(_dt.timezone.utc).isoformat(),
        "results": results,
        "summary": _summary(results),
    }
    out = results_dir / "e2e-results.json"
    out.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    for res in results:
        safe = re.sub(r"[^A-Za-z0-9._-]", "-", res["uc_id"] or "unknown")
        (results_dir / f"{safe}.json").write_text(
            json.dumps(res, ensure_ascii=False, indent=2), encoding="utf-8")
    return out


# --------------------------------------------------------------------------- #
# pytest options / hooks / fixtures
# --------------------------------------------------------------------------- #

def pytest_addoption(parser):
    parser.addoption("--dataset", action="store", default=str(DEFAULT_DATASET),
                     help="Path to the harness-e2e/1 YAML dataset.")
    parser.addoption("--results-dir", action="store", default=str(RESULTS_DIR),
                     help="Directory for machine-readable JSON results.")
    parser.addoption("--uc", action="append", default=None,
                     help="Restrict to a UC id (repeatable). Default: all.")


def pytest_collection_modifyitems(config, items):
    """Apply --uc as an execution filter, not only a results filter."""
    wanted = config.getoption("--uc") or []
    if not wanted:
        return
    wanted_nums = {str(uc).upper().split("-")[-1] for uc in wanted}
    kept, deselected = [], []
    for item in items:
        match = re.search(r"test_uc_e2e_(\d{3})_", item.nodeid)
        if match and match.group(1) in wanted_nums:
            kept.append(item)
        else:
            deselected.append(item)
    if deselected:
        config.hook.pytest_deselected(items=deselected)
        items[:] = kept


@pytest.fixture(scope="session")
def e2e_env():
    """Expose base URLs and timeouts to tests (optional convenience fixture)."""
    return {
        "base_url": DEFAULT_BASE_URL,
        "ollama_url": DEFAULT_OLLAMA_URL,
        "health_path": HEALTH_PATH,
        "timeout_seconds": HEALTH_TIMEOUT_SECONDS,
    }


def pytest_sessionfinish(session, exitstatus):
    """Write per-UC JSON results and tear the stack down (never on collect-only)."""
    if getattr(session.config.option, "collectonly", False):
        return
    try:
        write_all_results(session)
    finally:
        if os.environ.get("E2E_KEEP_STACK") != "1":
            _teardown_stack()
        _UC_REPORTS.clear()


# --------------------------------------------------------------------------- #
# Non-pytest helpers used by scripts/e2e.ps1
# --------------------------------------------------------------------------- #

def list_use_cases(dataset_path=None):
    """Return (id, title, priority) rows for the ``list`` runner subcommand."""
    path = Path(dataset_path) if dataset_path else DEFAULT_DATASET
    if not path.exists():
        raise FileNotFoundError(f"dataset not found: {path}")
    data = _load_dataset(path)
    return [
        {"id": uc["id"], "title": uc.get("title"), "priority": uc.get("priority")}
        for uc in data.get("use_cases", []) if isinstance(uc, dict)
    ]
