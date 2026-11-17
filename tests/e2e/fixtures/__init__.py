# -*- coding: utf-8 -*-
"""Suite-level, idempotent, loopback-only seed-and-cleanup fixtures (T-009, I-02).

This package is the single reusable source of truth for two things C7 (and any
future E2E run) needs:

* :func:`seed.apply` / :func:`seed.seed_dir` materialise the deterministic
  ``fixtures.sessions`` rows from the dataset into a source directory, wiping
  any pre-existing ``*.json`` first so a run is fully idempotent. The source
  directory is the opencode data dir (the backend binds it READ-ONLY, so the run
  must never mutate it), which :func:`cleanup.source_untouched` verifies after
  the run.
* :func:`cleanup.cleanup` performs the loopback-only ``DELETE /api/history``
  confirmation the whole suite relies on to reset the local stack between runs.

Design constraints (all enforced by the machine checks for this chunk):

* Import-safe: no disk, network, or environment mutation happens at import
  time. Every entry point is a plain function.
* Stdlib + pyyaml only (no ``requests`` / ``httpx``). HTTP uses ``urllib``.
* Loopback only: :func:`cleanup.cleanup` rejects any host that is not
  ``127.0.0.1`` / ``localhost`` / ``::1`` and never touches a production endpoint.
* Idempotent: :func:`seed.apply` and :func:`cleanup.cleanup` both yield the
  same result when called repeatedly.

No production or external host tokens appear anywhere in this package.
"""

from __future__ import annotations

from . import cleanup, seed

__all__ = [
    "seed",
    "cleanup",
    "build_corpus",
    "apply",
    "expected_manifest",
    "verify",
    "seed_dir",
    "cleanup_history",
    "source_untouched",
]


def build_corpus(data=None):
    """Re-export :func:`seed.build_corpus` (see its docstring for the contract)."""
    return seed.build_corpus(data)


def apply(target):
    """Re-export :func:`seed.apply`."""
    return seed.apply(target)


def expected_manifest(data=None):
    """Re-export :func:`seed.expected_manifest`."""
    return seed.expected_manifest(data)


def verify(target):
    """Re-export :func:`seed.verify`."""
    return seed.verify(target)


def seed_dir(target=None):
    """Re-export :func:`seed.seed_dir`."""
    return seed.seed_dir(target)


def cleanup_history(base_url=None):
    """Re-export :func:`cleanup.cleanup` (history-removal entry point)."""
    return cleanup.cleanup(base_url)


def source_untouched(target=None):
    """Re-export :func:`cleanup.source_untouched`."""
    return cleanup.source_untouched(target)
