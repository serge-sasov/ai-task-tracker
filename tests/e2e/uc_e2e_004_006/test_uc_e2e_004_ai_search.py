# -*- coding: utf-8 -*-
"""UC-E2E-004: AI search (POST /api/search) — ai-task-tracker (T-009, I-02).

Contract §4.4, dataset fixture ``fixtures.ai_search``. Live tests run against
the ISOLATED LOCAL stack only (127.0.0.1 via stdlib urllib); no production
endpoints, no secrets, no external network. Stdlib only.

Collection-only safe: the ``stack`` fixture (module-scoped) skips when the
stack is unhealthy, so the module collects cleanly while the stack is down.
AI-search tests additionally skip when the backend cannot reach Ollama (an
external dependency). Assertions are against the
documented contract / dataset, not implementation.
"""

from __future__ import annotations

import json
import time
import urllib.error
import urllib.request

import pytest

from uc_e2e_001_003 import shared

REQUIRED_TIMEOUT_SECONDS = 15  # R-023: AI search answer < 15 s


def _post_json(path: str, payload: dict) -> tuple[int, dict]:
    """POST JSON to the backend; return (status, parsed_dict|{'_raw':..})."""
    url = f"{shared.BASE_URL}{path}"
    data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        url, data=data, method="POST",
        headers={"Content-Type": "application/json", "Accept": "application/json"},
    )
    try:
        with urllib.request.urlopen(req, timeout=REQUIRED_TIMEOUT_SECONDS) as resp:
            body = resp.read().decode("utf-8", "replace")
            return resp.status, json.loads(body) if body.strip() else {}
    except urllib.error.HTTPError as exc:
        try:
            body = exc.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        try:
            return exc.code, json.loads(body) if body.strip() else {}
        except Exception:
            return exc.code, {"_raw": body[:400]}


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


@pytest.fixture(scope="module")
def ollama(stack):
    """Skip AI-search tests when the backend cannot reach Ollama."""
    import conftest as c

    status, health = c.json_request(c.HEALTH_PATH, timeout=10)
    ollama_status = None
    if status == 200 and isinstance(health, dict):
        ollama_status = health.get("ollamaStatus", health.get("ollama_status"))
    if ollama_status != "up":
        pytest.skip("Ollama unavailable from the backend - AI search requires it")
    return True


# --------------------------------------------------------------------------- #
# UC-E2E-004
# --------------------------------------------------------------------------- #

def test_semantic_query_top_k(stack, ollama):
    """Relevant semantic query returns the expected task in top-K (S1)."""
    fixtures = shared.dataset()["fixtures"]["ai_search"]
    term = fixtures["semantic_queries"][0]["query"]
    expected_ids = fixtures["semantic_queries"][0]["must_be_in_top_k"]
    body = _post_json("/api/search", {"query": term, "limit": fixtures["top_k_default"]})
    assert body["query"] == term, "query echoes the request payload"
    results = body.get("results", [])
    assert isinstance(results, list) and len(results) >= 1, "results list non-empty"
    # Ordered by score descending.
    scores = [r.get("score") for r in results]
    assert scores == sorted(scores, reverse=True), "results ordered by score desc"
    # Every result carries a non-empty snippet.
    for r in results:
        snippet = r.get("snippet")
        assert snippet and snippet.strip(), f"empty snippet for {r.get('task_id')}"
    # At least one result is a task the dataset expects in top-K.
    found = {r["task_id"] for r in results}
    assert found.intersection(expected_ids), (
        f"expected {expected_ids} in top-K, got {sorted(found)}"
    )


def test_unrelated_query_empty(stack, ollama):
    """Unrelated query returns an honest empty results list (S4)."""
    term = shared.dataset()["fixtures"]["ai_search"]["unrelated_query"]["query"]
    body = _post_json("/api/search", {"query": term, "limit": 5})
    assert body.get("results") == [], f"unrelated query returned non-empty: {body}"


def test_empty_query_client_validation(stack):
    """Empty/blank query is rejected by client validation (S5)."""
    for term in ("", "   ", "\t"):
        status, body = _post_json("/api/search", {"query": term, "limit": 5})
        assert status == 400, f"blank query {term!r} expected 400, got {status}"
        code = body.get("code") if isinstance(body, dict) else None
        msg = body.get("message", "") if isinstance(body, dict) else ""
        assert code == "INVALID_QUERY", f"expected INVALID_QUERY, got {code!r}"
        assert "Введите запрос" in msg, f"missing client message, got {msg!r}"


def test_response_time_bound(stack, ollama):
    """A broad search completes under the documented 15 s bound (R-023)."""
    term = shared.dataset()["fixtures"]["ai_search"]["semantic_queries"][0]["query"]
    start = time.monotonic()
    body = _post_json("/api/search", {"query": term, "limit": 5})
    elapsed = time.monotonic() - start
    assert elapsed < REQUIRED_TIMEOUT_SECONDS, f"search too slow: {elapsed:.2f}s"
    assert isinstance(body.get("results"), list), "results is a list"


def test_english_query_valid_200(stack, ollama):
    """An English query returns 200 with a valid results list (S1)."""
    status, body = _post_json("/api/search", {"query": "CI setup pipeline", "limit": 5})
    assert status == 200, f"English query HTTP {status}: {body.get('_raw')!r}"
    assert isinstance(body.get("results"), list), "results must be a list"
    for r in body.get("results", []):
        assert r.get("task_id") and isinstance(r["task_id"], str)
