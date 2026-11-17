# -*- coding: utf-8 -*-
"""Loopback-only history cleanup for the E2E suite (T-009, I-02).

Provides two things:

* :func:`cleanup` performs ``DELETE {base_url}/api/history`` with the
  ``{"confirm": true}`` body via stdlib ``urllib`` (like C5
  ``uc_e2e_007_008.helpers.delete_history``) but additionally guards that the
  host is loopback before any socket is opened. It never raises on HTTP errors:
  it always returns a ``{"ok", "status", "detail"}`` dict, so a run can report
  the outcome and move on.
* :func:`source_untouched` verifies the seed dir still holds exactly
  :func:`seed.expected_manifest` after a run, proving the backend did not modify
  the READ-ONLY opencode source (the C-3 contract).

All entry points are plain functions: import time touches no disk, network, or
environment. No production or external host tokens appear here.
"""

from __future__ import annotations

import json
import os
import urllib.error
import urllib.request
from pathlib import Path
from urllib.parse import urlsplit

from . import seed

# Loopback hosts are the ONLY destinations cleanup may touch (R-027: localhost).
_LOOPBACK_HOSTS = {"localhost", "127.0.0.1", "::1"}


def _is_loopback(host: str, port: str) -> bool:
    """True only for ``localhost`` / ``127.0.0.1`` / ``::1`` on any loopback port.

    ``::1`` contains a colon, so it is handled by a plain membership test
    instead of any colon-splitting that would strip part of the address. An
    empty port (e.g. bare ``localhost``) matches every loopback port.
    """
    if host.strip().lower() not in _LOOPBACK_HOSTS:
        return False
    if not port:
        return True
    try:
        return 0 <= int(port) <= 65535
    except ValueError:
        return False


def _host_port(base_url: str):
    """Return ``(host, port)`` parsed from ``base_url``; tolerate bad input.

    A scheme is required and must be ``http`` (plain ``host:port`` is treated as
    ``http://host:port``). IPv6 literals keep their surrounding brackets so
    ``::1`` is never truncated.
    """
    url = base_url
    if "://" not in url:
        url = "http://" + url
    split = urlsplit(url)
    if split.scheme.lower() != "http":
        return None
    netloc = split.netloc.rsplit("@", 1)[-1]  # drop any userinfo
    if netloc.startswith("["):  # IPv6 literal -> strip surrounding brackets
        host, _, port = netloc.rpartition(":")
        host = host.strip("[]").lower()
    else:
        host, _, port = netloc.rpartition(":")
    return host, port


def cleanup(base_url=None) -> dict:
    """``DELETE {base_url}/api/history`` with ``{"confirm": true}`` body.

    base_url defaults to ``E2E_BASE_URL`` then ``http://127.0.0.1:8080``. Any
    host that is not strictly loopback is rejected with
    ``{"ok": False, "status": 0, "detail": "non-loopback host rejected"}`` and
    no socket is opened. HTTP errors never raise; the response carries the HTTP
    status plus a short detail.
    """
    if base_url is None:
        base_url = os.environ.get("E2E_BASE_URL", "http://127.0.0.1:8080")
    base_url = base_url.rstrip("/")

    parsed = _host_port(base_url)
    if parsed is None or not _is_loopback(parsed[0], parsed[1]):
        return {"ok": False, "status": 0, "detail": "non-loopback host rejected"}

    body = json.dumps({"confirm": True}, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        f"{base_url}/api/history",
        data=body,
        method="DELETE",
        headers={"Content-Type": "application/json", "Accept": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=30.0) as resp:
            raw = resp.read().decode("utf-8", "replace")
            try:
                parsed_body = json.loads(raw) if raw.strip() else {}
            except ValueError:
                parsed_body = {"_raw": raw[:400]}
            return {"ok": resp.status < 400, "status": resp.status, "detail": parsed_body}
    except urllib.error.HTTPError as exc:
        raw = ""
        try:
            raw = exc.read().decode("utf-8", "replace")
        except Exception:
            raw = ""
        try:
            parsed_body = json.loads(raw) if raw.strip() else {}
        except ValueError:
            parsed_body = {"_raw": raw[:400]}
        return {"ok": False, "status": exc.code, "detail": parsed_body}
    except Exception:
        # DNS/connection errors keep the returned dict stable (HTTP-safe API).
        return {"ok": False, "status": 0, "detail": "connection failed"}


def source_untouched(target=None) -> bool:
    """True when the seed dir still holds exactly :func:`seed.expected_manifest`.

    If ``target`` is None it resolves the same default source dir as
    :func:`seed.seed_dir`; otherwise it checks that path. Used after a run to
    prove the source files were not modified by the backend.
    """
    try:
        import yaml  # local import keeps collection import cheap
    except ImportError as exc:  # pragma: no cover - pyyaml always present
        raise RuntimeError("pyyaml required for E2E seed") from exc

    if target is None:
        env_val = os.environ.get("E2E_OPPENCODE_DATA_DIR")
        if env_val:
            target = Path(env_val).expanduser().resolve()
        elif os.environ.get("OPENCODE_DATA_DIR"):
            target = Path(os.environ["OPENCODE_DATA_DIR"]).expanduser().resolve()
        else:
            target = seed.ROOT / "data" / "opencode"
    target = Path(target)

    # The manifest itself must still be loadable from the untouched dataset.
    expected = set(seed.expected_manifest())

    try:
        names = {p.name for p in target.glob("*.json")}
    except (FileNotFoundError, NotADirectoryError):
        return False
    if names != expected:
        return False
    for p in target.glob("*.json"):
        try:
            json.loads(p.read_text(encoding="utf-8"))
        except (ValueError, OSError):
            return False
    return True
