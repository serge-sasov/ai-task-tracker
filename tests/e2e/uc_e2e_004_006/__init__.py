# -*- coding: utf-8 -*-
"""UC-E2E-004..006 test collection for ai-task-tracker (T-009, I-02).

Cross-use-case e2e tests, run against the ISOLATED LOCAL stack only
(127.0.0.1; no production endpoints):

* UC-E2E-004 — AI search (POST /api/search)
* UC-E2E-005 — Calendar view (GET /api/calendar)
* UC-E2E-006 — Token statistics (GET /api/tokens)

Reuse of the deterministic fixture loader and duration parser lives in the
sibling package ``uc_e2e_001_003`` (C3) — imported with the absolute name
because ``tests/e2e`` itself is not a package (prepend import mode inserts it
onto sys.path, so both sibling packages resolve top-level).

Stdlib only. No secrets, no external networks; traffic is to 127.0.0.1.
"""
