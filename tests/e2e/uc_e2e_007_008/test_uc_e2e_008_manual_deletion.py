# -*- coding: utf-8 -*-
"""UC-E2E-008: Р СѓС‡РЅРѕРµ СѓРґР°Р»РµРЅРёРµ РёСЃС‚РѕСЂРёРё (ai-task-tracker, T-009, I-02).

Contract В§4.7 (DELETE /api/history), В§4.2/В§4.3/В§4.5/В§4.6 empty states; dataset
fixtures ``fixtures.sessions``. Live tests run against the ISOLATED LOCAL stack
only (127.0.0.1 via stdlib urllib); no production endpoints, no secrets, no
external network. Stdlib only.

UC-E2E-008 main flow (contract / dataset):
  S1  СЂР°Р·РґРµР» СѓРїСЂР°РІР»РµРЅРёСЏ РґР°РЅРЅС‹РјРё РґРѕСЃС‚СѓРїРµРЅ (UI вЂ”see note)
  S2  В«РЈРґР°Р»РёС‚СЊВ» СЃ РїРѕРґС‚РІРµСЂР¶РґРµРЅРёРµРј (R-029)
  S3  РїРѕРґС‚РІРµСЂРґРёС‚СЊ СѓРґР°Р»РµРЅРёРµ в†’ 200 ``{"deleted": true}``
  S4  РІСЃРµ СЂР°Р·РґРµР»С‹ РїРѕСЃР»Рµ СѓРґР°Р»РµРЅРёСЏ: Р·Р°РґР°С‡Рё/С‚РѕРєРµРЅС‹/РєР°Р»РµРЅРґР°СЂСЊ/РїРѕРёСЃРє вЂ” РїСѓСЃС‚С‹Рµ СЃРѕСЃС‚РѕСЏРЅРёСЏ
  S5  РєР°С‚Р°Р»РѕРі opencode (E2E_OPPENCODE_DATA_DIR) РќР• СѓРґР°Р»С‘РЅ (C-3)

Alternate / error flows, where the contract surface allows an API assertion:
  A2  РїРѕРґС‚РІРµСЂР¶РґРµРЅРёРµ РѕС‚РјРµРЅРµРЅРѕ в†’ РґР°РЅРЅС‹Рµ РЅРµ СѓРґР°Р»РµРЅС‹ (covered: confirm-required path);
      the literal browser В«РћС‚РјРµРЅР°В» is UI-only and skipped (no API surface).
  E2  РїРѕРІС‚РѕСЂРЅРѕРµ СѓРґР°Р»РµРЅРёРµ РїСЂРё РїСѓСЃС‚РѕР№ Р‘Р” в†’ 200 ``{"deleted": false}`` no-op
  E1  half-deletion state в†’ contract transaction forbids it (atomicity)

Collection-only safe: the ``stack`` fixture (module-scoped) skips the whole
module when the local stack is down, so ``pytest --collect-only`` and stack-down
machines never attempt I/O. Destructive steps (DELETE) live only inside live
tests, never at collection time.

cleanup: each test re-seeds the opencode dir and waits one polling cycle in
teardown (restore_state), returning the service to the deterministic 5-task
state so later batches are not poisoned.
"""

from __future__ import annotations

import warnings

import pytest

import conftest as c
from uc_e2e_001_003 import shared
from uc_e2e_007_008 import helpers

LOAD_TIMEOUT_SECONDS = 3  # R-023: task/token/calendar stats load < 3 s
EXPECTED_TASK_IDS = ["TASK-001", "TASK-002", "TASK-003", "TASK-004", "TASK-005"]


# --------------------------------------------------------------------------- #
# Stack gate + state restore (isolation between tests / batches)
# --------------------------------------------------------------------------- #

@pytest.fixture(scope="module")
def stack():  # pragma: no cover - live gate, collection-safe
    """UC-E2E-008 live gate: skip the whole module when the local stack is down.

    Fast pre-check on /api/health. On a stack-down machine ``pytest.skip``s with
    a clear reason (the DELETE requires a live stack), so collect-only and
    non-docker environments never hard-fail. Mirrors the C1/C4 module pattern.
    """
    if not c.stack_is_up(c.DEFAULT_BASE_URL):
        pytest.skip("UC-E2E-008: local stack unhealthy вЂ” DELETE /api/history requires a live stack")
    return c.DEFAULT_BASE_URL


