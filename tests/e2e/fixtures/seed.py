# -*- coding: utf-8 -*-
"""Deterministic, idempotent seed writer for the E2E suite (T-009, I-02).

Mirrors the re-seed logic of ``uc_e2e_007_008.helpers.seed_files`` /
``reseed_dir`` so the suite has a self-contained, reusable source writer that
does not import those per-use-case helpers. The corpus is built one
``(name, doc)`` pair per deterministic ``fixtures.sessions`` row; documents are
shaped exactly like the source the backend ingests.

All entry points are plain functions so import time touches no disk, network, or
environment.
"""

from __future__ import annotations

import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent.parent
DATASET = ROOT / "docs" / "analysis" / "e2e-test-data-ai-task-tracker.yaml"


def _load_data(data=None):
    """Return the dataset mapping, loaded lazily from the YAML contract.

    Imports pyyaml inside the function so merely importing this module at
    collection time performs no I/O. ``data`` lets callers pass a mapping
    directly (used by verification in :func:`expected_manifest`).
    """
    try:
        import yaml  # local import keeps collection import cheap
    except ImportError as exc:  # pragma: no cover - pyyaml always present
        raise RuntimeError("pyyaml required for E2E seed") from exc
    if data is not None:
        return data
    text = DATASET.read_text(encoding="utf-8-sig")
    parsed = yaml.safe_load(text)
    if not isinstance(parsed, dict):
        raise ValueError("dataset top-level must be a mapping")
    return parsed


def _sessions(data=None):
    """Return the deterministic ``fixtures.sessions`` list (source of data)."""
    sessions = _load_data(data).get("fixtures", {}).get("sessions")
    if not isinstance(sessions, list):
        raise ValueError("dataset fixtures.sessions must be a list")
    return sessions


def build_corpus(data=None):
    """Return ``list[tuple[str, dict]]`` of ``(name, doc)`` per session row.

    ``name`` is ``f"{task_id}.json"``; ``doc`` is shaped like C5
    ``seed_files()`` (``session_id``, ``task_id``, ``title``, ``started_at``,
    ``finished_at``, ``final_status``, ``actions``, ``solutions``, ``usage``) so
    the backend ingests exactly the same source documents.
    """
    corpus = []
    for s in _sessions(data):
        task_id = s["task_id"]
        doc = {
            "session_id": s.get("session_id"),
            "task_id": s.get("task_id"),
            "title": s.get("title"),
            "started_at": s.get("started_at"),
            "finished_at": s.get("finished_at"),
            "final_status": s.get("final_status"),
            "actions": s.get("actions", []),
            "solutions": s.get("solutions", {}),
            "usage": s.get("usage", None),
        }
        corpus.append((f"{task_id}.json", doc))
    return corpus


def apply(target: Path) -> Path:
    """Idempotently write the corpus into ``target`` and return it.

    Creates the directory, deletes any pre-existing ``*.json`` (so a re-seed
    after a run yields exactly the same files), then writes each corpus doc with
    ``ensure_ascii=False`` and two-space indentation.
    """
    target = Path(target)
    target.mkdir(parents=True, exist_ok=True)
    for existing in target.glob("*.json"):
        existing.unlink()
    for name, doc in build_corpus():
        (target / name).write_text(
            json.dumps(doc, ensure_ascii=False, indent=2), encoding="utf-8"
        )
    return target


def expected_manifest(data=None) -> list[str]:
    """Return the sorted corpus names (the exact set ``verify`` expects)."""
    return sorted(name for name, _ in build_corpus(data))


def verify(target: Path) -> bool:
    """True when ``target`` holds exactly the expected corpus names.

    The directory must exist, its ``*.json`` names must equal
    :func:`expected_manifest` exactly (no extras, no gaps), and every file must
    parse as JSON.
    """
    target = Path(target)
    try:
        names = {p.name for p in target.glob("*.json")}
    except (FileNotFoundError, NotADirectoryError):
        return False
    if names != set(expected_manifest()):
        return False
    for p in target.glob("*.json"):
        try:
            json.loads(p.read_text(encoding="utf-8"))
        except (ValueError, OSError):
            return False
    return True


def seed_dir(target=None) -> Path:
    """Resolve the source dir and write the corpus there (convenience wrapper).

    Resolution order mirrors C5 ``uc_e2e_007_008.helpers.oppencode_data_dir``:
    ``E2E_OPPENCODE_DATA_DIR``, then ``OPENCODE_DATA_DIR``, else
    ``ROOT/data/opencode`` (the compose bind-mount default).
    """
    if target is None:
        env_val = os.environ.get("E2E_OPPENCODE_DATA_DIR")
        if env_val:
            target = Path(env_val).expanduser().resolve()
        elif os.environ.get("OPENCODE_DATA_DIR"):
            target = Path(os.environ["OPENCODE_DATA_DIR"]).expanduser().resolve()
        else:
            target = ROOT / "data" / "opencode"
    return apply(target)
