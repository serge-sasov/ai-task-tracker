# -*- coding: utf-8 -*-
"""UC-E2E-003: Просмотр списка задач и деталей задачи (T-009, I-02).

Covers UC-E2E-003 list + detail from the use-case doc and contract §4.2/§4.3:
the list shows TASK-001..TASK-005 with durations (R-009); the detail for each
task exposes actions (R-003), solutions (proposed + chosen, R-004/R-005) with
an explicit empty state when none (TASK-004/005), and the per-model token block
(R-012). Read-only, so no cleanup; one batch teardown lives in UC-E2E-001's
``stack`` fixture. Isolated local stack only; no production endpoints.
"""

from __future__ import annotations

import pytest

from . import shared

EXPECTED_STATUS = shared.tasks_with_expected_status()
EXPECTED_DURATIONS = shared.durations_by_task()


# --------------------------------------------------------------------------- #
# UC-E2E-003
# --------------------------------------------------------------------------- #

@pytest.mark.parametrize("task_id,title", list(zip(shared.task_ids(), shared.task_titles())))
def test_list_shows_five_tasks_with_seed_titles(stack, task_id, title):
    """UC-E2E-003 S1/S2: list of 5, names from seed, durations R-009."""
    status, body = shared.json_request("/api/tasks")
    assert status == 200, f"tasks HTTP {status}"
    items = body.get("items", [])
    assert body.get("total") == 5, f"expected 5 tasks, got {body.get('total')!r}"
    titles = {t.get("task_id"): t.get("name") for t in items}
    assert titles.get(task_id) == title, f"title for {task_id}: {titles.get(task_id)!r}"
    for t in items:
        if t.get("task_id") == task_id:
            expected_min = EXPECTED_DURATIONS[task_id]
            dur_min = (t.get("duration_s") or 0) / 60
            assert dur_min == expected_min, (
                f"{task_id} duration_s {t.get('duration_s')} "
                f"(= {dur_min} min) (want {expected_min} min)"
            )


@pytest.mark.parametrize("task_id,expected_status", EXPECTED_STATUS)
def test_list_task_status_is_canonical(stack, task_id, expected_status):
    """UC-E2E-003 S4: status is the canonical RU value (R-006)."""
    status, body = shared.json_request("/api/tasks")
    assert status == 200, f"tasks HTTP {status}"
    for t in body.get("items", []):
        if t.get("task_id") == task_id:
            assert t.get("status") == expected_status, (
                f"{task_id} status {t.get('status')!r} (want {expected_status!r})"
            )
            return
    raise AssertionError(f"task {task_id} absent in list")


def test_detail_task_001_actions_and_chosen_solution(stack):
    """UC-E2E-003 S3/S4/S6: TASK-001 detail has 6 steps + chosen solution."""
    status, body = shared.json_request("/api/tasks/TASK-001")
    assert status == 200, f"detail HTTP {status}"

    steps = body.get("steps", [])
    assert len(steps) == 6, f"TASK-001 steps = {len(steps)} (want 6)"
    ats = [s.get("at") for s in steps]
    assert ats[0] == "2026-11-10T09:05:00", f"first step at {ats[0]!r}"
    assert ats[-1] == "2026-11-10T11:25:00", f"last step at {ats[-1]!r}"

    solutions = body.get("solutions") or {}
    proposed = [s.get("text") for s in solutions.get("proposed", [])]
    assert proposed == ["GitLab CI", "GitHub Actions"], f"proposed {proposed!r}"
    assert solutions.get("chosen", {}).get("text") == "GitLab CI", (
        f"chosen {solutions.get('chosen')!r}"
    )


@pytest.mark.parametrize("task_id,proposed,chosen", list(shared.iter_solutions()))
def test_detail_solutions_match_seed(stack, task_id, proposed, chosen):
    """UC-E2E-003 S4/S5/S6: proposed/chosen solutions match the seed fixture."""
    status, body = shared.json_request(f"/api/tasks/{task_id}")
    assert status == 200, f"detail HTTP {status}"
    solutions = body.get("solutions") or {}
    got_proposed = [s.get("text") for s in solutions.get("proposed", [])]
    assert got_proposed == proposed, (
        f"{task_id} proposed {got_proposed!r} (want {proposed!r})"
    )
    got_chosen = solutions.get("chosen")
    if chosen is None:
        assert not proposed, f"{task_id} unexpectedly has solutions"
        assert got_chosen is None, f"{task_id} chosen {got_chosen!r} (want null)"
    else:
        assert (got_chosen or {}).get("text") == chosen, (
            f"{task_id} chosen {got_chosen!r} (want {chosen!r})"
        )


def test_detail_task_005_has_explicit_empty_state(stack):
    """UC-E2E-003 S7: TASK-005 with no solutions shows empty proposed/chosen."""
    status, body = shared.json_request("/api/tasks/TASK-005")
    assert status == 200, f"detail HTTP {status}"
    solutions = body.get("solutions") or {}
    assert solutions.get("proposed", []) == [], f"TASK-005 proposed {solutions.get('proposed')!r}"
    assert solutions.get("chosen") is None, f"TASK-005 chosen {solutions.get('chosen')!r}"


def test_detail_token_block_matches_seed_usage(stack):
    """UC-E2E-003 S8: TASK-001 token block matches the seed usage (R-012)."""
    status, body = shared.json_request("/api/tasks/TASK-001")
    assert status == 200, f"detail HTTP {status}"
    tokens = body.get("tokens") or {}
    by_model = {tm.get("model"): tm for tm in tokens.get("by_model", [])}
    ses = shared.sessions()[0]
    usage = ses["usage"][0]
    model = usage["model"]
    got = by_model.get(model, {})
    assert got.get("input_tokens") == usage["input_tokens"], (
        f"{model} input {got.get('input_tokens')} (want {usage['input_tokens']})"
    )
    assert got.get("output_tokens") == usage["output_tokens"], (
        f"{model} output {got.get('output_tokens')} (want {usage['output_tokens']})"
    )