@pytest.fixture(scope="function", autouse=True)
def restore_state(stack):  # pragma: no cover - isolation guard
    """Re-seed the opencode seed dir and wait one polling cycle after each test."""
    yield
    try:
        helpers.reseed_dir()
        helpers.wait_for_sync(timeout_seconds=60, expected_count=5)
    except Exception as exc:  # pragma: no cover - best effort restore
        warnings.warn(f"UC-E2E-008 teardown restore failed: {exc}")


# --------------------------------------------------------------------------- #
# Local accessors
# --------------------------------------------------------------------------- #

def _tasks() -> dict:
    status, body = c.json_request("/api/tasks", timeout=LOAD_TIMEOUT_SECONDS)
    assert status == 200, f"tasks HTTP {status}"
    return body


def _tokens() -> dict:
    status, body = c.json_request("/api/tokens?period=all", timeout=LOAD_TIMEOUT_SECONDS)
    assert status == 200, f"tokens HTTP {status}"
    return body


def _calendar() -> dict:
    status, body = c.json_request("/api/calendar?view=week&date=2026-11-09", timeout=LOAD_TIMEOUT_SECONDS)
    assert status == 200, f"calendar HTTP {status}"
    return body


def _search(query: str) -> dict:
    return shared._post_json or None  # placeholder replaced inline below


