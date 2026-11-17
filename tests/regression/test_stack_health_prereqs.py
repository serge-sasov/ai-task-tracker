# -*- coding: utf-8 -*-
"""Regression test for BUG-003: local stack health prerequisites (T-015).

The local stack cannot become healthy when either prerequisite is missing:

1. Flyway 10 modularised database support: ``flyway-core`` alone does not
   recognise PostgreSQL 16, so the backend crashes on startup with
   ``FlywayException: Unsupported Database: PostgreSQL 16.16``. The
   ``flyway-database-postgresql`` provider must be on the classpath.
2. The backend host port must be overridable, so the e2e stack can run when
   another local service already holds 8080 (``E2E_BACKEND_PORT``).

Static and offline: parses ``backend/pom.xml`` and ``docker-compose.yml``.
Fails on the base revision, passes after the fix.
"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
POM = ROOT / "backend" / "pom.xml"
COMPOSE = ROOT / "docker-compose.yml"


def test_flyway_postgresql_provider_is_declared() -> None:
    """backend/pom.xml must include the Flyway PostgreSQL database module."""
    text = POM.read_text(encoding="utf-8")
    assert "flyway-database-postgresql" in text, (
        "backend/pom.xml must declare org.flywaydb:flyway-database-postgresql; "
        "flyway-core 10.x alone rejects PostgreSQL 16 at startup"
    )


def test_backend_host_port_is_overridable() -> None:
    """docker-compose.yml must not hardcode the backend host port 8080."""
    text = COMPOSE.read_text(encoding="utf-8")
    backend = text.split("frontend:", 1)[0]
    ports = re.search(r"ports:\s*\n((?:\s*-\s*[^\n]+\n)+)", backend)
    assert ports, "backend service must publish a port"
    published = ports.group(1)
    assert "E2E_BACKEND_PORT" in published, (
        "backend port must be overridable via ${E2E_BACKEND_PORT:-8080}; "
        "hardcoded 8080 cannot bind when another local service holds it. "
        "Got: %r" % published.strip()
    )
