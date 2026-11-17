# -*- coding: utf-8 -*-
"""UC-E2E-007..008 test collection for ai-task-tracker (T-009, I-02).

Cross-use-case e2e tests, run against the ISOLATED LOCAL stack only
(127.0.0.1; no production endpoints):

* UC-E2E-007 — Reliability: service restart + idempotent re-sync
  (``test_uc_e2e_007_restart_idempotency.py``)
* UC-E2E-008 — Manual history deletion via ``DELETE /api/history``
  (``test_uc_e2e_008_manual_deletion.py``)

These two use cases are the most environment-dependent in the suite: UC-E2E-007
drives ``docker compose restart`` plus a re-sync, and UC-E2E-008 mutates the
service database. They run inside ``module``-scoped ``stack`` fixtures that gate
on a live local stack, and restore state in teardown (re-seed or
``docker compose down -v`` + re-import) so later batches are never poisoned.
Later batches are therefore gated behind a live stack too.

Reuse of the deterministic fixture loader and JSON-path helpers lives in the
sibling package ``uc_e2e_001_003`` (C3) — imported with the absolute name
because ``tests/e2e`` is not itself a package (prepend import mode inserts it
onto sys.path, so both sibling packages resolve top-level). A tiny local
``helpers`` module provides the docker restart + re-seed + delete helpers that
the shared helpers do not (no sibling owns compose mutation).

Stdlib only. No secrets, no external networks; traffic is to 127.0.0.1.
"""