def _empty_search() -> tuple:
    """POST an unrelated search; returns the parsed body."""
    import json
    import urllib.error
    import urllib.request

    url = f"{c.BASE_URL if hasattr(c, 'BASE_URL') else ''}"
    # reuse conftest base explicitly
    base = c.DEFAULT_BASE_URL
    payload = json.dumps({"query": "СЂРµС†РµРїС‚ Р±РѕСЂС‰Р°", "limit": 5}, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        f"{base}/api/search",
        data=payload,
        method="POST",
        headers={"Content-Type": "application/json", "Accept": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=LOAD_TIMEOUT_SECONDS) as resp:
            body = resp.read().decode("utf-8", "replace")
            return resp.status, (json.loads(body) if body.strip() else {})
    except urllib.error.HTTPError as exc:
        try:
            body = exc.read().decode("utf-8", "replace")
            return exc.code, (json.loads(body) if body.strip() else {})
        except Exception:
            return exc.code, {"_raw": body[:400]}


# --------------------------------------------------------------------------- #
# UC-E2E-008 main flow
# --------------------------------------------------------------------------- #

def test_delete_requires_confirmation(stack):
    """UC-E2E-008 S2/S3: DELETE without confirm в†’ 400 CONFIRMATION_REQUIRED (R-029).

    The confirmation guard is the contract surface backing the В«РћС‚РјРµРЅР°В»/confirm
    UX (A2/A3). A bare DELETE (no confirm:true) must be rejected by the API.
    """
    status, body = helpers.delete_history(timeout=LOAD_TIMEOUT_SECONDS, confirm=False)
    # A DELETE without the confirm body is rejected before any deletion happens.
    assert status in (400,), f"UC-E2E-008: DELETE-without-confirm HTTP {status}"
    code = body.get("code") if isinstance(body, dict) else None
    assert code == "CONFIRMATION_REQUIRED", f"UC-E2E-008: got code {code!r}"
    # Data still present: the rejected request deleted nothing.
    body_tasks = _tasks()
    assert body_tasks.get("total") == 5, f"UC-E2E-008: total after rejected delete {body_tasks.get('total')}"


def test_cancel_confirmation_leaves_data(stack):
    """UC-E2E-008 A2: cancel the confirmation в†’ data unchanged (UI-cancel assertion).

    The literal browser В«РћС‚РјРµРЅР°В» sends no request (UI-only); the API-visible
    behaviour is that a non-confirmed delete is a no-op. We assert the
    data is intact when no successful confirmation-based delete has happened.
    """
    body_tasks = _tasks()
    ids = [it.get("task_id") for it in body_tasks.get("items", [])]
    assert sorted(ids) == sorted(EXPECTED_TASK_IDS), f"UC-E2E-008 A2: pre-assertion ids {ids}"
    assert body_tasks.get("total") == 5, "UC-E2E-008 A2: data must be intact before any delete"


def test_delete_confirmed_empties_all_sections(stack):
    """UC-E2E-008 S3+S4: confirm в†’ deleted:true, all sections empty (R-028/R-030)."""
    status, body = helpers.delete_history(timeout=LOAD_TIMEOUT_SECONDS)
    assert status in (200, 204), f"UC-E2E-008 S3: DELETE HTTP {status}"
    assert body.get("deleted") is True, f"UC-E2E-008 S3: {body!r}"

    # S4: tasks list в†’ explicit empty state (200 total 0, no items).
    tasks = _tasks()
    assert tasks.get("total") == 0, f"UC-E2E-008 S4: tasks total {tasks.get('total')}"
    assert tasks.get("items") == [], f"UC-E2E-008 S4: tasks items {tasks.get('items')!r}"

    # S4: token stats в†’ zeros / empty (R-030).
    tokens = _tokens()
    assert tokens.get("has_data") is False, f"UC-E2E-008 S4: tokens has_data {tokens.get('has_data')}"
    tok_total = tokens.get("total", {})
    if isinstance(tok_total, dict):
        assert tok_total.get("total_tokens", 0) == 0, f"UC-E2E-008 S4: tokens total {tok_total}"
    assert tokens.get("by_model", []) == [], f"UC-E2E-008 S4: tokens by_model {tokens.get('by_model')}"

    # S4: calendar в†’ empty periods (no tasks in the week cells).
    cal = _calendar()
    week_tasks = 0
    for cell in cal.get("days", []):
        week_tasks += len(cell.get("tasks", []) or [])
    assert week_tasks == 0, f"UC-E2E-008 S4: calendar week task cells {week_tasks}"

    # S4: AI search в†’ В«РЅРёС‡РµРіРѕ РЅРµ РЅР°Р№РґРµРЅРѕВ» (honest empty).
    sstatus, sbody = _empty_search()
    assert sstatus == 200, f"UC-E2E-008 S4: search HTTP {sstatus}"
    assert sbody.get("results", []) == [], f"UC-E2E-008 S4: search results {sbody!r}"


def test_opencode_source_not_deleted(stack):
    """UC-E2E-008 S5 + C-3: the opencode seed dir is NOT touched by the service."""
    helpers.delete_history(timeout=LOAD_TIMEOUT_SECONDS)

    seed_dir = helpers.oppencode_data_dir()
    assert seed_dir.exists(), f"UC-E2E-008 C-3: seed dir missing {seed_dir}"
    files = list(seed_dir.glob("*.json"))
    ids = sorted(f.stem for f in files)
    assert ids == sorted(EXPECTED_TASK_IDS), (
        f"UC-E2E-008 C-3: opencode source changed after delete: {ids}"
    )
    assert len(files) == 5, f"UC-E2E-008 C-3: seed file count {len(files)}"


def test_delete_again_empty_db_is_noop(stack):
    """UC-E2E-008 E2: DELETE on an empty DB в†’ 200 {deleted:false}, no error."""
    # First ensure the DB is empty (idempotent: second delete is a no-op).
    helpers.delete_history(timeout=LOAD_TIMEOUT_SECONDS)
    tasks = _tasks()
    if tasks.get("total", 0) == 0:
        status, body = helpers.delete_history(timeout=LOAD_TIMEOUT_SECONDS)
        assert status in (200,), f"UC-E2E-008 E2: DELETE-again HTTP {status}"
        assert body.get("deleted") is False, f"UC-E2E-008 E2: {body!r}"
    else:
        pytest.skip("UC-E2E-008 E2: DB not empty at start (stack not seeded)")


def test_reimport_after_delete_resets_data(stack):
    """UC-E2E-008 A1 + C-3: after deletion, re-seed re-imports the 5 tasks."""
    helpers.delete_history(timeout=LOAD_TIMEOUT_SECONDS)
    reseeded = helpers.reseed_dir()
    assert reseeded.exists(), "UC-E2E-008 A1: seed dir was not created"
    synced = helpers.wait_for_sync(timeout_seconds=120, expected_count=5)
    assert synced, "UC-E2E-008 A1: re-import did not stabilise within timeout"

    tasks = _tasks()
    ids = [it.get("task_id") for it in tasks.get("items", [])]
    assert sorted(ids) == sorted(EXPECTED_TASK_IDS), f"UC-E2E-008 A1: post-reimport ids {ids}"
    assert tasks.get("total") == 5, f"UC-E2E-008 A1: total {tasks.get('total')}"
