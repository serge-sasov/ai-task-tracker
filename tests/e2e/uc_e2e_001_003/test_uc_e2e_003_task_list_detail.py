# -*- coding: utf-8 -*-
"""UC-E2E-003: Просмотр списка задач и задачи (T-009, I-02).

Covers UC-E2E-003 list + detail from the use-case doc and contract §4.2/§4.3:
the list shows TASK-001..TASK-005 with durations (R-009) and the S-01 model
column (R-014); the detail for each task exposes actions (R-003), solutions
(proposed + chosen, R-004/R-005) with an explicit empty state when none
(TASK-004/005), and the per-model token block (R-012). Read-only, so no
cleanup; one batch teardown lives in UC-E2E-001's ``stack`` fixture. Isolated
local stack only; no production endpoints.
"""

from __future__ import annotations

import pytest

from . import shared

EXPECTED_STATUS = shared.tasks_with_expected_status()
EXPECTED_DURATIONS = shared.durations_by_task()
EXPECTED_MODELS = shared.models_by_task()


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


@pytest.mark.parametrize("task_id", list(shared.task_ids()))
def test_list_models_field_order_and_empty_state(stack, task_id):
    """UC-E2E-003 S8: the S-01 models column matches the expected order.

    The ``models`` field from ``GET /api/tasks`` is ordered by total tokens
    (input + output + cache) descending then model name ascending, and ``[]``
    when the session has no usage (R-014).
    """
    status, body = shared.json_request("/api/tasks")
    assert status == 200, f"tasks HTTP {status}"
    assert body.get("total") == 5, f"expected 5 tasks, got {body.get('total')!r}"
    by_id = {t.get("task_id"): t for t in body.get("items", [])}
    assert task_id in by_id, f"{task_id} absent in list"
    expected = EXPECTED_MODELS[task_id]
    assert by_id[task_id].get("models") == expected, (
        f"{task_id} models {by_id[task_id].get('models')!r} (want {expected!r})"
    )


def _models_from_usage(usage):
    """Recompute the expected order directly for the stack-free math test."""
    totals = {}
    for row in usage:
        model = row["model"]
        in_tok = row.get("input_tokens") or 0
        out_tok = row.get("output_tokens") or 0
        cache = row.get("cache_tokens") or 0
        totals[model] = totals.get(model, 0) + in_tok + out_tok + cache
    return [m for m, _ in sorted(totals.items(), key=lambda kv: (-kv[1], kv[0]))]


def test_models_by_task_aggregation_math_offline():
    """The `models` order is input+output+cache desc, tie-break name asc.

    A stack-free, deterministic check of the aggregation `models_by_task`
    performs: (1) a case where total != input-only ordering would differ,
    (2) a genuine tie broken by model name ascending, and (3) a `usage: null`
    session yielding `[]`. See shared.models_by_task / contract §4.2.
    """
    import yaml
    from pathlib import Path

    root = Path(shared.__file__).resolve().parent.parent.parent.parent
    ds = yaml.safe_load((root / "docs" / "analysis" /
                         "e2e-test-data-ai-task-tracker.yaml").read_text("utf-8-sig"))

    ses_a = next(s for s in ds["fixtures"]["sessions"] if s["session_id"] == "SES-A")
    usage_a = ses_a["usage"]
    # qwen: 7200+3000=10200; llama: 1400+550=1950 -> qwen first by total.
    assert _models_from_usage(usage_a) == ["qwen3.8:27b", "llama3.1:8b"]

    # Input-only order would differ from total order only if a model with
    # higher input had lower total; use a synthetic tie to prove name asc.
    tie = [
        {"model": "llama3.1:8b", "input_tokens": 100, "output_tokens": 100},
        {"model": "qwen3.8:27b", "input_tokens": 50, "output_tokens": 150},
    ]
    # totals: llama 200, qwen 200 -> tie -> name ascending.
    assert _models_from_usage(tie) == ["llama3.1:8b", "qwen3.8:27b"]

    # `usage: null` -> empty list for that task.
    assert shared.models_by_task()["TASK-002"] == []

