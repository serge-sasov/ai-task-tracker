# -*- coding: utf-8 -*-
"""UC-E2E-007: РќР°РґС‘Р¶РЅРѕСЃС‚СЊ вЂ” РїРµСЂРµР·Р°РїСѓСЃРє СЃРµСЂРІРёСЃР° Рё РёРґРµРјРїРѕС‚РµРЅС‚РЅР°СЏ СЃРёРЅС…СЂРѕРЅРёР·Р°С†РёСЏ
(ai-task-tracker, T-009, I-02).

Contract В§3 (idempotency), В§4.1 (health), В§4.2 (list), В§4.3 (detail),
В§4.6 (token totals); dataset fixtures ``fixtures.sessions`` /
``fixtures.token_totals``. Live tests run against the ISOLATED LOCAL stack only
(127.0.0.1 via stdlib urllib + ``docker compose restart``); no production
endpoints, no secrets, no external network. Stdlib only.

UC-E2E-007 main flow (contract / dataset):
  S1  docker compose restart в†’ GET /api/health в†’ 200 ok (R-028)
  S2  СЃРїРёСЃРѕРє Р·Р°РґР°С‡ РїРѕСЃР»Рµ СЂРµСЃС‚Р°СЂС‚Р° в†’ 5 Р·Р°РґР°С‡ TASK-001..005, РїСЂРµР¶РЅРёРµ ID/СЃС‚Р°С‚СѓСЃС‹
  S3  СЃС‚Р°С‚РёРЅРіР° С‚РѕРєРµРЅРѕРІ РїРѕСЃР»Рµ СЂРµСЃС‚Р°СЂС‚Р° в†’ 18260 (12850 in / 5410 out)
  S4  РїРѕРІС‚РѕСЂРЅРѕРµ СЂР°Р·РјРµС‰РµРЅРёРµ С‚РµС… Р¶Рµ seed-С„Р°Р№Р»РѕРІ в†’ РІСЃС‘ РµС‰С‘ 5 Р·Р°РґР°С‡, TASK-001 = 6
      С€Р°РіРѕРІ, Р±РµР· РґСѓР±Р»РµР№ (R-025)
  S5  СЃСѓРјРјС‹ С‚РѕРєРµРЅРѕРІ РїРѕСЃР»Рµ РїРѕРІС‚РѕСЂРЅРѕРіРѕ РёРјРїРѕСЂС‚Р° unchanged (R-025)

Error flows:
  E1  health down after restart в†’ FAIL (covered as skip-guard + R-028 assertion)
  E2  re-sync creates duplicates (6+ tasks) в†’ FAIL with task-list dump
  E3  half-records (task present, steps/tokens missing) в†’ FAIL (atomicity)

Collection-only safe: the ``stack`` fixture (module-scoped) performs a fast
health pre-check and ``pytest.skip``s with a clear reason when the local stack /
docker is unavailable, so ``pytest --collect-only`` and stack-down machines never
attempt I/O. The destructive steps (restart / re-sync) live only inside live
tests, never at collection time.

cleanup: each test restores service state in teardown (re-seed the seed dir +
wait one polling cycle, or the whole-batch ``docker compose down -v`` + re-import
in conftest), so UC-E2E-008 and later batches are not poisoned.
"""

from __future__ import annotations

import warnings

import pytest

import conftest as c
from uc_e2e_001_003 import shared
from uc_e2e_007_008 import helpers


LOAD_TIMEOUT_SECONDS = 3  # R-023: task/token stats load < 3 s
EXPECTED_TASK_IDS = ["TASK-001", "TASK-002", "TASK-003", "TASK-004", "TASK-005"]
# Canonical RU statuses come from the dataset (single source of truth).
EXPECTED_STATUS = dict(shared.tasks_with_expected_status())


# --------------------------------------------------------------------------- #
# Stack gate + state restore (isolation between tests / batches)
# --------------------------------------------------------------------------- #

@pytest.fixture(scope="module")
def stack():  # pragma: no cover - live gate, collection-safe
    """UC-E2E-007 live gate: skip the whole module when the local stack is down.

    Fast pre-check on /api/health. On a stack-down machine ``pytest.skip``s with
    a clear reason (docker restart / re-sync unavailable) so collect-only and
    non-docker environments never hard-fail. Mirrors the C1/C4 module pattern.
    """
    if not c.stack_is_up(c.DEFAULT_BASE_URL):
        pytest.skip("UC-E2E-007: local stack unhealthy вЂ” docker restart and re-sync require a live stack")
    return c.DEFAULT_BASE_URL


@pytest.fixture(scope="function", autouse=True)
def restore_state(stack):  # pragma: no cover - isolation guard
    """Re-seed the opencode seed dir and wait one polling cycle after each test.

    Guarantees the service DB is back to the deterministic 5 tasks before the
    next test runs (so a failed test cannot leave half-synced records вЂ” E3), and
    that UC-E2E-008 / later batches start from the same clean state. Best-effort:
    does not fail the test if the stack drops mid-suite.
    """
    yield
    try:
        helpers.reseed_dir()
        helpers.wait_for_sync(timeout_seconds=60, expected_count=5)
    except Exception as exc:  # pragma: no cover - best effort restore
        warnings.warn(f"UC-E2E-007 teardown restore failed: {exc}")


# --------------------------------------------------------------------------- #
# Local accessors
# --------------------------------------------------------------------------- #

def _tasks() -> tuple[int, dict]:
    status, body = c.json_request("/api/tasks", timeout=LOAD_TIMEOUT_SECONDS)
    return status, body


def _task_ids(body: dict) -> list:
    return [it.get("task_id") for it in body.get("items", [])]


