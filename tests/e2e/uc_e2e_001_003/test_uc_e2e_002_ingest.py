# -*- coding: utf-8 -*-
"""UC-E2E-002: Сбор данных об активностях opencode (инжест) (T-009, I-02).

Covers UC-E2E-002 (ingest) from the use-case doc and contract §3/§4.2/§4.3:
after the seed sessions (SES-A..SES-E) are ingested the service exposes exactly
5 tasks TASK-001..TASK-005 with unique ids and per-task final statuses from the
dataset. Isolated local stack only; no production endpoints.

cleanup: UC-E2E-001's ``stack`` fixture owns one ``docker compose down -v``
teardown for the whole batch (see UC-E2E-001). Seed files are the opencode
source and are never modified by the service (contract C-3), so this UC only
asserts ingest output.
"""

from __future__ import annotations

import pytest

from . import shared

SEED_TASKS = shared.task_ids()
SEED_TASK_STATUS = shared.tasks_with_expected_status()
SEED_TASK_TITLE = shared.task_titles()


# --------------------------------------------------------------------------- #
# UC-E2E-002
# --------------------------------------------------------------------------- #

def test_sessions_are_ingested_to_five_tasks(stack):
    """UC-E2E-002 S3: exactly 5 tasks with unique ids, titles from seed."""
    status, body = shared.json_request("/api/tasks")
    assert status == 200, f"tasks HTTP {status}"
    items = body.get("items", [])
    assert body.get("total") == 5, f"expected 5 tasks, got {body.get('total')!r}"

    ids = [t.get("task_id") for t in items]
    assert len(ids) == len(set(ids)), f"task ids not unique: {ids}"
    assert sorted(ids) == sorted(SEED_TASKS), f"task set mismatch: {ids}"
    titles = {t.get("task_id"): t.get("name") for t in items}
    expected = dict(zip(SEED_TASKS, SEED_TASK_TITLE))
    for tid, name in expected.items():
        assert titles.get(tid) == name, f"title for {tid}: {titles.get(tid)!r}"


@pytest.mark.parametrize("task_id,expected_status", SEED_TASK_STATUS)
def test_ingested_task_status_matches_seed(stack, task_id, expected_status):
    """UC-E2E-002 S4: final status matches the seed session's final_status."""
    status, body = shared.json_request("/api/tasks")
    assert status == 200, f"tasks HTTP {status}"
    for t in body.get("items", []):
        if t.get("task_id") == task_id:
            assert t.get("status") == expected_status, (
                f"{task_id} status {t.get('status')!r} (want {expected_status!r})"
            )
            return
    raise AssertionError(f"seed task {task_id} absent after ingest")


def test_ingested_detail_has_six_chronological_steps(stack):
    """UC-E2E-002 S3 -> UC-E2E-003 S3: TASK-001 carries 6 chronological steps."""
    status, body = shared.json_request("/api/tasks/TASK-001")
    assert status == 200, f"detail HTTP {status}"
    steps = body.get("steps", [])
    assert len(steps) == 6, f"TASK-001 steps = {len(steps)} (want 6)"
    ats = [s.get("at") for s in steps]
    assert ats == sorted(ats), f"steps not chronological: {ats}"
    ses = shared.sessions()[0]
    assert ats[0] == ses["actions"][0]["at"], "first step timestamp mismatch"
    assert ats[-1] == ses["actions"][-1]["at"], "last step timestamp mismatch"
