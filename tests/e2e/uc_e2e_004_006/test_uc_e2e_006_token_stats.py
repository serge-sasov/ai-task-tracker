# -*- coding: utf-8 -*-
"""UC-E2E-006: Token statistics (GET /api/tokens) вЂ” ai-task-tracker (T-009, I-02).

Contract В§4.6, dataset fixture ``fixtures.token_totals``. Live tests run
against the ISOLATED LOCAL stack only (127.0.0.1 via stdlib urllib); no
production endpoints, no secrets, no external network. Stdlib only.

Collection-only safe: the ``stack`` fixture (module-scoped) skips when the
stack is unhealthy. Assertions are
against the documented contract / dataset, not implementation.
"""

from __future__ import annotations

import pytest

from uc_e2e_001_003 import shared

LOAD_TIMEOUT_SECONDS = 3  # R-023: token stats load < 3 s


def _get(path: str) -> tuple[int, dict]:
    """GET via the shared JSON helper; return (status, body_dict)."""
    return shared.json_request(path, timeout=LOAD_TIMEOUT_SECONDS)


def _by_model(body: dict) -> dict:
    """Build a {model: {input_tokens, output_tokens, total_tokens}} map."""
    out = {}
    for row in body.get("by_model", []):
        out[row["model"]] = {
            "input_tokens": row["input_tokens"],
            "output_tokens": row["output_tokens"],
            "total_tokens": row["total_tokens"],
        }
    return out


def _same_by_model(a: dict, b: dict, models: list) -> bool:
    """All requested models carry identical input/output/total in a and b."""
    return all(a[m] == b[m] for m in models)


def _assert_period(body: dict, expected: dict, models: list, where: str) -> None:
    """Assert body totals equal expected per-model totals for the given models."""
    got = _by_model(body)
    for m in models:
        exp = expected[m]
        assert got[m]["input_tokens"] == exp["input_tokens"], (
            f"[{where}] {m} input {got[m]['input']} != {exp['input']}"
        )
        assert got[m]["output_tokens"] == exp["output_tokens"], (
            f"[{where}] {m} output {got[m]['output']} != {exp['output']}"
        )
        assert got[m]["total_tokens"] == exp["total_tokens"], (
            f"[{where}] {m} total {got[m]['total']} != {exp['total']}"
        )


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
# UC-E2E-006
# --------------------------------------------------------------------------- #

def test_all_period_totals(stack):
    """All-period totals equal per-model sums and grand_total (S1)."""
    stats = shared.dataset()["fixtures"]["token_totals"]
    all_time = stats["all_time"]
    models = [row["model"] for row in all_time["by_model"]]
    status, body = _get("/api/tokens?period=all")
    assert status == 200, f"tokens HTTP {status}"
    # Each model's total == input + output.
    for row in all_time["by_model"]:
        got = _by_model(body)[row["model"]]
        assert got["input_tokens"] == row["input_tokens"], (
            f"qwen/llama input {got['input_tokens']} != {row['input_tokens']}"
        )
        assert got["output_tokens"] == row["output_tokens"], (
            f"{row['model']} output {got['output_tokens']} != {row['output_tokens']}"
        )
        assert got["total_tokens"] == row["total_tokens"], (
            f"{row['model']} total {got['total_tokens']} != {row['total_tokens']}"
        )
    # Grand total matches the dataset grand_total.
    gt = body.get("total", {})
    assert gt.get("input_tokens") == all_time["grand_total"]["input_tokens"], (
        f"grand input {gt.get('input_tokens')} != {all_time['grand_total']['input_tokens']}"
    )
    assert gt.get("output_tokens") == all_time["grand_total"]["output_tokens"], (
        f"grand output {gt.get('output_tokens')} != {all_time['grand_total']['output_tokens']}"
    )
    assert gt.get("total_tokens") == all_time["grand_total"]["total_tokens"], (
        f"grand total {gt.get('total_tokens')} != {all_time['grand_total']['total_tokens']}"
    )


def test_period_2026_11_10_totals(stack):
    """Single-period totals match dataset for 10.11 (and 11.11) (S2/S3)."""
    stats = shared.dataset()["fixtures"]["token_totals"]
    for period, key in (("2026-11-10", "period_2026-11-10"),
                        ("2026-11-11", "period_2026-11-11")):
        expected = {row["model"]: row for row in stats[key]["by_model"]}
        models = list(expected.keys())
        status, body = _get(f"/api/tokens?period={period}")
        assert status == 200, f"tokens HTTP {status} for {period}"
        assert body.get("period") == period, f"period echoes {period}"
        _assert_period(body, expected, models, f"period {period}")
    # TASK-002 (SES-B) carries no usage; confirm it contributes nothing by
    # checking the 11-10 totals equal the SES-A-only expectation.
    status, body = _get("/api/tokens?period=2026-11-10")
    got = _by_model(body)
    assert got["llama3.1:8b"]["input_tokens"] == 1400, "11-10 llama input"
    assert got["llama3.1:8b"]["output_tokens"] == 550, "11-10 llama output"


def test_range_equals_all(stack):
    """Range 10.11вЂ“11.11 equals the all-period totals exactly (S4)."""
    stats = shared.dataset()["fixtures"]["token_totals"]
    models = [row["model"] for row in stats["all_time"]["by_model"]]
    status, body = _get("/api/tokens?from=2026-11-10&to=2026-11-11")
    assert status == 200, f"tokens HTTP {status}"
    status_all, body_all = _get("/api/tokens?period=all")
    assert _same_by_model(_by_model(body), _by_model(body_all), models), (
        "range totals differ from all-period totals"
    )
    assert body.get("total") == body_all.get("total"), "range grand_total differs"


def test_from_gt_to_400(stack):
    """from > to is rejected by client validation (S4)."""
    status, body = _get("/api/tokens?from=2026-11-12&to=2026-11-10")
    assert status == 400, f"from>to expected 400, got {status}"
    assert body.get("code") == "INVALID_PERIOD", f"got code {body.get('code')!r}"
    assert "Некорректный период" in body.get("message", ""), (
        f"missing client message, got {body.get('message')!r}"
    )


def test_has_data_true(stack):
    """All-period response reports has_data=true (S1)."""
    status, body = _get("/api/tokens?period=all")
    assert status == 200, f"tokens HTTP {status}"
    assert body.get("has_data") is True, "has_data must be true when data exists"
    assert len(body.get("by_model", [])) > 0, "by_model must be non-empty"
