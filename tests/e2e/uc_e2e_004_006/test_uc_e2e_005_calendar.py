# -*- coding: utf-8 -*-
"""UC-E2E-005: Calendar view (GET /api/calendar) — ai-task-tracker (T-009, I-02).

Contract §4.5, dataset fixture ``fixtures.calendar``. Live tests run against
the ISOLATED LOCAL stack only (127.0.0.1 via stdlib urllib); no production
endpoints, no secrets, no external network. Stdlib only.

Collection-only safe: the ``stack`` fixture (module-scoped) skips when the
stack is unhealthy. Assertions are
against the documented contract / dataset, not implementation.
"""

from __future__ import annotations

from datetime import datetime, timedelta

import pytest

from uc_e2e_001_003 import shared

LOAD_TIMEOUT_SECONDS = 3  # R-023: calendar load < 3 s


def _get(path: str) -> tuple[int, dict]:
    """GET via the shared JSON helper; return (status, body_dict)."""
    return shared.json_request(path, timeout=LOAD_TIMEOUT_SECONDS)


def _cell_tasks(cell: dict) -> list:
    """Return the task_ids in a calendar day cell (day or month views)."""
    tasks = cell.get("tasks")
    if tasks is None:
        return []
    return [t.get("task_id") for t in tasks] if isinstance(tasks, list) else []


@pytest.fixture(scope="module")
def stack():  # pragma: no cover - live gate, mirrors C1's module pattern
    """Verify the isolated local stack (skip when it is not healthy).

    Live-gated: stack-down runs SKIP with a clear reason instead of erroring.
    """
    import conftest as c

    base_url = c.DEFAULT_BASE_URL
    if not c.stack_is_up(base_url):
        pytest.skip("local stack unhealthy - live E2E requires docker compose up")
    return base_url


# --------------------------------------------------------------------------- #
# UC-E2E-005
# --------------------------------------------------------------------------- #

def test_week_view_days(stack):
    """Week of 09–15.11.2026: 7 Mon–Sun cells; Tue10 has TASK-001/002 (S1)."""
    status, body = _get("/api/calendar?view=week&date=2026-11-09")
    assert status == 200, f"calendar HTTP {status}"
    assert body["view"] == "week", "view echoes the request"
    assert body["date"] == "2026-11-09", "anchor date echoes the request"
    days = body.get("days", [])
    assert isinstance(days, list) and len(days) == 7, f"week must have 7 cells: {len(days)}"
    # Cells are Mon..Sun: cell0=2026-11-09 (Mon), cell1=2026-11-10 (Tue),
    # cell2=2026-11-11 (Wed), cell6=2026-11-15 (Sun).
    assert days[0]["date"] == "2026-11-09"
    assert days[6]["date"] == "2026-11-15"
    # Tuesday 2026-11-10 carries TASK-001/TASK-002.
    tue = days[1]
    assert tue["date"] == "2026-11-10", f"unexpected Tue date {tue['date']}"
    assert not tue["outside"], "adjacent day flags outside week"
    tue_ids = set(_cell_tasks(tue))
    assert tue_ids == {"TASK-001", "TASK-002"}, f"Tue tasks {tue_ids}"
    # Monday 2026-11-09 is the anchor (not outside) with no tasks.
    assert days[0]["date"] == "2026-11-09"
    assert not days[0].get("outside", False), "anchor day must not be flagged outside"
    assert _cell_tasks(days[0]) == [], "empty anchor day must have no tasks"