def _detail(task_id: str) -> dict:
    status, body = shared.json_request(f"/api/tasks/{task_id}", timeout=LOAD_TIMEOUT_SECONDS)
    assert status == 200, f"detail HTTP {status} for {task_id}"
    return body


def _token_totals() -> tuple[int, dict]:
    status, body = c.json_request("/api/tokens?period=all", timeout=LOAD_TIMEOUT_SECONDS)
    return status, body


# --------------------------------------------------------------------------- #
# UC-E2E-007 main flow
# --------------------------------------------------------------------------- #

def test_health_after_restart(stack):
    """UC-E2E-007 S1: docker compose restart then /api/health в†’ 200 ok (R-028)."""
    ok = helpers.restart_compose()
    assert ok, "UC-E2E-007 S1: docker compose restart returned non-zero"
    status, health = shared.json_request(shared.HEALTH_PATH, timeout=c.HEALTH_TIMEOUT_SECONDS)
    assert status == 200, f"UC-E2E-007 S1: health HTTP {status}"
    ollama = health.get("ollama_status", health.get("ollamaStatus"))
    expected_status = "ok" if ollama == "up" else "degraded"
    shared.assert_json_path(health, "$.status", expected_status, "health after restart")
    shared.assert_json_path(health, "$.checks.postgres", "ok", "postgres after restart")
    shared.assert_json_path(health, "$.checks.redis", "ok", "redis after restart")


def test_list_unchanged_after_restart(stack):
    """UC-E2E-007 S2: list still 5 tasks TASK-001..005 with prior statuses (R-025)."""
    helpers.restart_compose()
    status, body = _tasks()
    assert status == 200, f"UC-E2E-007 S2: tasks HTTP {status}"
    assert body.get("total") == 5, f"UC-E2E-007 S2: total {body.get('total')}"
    ids = _task_ids(body)
    assert sorted(ids) == sorted(EXPECTED_TASK_IDS), f"UC-E2E-007 S2: task ids changed after restart: {ids}"
    statuses = {it.get("task_id"): it.get("status") for it in body.get("items", [])}
    for tid, want in EXPECTED_STATUS.items():
        assert statuses.get(tid) == want, (
            f"UC-E2E-007 S2: status of {tid} = {statuses.get(tid)!r} (want {want!r})"
        )
    # TASK-001 retains exactly 6 steps (R-003), no duplication after restart.
    detail = _detail("TASK-001")
    assert len(detail.get("steps", [])) == 6, (
        f"UC-E2E-007 S2: TASK-001 steps = {len(detail.get('steps', []))}"
    )


def test_token_totals_unchanged_after_restart(stack):
    """UC-E2E-007 S3: token totals unchanged after restart (18260) (R-025)."""
    helpers.restart_compose()
    status, body = _token_totals()
    assert status == 200, f"UC-E2E-007 S3: tokens HTTP {status}"
    totals = body.get("total", {})
    assert totals.get("total_tokens") == 18260, f"UC-E2E-007 S3: total {totals}"
    assert totals.get("input_tokens") == 12850, f"UC-E2E-007 S3: in {totals}"
    assert totals.get("output_tokens") == 5410, f"UC-E2E-007 S3: out {totals}"


# --------------------------------------------------------------------------- #
# UC-E2E-007 re-sync / idempotency
# --------------------------------------------------------------------------- #

def test_resync_idempotent_no_duplicates(stack):
    """UC-E2E-007 S4 + E2: re-seed same files в†’ still 5 tasks, TASK-001 = 6 steps.

    Re-places the identical seed files in E2E_OPPENCODE_DATA_DIR, waits one
    polling cycle, then asserts no duplicate IDs (R-025). E2: if 6+ tasks appear
    after re-sync, FAIL with the full task-list dump.
    """
    reseeded = helpers.reseed_dir()
    assert reseeded.exists(), "UC-E2E-007 S4: seed dir was not created"

    synced = helpers.wait_for_sync(timeout_seconds=120, expected_count=5)
    assert synced, "UC-E2E-007 S4: re-sync polling did not stabilise within timeout"

    status, body = _tasks()
    assert status == 200, f"UC-E2E-007 S4: tasks HTTP {status}"
    ids = _task_ids(body)
    if len(ids) > 5 or len(set(ids)) != len(ids):
        raise AssertionError(
            f"UC-E2E-007 E2: duplicates after re-sync вЂ” task-list dump: {body!r}"
        )
    assert sorted(ids) == sorted(EXPECTED_TASK_IDS), f"UC-E2E-007 S4: task ids changed: {ids}"

    detail = _detail("TASK-001")
    assert len(detail.get("steps", [])) == 6, (
        f"UC-E2E-007 S4: TASK-001 steps after re-sync = {len(detail.get('steps', []))}"
    )


def test_no_partial_records_after_resync(stack):
    """UC-E2E-007 S5 + E3: totals unchanged and per-task data intact (atomicity)."""
    helpers.reseed_dir()
    helpers.wait_for_sync(timeout_seconds=120, expected_count=5)

    # Totals unchanged after re-import (R-025).
    status, body = _token_totals()
    assert status == 200, f"UC-E2E-007 S5: tokens HTTP {status}"
    totals = body.get("total", {})
    assert totals.get("total_tokens") == 18260, f"UC-E2E-007 S5: total {totals}"

    # No half-records: TASK-001 has all 6 steps AND non-zero token totals.
    detail = _detail("TASK-001")
    assert len(detail.get("steps", [])) == 6, "UC-E2E-007 E3: TASK-001 missing steps"
    tok_total = detail.get("tokens", {}).get("total", {})
    assert tok_total.get("total_tokens") == 12150, (
        f"UC-E2E-007 E3: TASK-001 token total {tok_total!r} (want 12150)"
    )
