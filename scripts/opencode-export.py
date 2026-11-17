#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Periodic opencode -> harness-e2e/1 JSON exporter (local deployment helper).

Runs a cron-like loop inside the docker-compose ``exporter`` service:

* opens the real opencode SQLite DB (``OPENCODE_DB``) READ-ONLY;
* takes the latest top-level sessions (``EXPORT_LIMIT_SESSIONS``);
* re-exports only sessions whose ``time_updated`` changed (state in ``.state``);
* writes one JSON per session into ``EXPORT_DIR`` (shared with the backend as
  ``OPENCODE_DATA_DIR``), shaped for ``tests/e2e/conftest.py`` /
  ``IngestJsonParser`` (``session_id``, ``title``, ``started_at``,
  ``finished_at``, ``final_status``, ``messages``, ``solutions``, ``usage``).

The backend polls/upserts those files on its own ingest interval
(``INGEST_INTERVAL_SECONDS``), so the calendar/token views fill with the real
activity over time. Stdlib only (sqlite3/json). All data stays local.
"""

from __future__ import annotations

import json
import os
import sqlite3
import sys
import time
from datetime import datetime
from pathlib import Path

DB = os.environ.get("OPENCODE_DB", "/opencode-src/opencode.db")
EXPORT_DIR = Path(os.environ.get("EXPORT_DIR", "/export"))
INTERVAL = int(os.environ.get("EXPORT_INTERVAL_SECONDS", "60"))
LIMIT = int(os.environ.get("EXPORT_LIMIT_SESSIONS", "1000"))
MAX_PARTS = int(os.environ.get("EXPORT_MAX_PARTS", "400"))
MAX_MESSAGES = int(os.environ.get("EXPORT_MAX_MESSAGES", "50"))
MAX_TEXT = int(os.environ.get("EXPORT_MAX_TEXT", "500"))

STATE = EXPORT_DIR / ".state"
FINAL_STATUS = "Р—Р°РІРµСЂС€РµРЅР°"


def _iso(ms) -> str | None:
    try:
        return datetime.fromtimestamp(int(ms) / 1000).strftime("%Y-%m-%dT%H:%M:%S")
    except (TypeError, ValueError, OSError):
        return None


def _to_int(value) -> int:
    try:
        return int(float(value))
    except (TypeError, ValueError):
        return 0


def _model_id(raw) -> str:
    try:
        data = json.loads(raw or "{}")
        return str(data.get("id") or data.get("model") or "unknown")
    except (TypeError, ValueError):
        return "unknown"


def _load_state() -> dict:
    try:
        return json.loads(STATE.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return {}


def _save_state(state: dict) -> None:
    tmp = STATE.with_suffix(".tmp")
    tmp.write_text(json.dumps(state, ensure_ascii=False), encoding="utf-8")
    tmp.replace(STATE)


def _text_messages(cur, session_id: str) -> list[dict]:
    """Bounded text parts of a session (chronological, truncated)."""
    rows = list(
        cur.execute(
            "select message_id, time_created, data from part "
            "where session_id = ? order by time_created limit ?",
            (session_id, MAX_PARTS),
        )
    )
    messages: list[dict] = []
    for message_id, created, raw in rows:
        try:
            data = json.loads(raw or "{}")
        except (TypeError, ValueError):
            continue
        if data.get("type") != "text":
            continue
        text = str(data.get("text") or "").strip()
        if not text:
            continue
        messages.append({
            "message_id": message_id,
            "at": _iso(created),
            "text": text[:MAX_TEXT],
        })
        if len(messages) >= MAX_MESSAGES:
            break
    return messages


def _export_session(cur, row, state: dict) -> bool:
    (sid, title, slug, created, updated, t_in, t_out,
     t_cache_read, t_cache_write, model) = row
    started = _iso(created)
    finished = _iso(updated)
    if not started or not finished:
        return False

    doc = {
        "session_id": sid,
        "title": (title or slug or sid).strip(),
        "started_at": started,
        "finished_at": finished,
        "final_status": FINAL_STATUS,
        "messages": _text_messages(cur, sid),
        "solutions": {"proposed": [], "chosen": None},
        "usage": [{
            "model": _model_id(model),
            "input_tokens": _to_int(t_in),
            "output_tokens": _to_int(t_out),
            "cache_tokens": _to_int(t_cache_read) + _to_int(t_cache_write),
        }],
    }

    target = EXPORT_DIR / f"{sid}.json"
    tmp = target.with_suffix(".json.tmp")
    tmp.write_text(json.dumps(doc, ensure_ascii=False, indent=2), encoding="utf-8")
    tmp.replace(target)
    state[sid] = str(updated)
    return True


def run_once() -> None:
    EXPORT_DIR.mkdir(parents=True, exist_ok=True)
    state = _load_state()
    con = sqlite3.connect(f"file:{DB}?mode=ro", uri=True, timeout=30)
    try:
        cur = con.cursor()
        rows = list(
            cur.execute(
                "select id, title, slug, time_created, time_updated, "
                "tokens_input, tokens_output, tokens_cache_read, tokens_cache_write, model "
                "from session where parent_id is null "
                "order by time_updated desc limit ?",
                (LIMIT,),
            )
        )
        exported = 0
        for row in rows:
            sid, updated = row[0], row[4]
            if state.get(sid) == str(updated):
                continue
            if _export_session(cur, row, state):
                exported += 1
        _save_state(state)
        print(
            "[exporter] sessions=%d exported=%d state=%d"
            % (len(rows), exported, len(state)),
            flush=True,
        )
    finally:
        con.close()


def main() -> None:
    print(
        "[exporter] db=%s export_dir=%s interval=%ss"
        % (DB, EXPORT_DIR, INTERVAL),
        flush=True,
    )
    while True:
        try:
            run_once()
        except Exception as exc:  # keep the cron loop alive on transient errors
            print("[exporter] error: %s" % exc, flush=True)
        time.sleep(INTERVAL)


if __name__ == "__main__":
    if "--once" in sys.argv:
        run_once()
    else:
        main()
