# -*- coding: utf-8 -*-
"""Local helpers for UC-E2E-007/008 (T-009, I-02).

Small, local-only helpers that the shared C3 helpers do not provide:

* docker compose ``restart`` against the local stack only (UC-E2E-007 S1),
* re-seeding the opencode seed directory from the deterministic fixture loader
  (UC-E2E-007 S4) and waiting one polling cycle for re-ingest,
* triggering ``DELETE /api/history`` with confirmation (UC-E2E-008 S3),

All traffic stays to 127.0.0.1 / localhost. ``docker`` and ``curl`` invocations
run only when the host supports the command (a fast pre-check), so a plain
``pytest --collect-only`` on a stack-down machine never attempts I/O.

Stdlib only. No secrets, no external networks.
"""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import urllib.error
import urllib.request
from pathlib import Path

import conftest as c
from uc_e2e_001_003 import shared

# --------------------------------------------------------------------------- #
# Paths
# --------------------------------------------------------------------------- #

ROOT = Path(__file__).resolve().parent.parent.parent.parent
DATASET = ROOT / "docs" / "analysis" / "e2e-test-data-ai-task-tracker.yaml"
COMPOSE_FILE = ROOT / "docker-compose.yml"

# Host-side opencode data dir. The contract bind-mounts it READ-ONLY into the
# backend (docker-compose.yml): the service cannot modify it (C-3), so re-seeding
# writes here and the next polling cycle re-ingests unchanged source files.
# Solved from E2E_OPPENCODE_DATA_DIR, then the compose default (./data/opencode).
def oppencode_data_dir() -> Path:
    env_val = os.environ.get("E2E_OPPENCODE_DATA_DIR")
    if env_val:
        return Path(env_val).expanduser().resolve()
    for name in ("OPENCODE_DATA_DIR",):
        if os.environ.get(name):
            return Path(os.environ[name]).expanduser().resolve()
    # docker-compose.yml default: ${OPENCODE_DATA_DIR:-./data/opencode}
    return ROOT / "data" / "opencode"


def health_url() -> str:
    return f"{c.DEFAULT_BASE_URL}{c.HEALTH_PATH}"


def _exists(cmd: str) -> bool:
    """True when the external command is on PATH (no network touch)."""
    return shutil.which(cmd) is not None


# --------------------------------------------------------------------------- #
# Re-seed: materialise the deterministic fixtures.sessions into the opencode dir
# --------------------------------------------------------------------------- #

def seed_files() -> list[Path]:
    """Yield one (path, document) per deterministic fixture session."""
    try:
        import yaml
    except ImportError as exc:  # pragma: no cover - pyyaml always present
        raise RuntimeError("pyyaml required for re-seed") from exc
    sessions = yaml.safe_load(DATASET.read_text(encoding="utf-8-sig"))["fixtures"][
        "sessions"
    ]
    out: list[tuple[Path, dict]] = []
    for s in sessions:
        task_id = s["task_id"]
        doc = {
            "session_id": s["session_id"],
            "task_id": s.get("task_id"),
            "title": s["title"],
            "started_at": s.get("started_at"),
            "finished_at": s.get("finished_at"),
            "final_status": s.get("final_status"),
            "actions": s.get("actions", []),
            "solutions": s.get("solutions", {}),
            "usage": s.get("usage", None),
        }
        out.append((f"{task_id}.json", doc))
    return out


def reseed_dir() -> Path:
    """Create (or clear) the seed dir and write the deterministic fixtures.

    Returns the directory. Idempotent: pre-existing files are removed first so a
    re-seed after deletion/restart yields exactly the same 5 source files.
    """
    target = oppencode_data_dir()
    target.mkdir(parents=True, exist_ok=True)
    for existing in target.glob("*.json"):
        existing.unlink()
    for name, doc in seed_files():
        (target / name).write_text(
            json.dumps(doc, ensure_ascii=False, indent=2), encoding="utf-8"
        )
    return target


# --------------------------------------------------------------------------- #
# docker compose restart (local stack only)
# --------------------------------------------------------------------------- #

def docker_restart_ok() -> bool:
    """True when docker is present AND a restart of the local stack succeeds."""
    if not _exists("docker"):
        return False
    try:
        proc = subprocess.run(
            ["docker", "compose", "-f", str(COMPOSE_FILE), "restart"],
            capture_output=True,
            text=True,
            timeout=180,
        )
    except subprocess.TimeoutExpired:
        return False
    except FileNotFoundError:
        return False
    return proc.returncode == 0


def restart_compose() -> bool:
    """Restart the local stack, then wait until it is healthy again."""
    if not docker_restart_ok():
        return False
    return wait_for_health(timeout_seconds=90)


def wait_for_health(timeout_seconds: int = 90) -> bool:
    """Poll /api/health until it returns 200 (backend restart takes seconds)."""
    import time

    deadline = time.monotonic() + timeout_seconds
    while time.monotonic() < deadline:
        if c.stack_is_up(c.DEFAULT_BASE_URL):
            return True
        time.sleep(3)
    return False


# --------------------------------------------------------------------------- #
# Wait for one polling cycle so a re-sync becomes visible (<= 300 s)
# --------------------------------------------------------------------------- #

def wait_for_sync(base_url: str | None = None, timeout_seconds: int = 60,
                  expected_count: int | None = None) -> bool:
    """Poll /api/tasks until ``expected_count`` is reached or the count stabilises.

    Waits up to ``timeout_seconds``. Returns True when the expected count is
    observed (when given) or the count stabilised; False on timeout.
    """
    import time

    if base_url is None:
        base_url = c.DEFAULT_BASE_URL

    def task_count() -> int:
        status, body = shared.json_request("/api/tasks", timeout=15)
        if status == 200 and isinstance(body, dict):
            return int(body.get("total", 0))
        return -1

    seen: list[int] = []
    deadline = time.monotonic() + timeout_seconds
    while time.monotonic() < deadline:
        count = task_count()
        if count >= 0:
            if expected_count is not None:
                if count == expected_count:
                    return True
            else:
                seen.append(count)
                if len(seen) >= 3 and len(set(seen[-3:])) == 1:
                    return True
        time.sleep(5)
    return len(seen) >= 1


# --------------------------------------------------------------------------- #
# DELETE /api/history with confirmation (UC-E2E-008)
# --------------------------------------------------------------------------- #

def delete_history(timeout: float = 30.0, confirm: bool = True) -> tuple[int, dict]:
    """DELETE /api/history with the given confirm flag. Returns (status, body)."""
    data = json.dumps({"confirm": confirm}, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        f"{shared.BASE_URL}/api/history",
        data=data,
        method="DELETE",
        headers={"Content-Type": "application/json", "Accept": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            body = resp.read().decode("utf-8", "replace")
            return resp.status, json.loads(body) if body.strip() else {}
    except urllib.error.HTTPError as exc:
        try:
            body = exc.read().decode("utf-8", "replace")
            return exc.code, json.loads(body) if body.strip() else {}
        except Exception:
            return exc.code, {"_raw": body[:400]}
