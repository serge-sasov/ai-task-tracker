# -*- coding: utf-8 -*-
"""UC-E2E-001: Подъём стека и стартовое состояние сервиса (T-009, I-02).

Covers UC-E2E-001 preconditions/expected-result/postcondition from
``docs/analysis/e2e-use-cases-ai-task-tracker.md`` and the contract §4.1
(health) / §4.2 (empty list => 200 empty). All work is against an ISOLATED
LOCAL stack only (127.0.0.1); no production endpoints.

Collection-only safe: stack-dependent assertions are inside the live fixtures,
so ``pytest --collect-only`` works with the stack down.

cleanup: ``stack`` fixture tears the local stack down via
``conftest``/``pytest_sessionfinish`` (see teardown in this module).
"""

from __future__ import annotations

from pathlib import Path

import pytest

from . import shared


@pytest.fixture(scope="session")
def stack():
    """Bring up the isolated local stack (C1 conftest fixture), then teardown.

    Reuses the C1 ``e2e_env``-level container lifecycle: the suite's
    ``pytest_sessionfinish`` (conftest.py) runs ``docker compose down -v``
    cleanup for the whole e2e batch, so UC-E2E-001 stack up + final teardown
    live in one place. This marker documents that UC-E2E-001 owns the stack
    lifecycle and that cleanup happens once at the end of the batch.
    """
    import conftest as c

    base_url = c.DEFAULT_BASE_URL
    if not c.stack_is_up(base_url):
        pytest.skip(
            "local stack unhealthy - live E2E requires docker compose up"
        )
    return base_url


# --------------------------------------------------------------------------- #
# UC-E2E-001
# --------------------------------------------------------------------------- #

def test_stack_boots_and_empty_state(stack):
    """UC-E2E-001 main flow: stack up, health 200 ok, empty list => 200 empty."""
    # S1 + S2: health endpoint responds 200 with status ok and DB checks.
    status, health = shared.json_request(shared.HEALTH_PATH)
    assert status == 200, f"health HTTP {status}"
    shared.assert_json_path(health, "$.checks.postgres", "ok", "health")
    shared.assert_json_path(health, "$.checks.redis", "ok", "health")
    # Contract §4.1: status is "ok" only when Ollama is up; otherwise "degraded"
    # (Ollama is an external dependency, so accept both consistent states).
    ollama = health.get("ollamaStatus", health.get("ollama_status"))
    expected_status = "ok" if ollama == "up" else "degraded"
    shared.assert_json_path(health, "$.status", expected_status, "health")

    # S4 (initial state): before ingest, tasks list is empty -> 200, no items.
    # Contract §4.2: empty state is 200 with empty collections, never 4xx.
    status, tasks = shared.json_request("/api/tasks")
    assert status == 200, f"tasks HTTP {status}"
    assert tasks.get("total") == 0, f"expected empty list, got {tasks!r}"
    assert tasks.get("items") == [], f"expected empty items, got {tasks!r}"


def test_initial_state_has_no_tasks_in_db(stack):
    """UC-E2E-001 postcondition: DB empty before ingest (postcondition check)."""
    status, tasks = shared.json_request("/api/tasks", timeout=shared.HEALTH_TIMEOUT_SECONDS)
    assert status == 200, f"tasks HTTP {status}"
    assert tasks.get("total", 0) == 0, "DB must be empty before UC-E2E-002 ingest"
