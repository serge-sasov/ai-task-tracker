#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate deterministic OpenCode session seed files for the E2E runner.

Reads the E2E dataset ``docs/analysis/e2e-test-data-ai-task-tracker.yaml``
(``fixtures.sessions``) and writes one ``<session_id>.json`` file per session
into the target directory. The files match the backend's ingest schema
(``IngestJsonParser``): ``session_id`` required, ``task_id``/``title``/
``started_at``/``finished_at``/``final_status`` optional, plus ``actions``,
``solutions`` and ``usage`` arrays. Stdlib only, deterministic, no secrets.

The OpenCode backend scans the ingest directory for ``*.json`` files and
ingests each one (contracts §3); nothing here is written to the *source*
directory. This writer only emits seed files.

Example:
    python scripts/generate-e2e-seed.py
    python scripts/generate-e2e-seed.py --target-dir ./data/e2e-opencode
    python scripts/generate-e2e-seed.py --dataset docs/analysis/e2e-test-data-ai-task-tracker.yaml
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_DATASET = ROOT / "docs" / "analysis" / "e2e-test-data-ai-task-tracker.yaml"

# Fields copied verbatim into each session file, in this key order.
_TOP_LEVEL_FIELDS = (
    "session_id",
    "task_id",
    "title",
    "started_at",
    "finished_at",
    "final_status",
)


def _session_to_payload(session: dict) -> dict:
    """Map a ``fixtures.sessions`` entry to the ingest seed schema.

    Keeps only the fields the backend ingests (documented keys of
    ``IngestJsonParser``). ``usage: null`` sessions are emitted with an empty
    usage array so ``models`` resolves to [] (the «Нет данных» S-01 case).
    """
    payload: dict = {}
    for key in _TOP_LEVEL_FIELDS:
        if key in session:
            payload[key] = session[key]

    # actions/messages
    if "actions" in session:
        actions = []
        for action in session["actions"]:
            entry = {"at": action.get("at"), "text": action.get("text")}
            actions.append(entry)
        payload["actions"] = actions

    # solutions
    if "solutions" in session:
        solutions = session["solutions"] or {}
        payload["solutions"] = {
            "proposed": list(solutions.get("proposed") or []),
            "chosen": solutions.get("chosen"),
        }

    # usage
    if "usage" in session:
        payload["usage"] = list(session["usage"] or [])
    else:
        # No usage -> empty array so the model column shows «Нет данных».
        payload["usage"] = []

    return payload


def generate(dataset: Path, target_dir: Path) -> list[str]:
    """Write one seed JSON file per session; return the written paths."""
    try:
        import yaml
    except ModuleNotFoundError:  # pragma: no cover - dataset always has PyYAML
        sys.exit("PyYAML is required to read the dataset; pip install pyyaml")

    data = yaml.safe_load(dataset.read_text(encoding="utf-8"))
    sessions = data.get("fixtures", {}).get("sessions")
    if not sessions:
        sys.exit("dataset has no fixtures.sessions to seed")

    target_dir.mkdir(parents=True, exist_ok=True)

    written: list[str] = []
    for session in sessions:
        session_id = session.get("session_id")
        if not session_id:
            continue
        payload = _session_to_payload(session)
        out_file = target_dir / f"{session_id}.json"
        out_file.write_text(
            json.dumps(payload, ensure_ascii=False, indent=2, sort_keys=True)
            + "\n",
            encoding="utf-8",
        )
        written.append(str(out_file))

    return written


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate E2E OpenCode seed files.")
    parser.add_argument(
        "--dataset",
        type=Path,
        default=DEFAULT_DATASET,
        help="Path to the E2E dataset YAML (default: %(default)s).",
    )
    parser.add_argument(
        "--target-dir",
        type=Path,
        default=Path("./data/e2e-opencode"),
        help="Where to write <session_id>.json seed files (default: %(default)s).",
    )
    args = parser.parse_args()

    if not args.dataset.exists():
        sys.exit(f"dataset not found: {args.dataset}")

    written = generate(args.dataset, args.target_dir)
    print(f"Generated {len(written)} seed file(s) in {args.target_dir}:")
    for path in written:
        print(f"  {path}")


if __name__ == "__main__":
    main()