def test_week_cell_tasks_match_dataset(stack):
    """Each day cell's task set matches the dataset, keyed by date (S1)."""
    week = shared.dataset()["fixtures"]["calendar"]["week_2026-11-09_2026-11-15"]
    status, body = _get("/api/calendar?view=week&date=2026-11-09")
    assert status == 200, f"calendar HTTP {status}"
    days = body.get("days", [])
    assert len(days) == 7, "week must have 7 cells"
    # Days are Mon..Sun starting at the anchor.
    anchor = datetime.strptime(days[0]["date"], "%Y-%m-%d")
    by_date = {}
    for idx, cell in enumerate(days):
        d = anchor + timedelta(days=idx)
        cell["_date"] = d.strftime("%Y-%m-%d")
        by_date[cell["_date"]] = cell
        del cell["_date"]
    # Days that the dataset marks as having tasks.
    for date, entry in week.items():
        if not isinstance(entry, dict):
            continue
        if date in week.get("empty_days", []):
            continue
        assert date in by_date, f"expected a cell for {date}"
        cell = by_date[date]
        assert cell["date"] == date
        got = set(_cell_tasks(cell))
        want = set(entry["tasks"])
        assert got == want, f"{date}: expected {sorted(want)}, got {sorted(got)}"
    # Days marked empty in the dataset must have no tasks.
    for date in week.get("empty_days", []):
        assert date in by_date, f"expected a cell for {date}"
        assert _cell_tasks(by_date[date]) == [], f"{date} should be empty"
    # The Tuesday cell (2026-11-10) must not be flagged outside the week.
    assert not by_date["2026-11-10"]["outside"], "in-week cell must be outside=false"


def test_day_view_duration_semantics(stack):
    """Day view duration_s == 60 * parse_duration_ru(started + duration) (S2)."""
    day = shared.dataset()["fixtures"]["calendar"]["day_2026-11-11"]
    status, body = _get("/api/calendar?view=day&date=2026-11-11")
    assert status == 200, f"calendar HTTP {status}"
    assert body["view"] == "day", "view echoes the request"
    assert body["date"] == "2026-11-11", "anchor date echoes the request"
    days = body.get("days", [])
    assert len(days) == 1, f"day view must have 1 cell, got {len(days)}"
    resp_tasks = days[0].get("tasks", [])
    assert len(resp_tasks) == len(day["tasks"]), "day view task count matches dataset"
    for exp in day["tasks"]:
        for r in resp_tasks:
            if r["task_id"] != exp["task"]:
                continue
            # Derive expected finished_at via started + parsed duration.
            minute = shared.parse_duration_ru(exp["duration"])
            started = datetime.strptime(
                f"{body['date']}T{exp['start']}", "%Y-%m-%dT%H:%M"
            )
            finished = started + timedelta(minutes=minute)
            expected_finished = finished.strftime("%Y-%m-%dT%H:%M:%S")
            assert r["started_at"].startswith(f"{body['date']}T{exp['start']}"), (
                f"{exp['task']} start {r.get('started_at')} != {exp['start']}"
            )
            assert r["finished_at"] == expected_finished, (
                f"{exp['task']} finished_at {r.get('finished_at')} != {expected_finished}"
            )
            assert r["duration_s"] == 60 * minute, (
                f"{exp['task']} duration_s {r.get('duration_s')} != {60 * minute}"
            )


def test_month_view_task_counts(stack):
    """Month view: 30–31 day cells; Nov10 has 2, Nov11 has 3 tasks (S4)."""
    month = shared.dataset()["fixtures"]["calendar"]["month_2026-11"]
    status, body = _get("/api/calendar?view=month&date=2026-11-01")
    assert status == 200, f"calendar HTTP {status}"
    assert body["view"] == "month", "view echoes the request"
    days = body.get("days", [])
    assert isinstance(days, list) and 28 <= len(days) <= 42 and len(days) % 7 == 0, (
        f"month view expected a full Mon-Sun grid (28-42 cells), got {len(days)}"
    )
    by_date = {cell["date"]: cell for cell in days}
    # Every date the dataset marks with a task count is present and matches.
    for date, count in month.items():
        assert date in by_date, f"expected a month cell for {date}"
        assert len(_cell_tasks(by_date[date])) == count, (
            f"{date}: expected {count} tasks, got {len(_cell_tasks(by_date[date]))}"
        )
    assert len(_cell_tasks(by_date["2026-11-10"])) == 2
    assert len(_cell_tasks(by_date["2026-11-11"])) == 3


def test_invalid_period_400(stack):
    """Calendar with no/invalid period is rejected by client validation (S5)."""
    status, body = _get("/api/calendar")
    assert status == 400, f"missing date expected 400, got {status}"
    assert body.get("code") == "INVALID_PERIOD", f"got code {body.get('code')!r}"
    # A clearly invalid date is also rejected.
    status2, body2 = _get("/api/calendar?view=week&date=nope")
    assert status2 == 400, f"invalid date expected 400, got {status2}"
    assert body2.get("code") == "INVALID_PERIOD", f"got code {body2.get('code')!r}"
