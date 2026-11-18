# -*- coding: utf-8 -*-
"""Shared helpers for UC-E2E-001..003 (T-009, I-02).

Reusable for the suite: the deterministic fixture loader from the
``harness-e2e/1`` dataset, a stdlib JSON-path accessor, duration parsing, and
test-data-driven parametrize helpers. All assertions here are against the
documented API contract (health, list, detail) / dataset, not implementation.

Stdlib only. No secrets, no external networks — traffic is to 127.0.0.1.
"""

from __future__ import annotations

import json
import os
import re
import urllib.error
import urllib.request
from collections.abc import Iterator
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent.parent
DATASET = ROOT / "docs" / "analysis" / "e2e-test-data-ai-task-tracker.yaml"
ENVIRON = os.environ  # E2E_BASE_URL etc.
BASE_URL = os.environ.get("E2E_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
HEALTH_PATH = "/api/health"
HEALTH_TIMEOUT_SECONDS = 30


# --------------------------------------------------------------------------- #
# Dataset loading (single source of data values)
# --------------------------------------------------------------------------- #

def _load_dataset() -> dict:
    try:
        import yaml  # local import keeps collection import cheap
    except ImportError as exc:  # pragma: no cover - pyyaml always present
        raise RuntimeError("pyyaml required for E2E dataset") from exc
    text = DATASET.read_text(encoding="utf-8-sig")
    data = yaml.safe_load(text)
    if not isinstance(data, dict):
        raise ValueError("dataset top-level must be a mapping")
    if data.get("schema") != "harness-e2e/1":
        raise ValueError(f"unsupported schema: {data.get('schema')!r}")
    return data


def dataset() -> dict:
    return _load_dataset()


def sessions(data: dict | None = None) -> list:
    return (data or dataset())["fixtures"]["sessions"]


# --------------------------------------------------------------------------- #
# JSON path accessor (subset used by the contract tests)
# --------------------------------------------------------------------------- #

def json_path(root, path) -> object:
    if not path.startswith("$."):
        path = "$." + path
    cur = root
    for seg in path[2:].split("."):
        if seg == "":
            continue
        cur = cur[int(seg)] if isinstance(cur, list) else cur[seg]
    return cur


# --------------------------------------------------------------------------- #
# HTTP + assertions
# --------------------------------------------------------------------------- #

def _request(method: str, url: str, timeout: float = HEALTH_TIMEOUT_SECONDS):
    req = urllib.request.Request(url, method=method,
                                 headers={"Accept": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, resp.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as exc:
        try:
            body = exc.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        return exc.code, body
    except (urllib.error.URLError, OSError):
        return 0, ""


def json_request(path_or_url: str, timeout: float = HEALTH_TIMEOUT_SECONDS):
    """GET from either a base-relative path (``/api/tasks``) or a full URL."""
    url = path_or_url if path_or_url.startswith("http") else f"{BASE_URL}{path_or_url}"
    status, body = _request("GET", url, timeout=timeout)
    try:
        return status, json.loads(body) if body.strip() else {}
    except Exception:
        return status, {"_raw": body[:400]}


def assert_json_path(root, path, expected, where: str = ""):
    got = json_path(root, path)
    msg = f"[UC-E2E] {where} {path} = {got!r} (want {expected!r})"
    assert got == expected, msg


# --------------------------------------------------------------------------- #
# Duration parsing (RU locale from the dataset: "2ч 30мин")
# --------------------------------------------------------------------------- #

HOUR_MINUTES = {"ч": 60}
UNIT_MINUTES = {"ч": 60, "мин": 1, "мин.": 1}


def parse_duration_ru(value: str) -> int:
    """Convert a RU duration like ``"2ч 30мин"`` to whole minutes."""
    parts = re.findall(r"(\d+)\s*([чмин]+)", value)
    total = 0
    for amount, unit in parts:
        total += int(amount) * UNIT_MINUTES.get(unit.strip(), 0)
    if not parts:
        raise ValueError(f"cannot parse RU duration: {value!r}")
    return total


# --------------------------------------------------------------------------- #
# Deterministic params (single source: the dataset)
# --------------------------------------------------------------------------- #

def tasks() -> list:
    return [(s["task_id"], s["title"]) for s in sessions()]


def tasks_with_expected_status() -> list:
    return [
        (s["task_id"], s["final_status"]) for s in sessions()
    ]


def task_ids() -> list:
    return [s["task_id"] for s in sessions()]


def task_titles() -> list:
    return [s["title"] for s in sessions()]


def task_solutions() -> list:
    return [(s["task_id"], s["solutions"]) for s in sessions()]


def iter_solutions() -> Iterator[tuple]:
    """(task_id, proposed_list, chosen_value) rows, in dataset order."""
    for s in sessions():
        sol = s.get("solutions") or {}
        yield s["task_id"], list(sol.get("proposed") or []), sol.get("chosen")


def durations_by_task() -> dict:
    return {s["task_id"]: parse_duration_ru(s["duration"]) for s in sessions()}


def models_by_task() -> dict:
    """Expected ``models`` order per task from ``fixtures.sessions`` usage.

    Covers UC-E2E-003 step S8 / contract §4.2 ``models``: aggregate each
    session's ``usage`` rows by model, total = input + output (+ ``cache`` /
    ``cache_tokens`` when present; the current dataset has no cache key), order
    by total descending then model name ascending, return model names. A
    session with ``usage: null`` or no rows yields ``[]``. Stdlib only.
    """

    result: dict[str, list[str]] = {}
    for s in sessions():
        usage = s.get("usage") or []
        if not usage:
            result[s["task_id"]] = []
            continue
        totals: dict[str, int] = {}
        for row in usage:
            model = row["model"]
            input_tok = row.get("input_tokens", row.get("input")) or 0
            output_tok = row.get("output_tokens", row.get("output")) or 0
            cache = row.get("cache_tokens") or row.get("cache") or 0
            total = input_tok + output_tok + cache
            totals[model] = totals.get(model, 0) + total
        result[s["task_id"]] = [
            model for model, _ in sorted(totals.items(), key=lambda kv: (-kv[1], kv[0]))
        ]
    return result
