# -*- coding: utf-8 -*-
"""Package-level fixtures for UC-E2E-001..003 (T-009, I-02).

``conftest.py`` in this package supplies the shared ``stack`` fixture used by
the UC-E2E-002 and UC-E2E-003 modules. UC-E2E-001 keeps its own module-level
``stack`` fixture (empty-state gate) which overrides this one, so the initial
"no tasks" assertions still run before any seed is applied.

The fixture is deliberately live-gated: when the isolated local stack is not
healthy the dependent tests SKIP with a clear reason (they do not error), so a
stack-down machine still produces a deterministic, honest run.
"""

from __future__ import annotations

import time

import pytest

import conftest as c
from fixtures import seed

SEED_TASK_COUNT = 5
SEED_WAIT_SECONDS = 120
SEED_POLL_SECONDS = 3


@pytest.fixture(scope="session")
def stack():
    """Health-gate the stack, seed the deterministic corpus, wait for ingest."""
    base_url = c.DEFAULT_BASE_URL
    if not c.stack_is_up(base_url):
        pytest.skip("local stack unhealthy - live E2E requires docker compose up")

    seed.seed_dir()

    deadline = time.monotonic() + SEED_WAIT_SECONDS
    last_total = None
    while time.monotonic() < deadline:
        status, body = c.json_request("/api/tasks", timeout=15)
        if status == 200 and isinstance(body, dict):
            last_total = body.get("total")
            if last_total == SEED_TASK_COUNT:
                return base_url
        time.sleep(SEED_POLL_SECONDS)

    pytest.fail(
        "ingest did not expose %d tasks within %ds (last total=%r); "
        "check the opencode seed dir bind-mount and the ingest poller"
        % (SEED_TASK_COUNT, SEED_WAIT_SECONDS, last_total)
    )
