# -*- coding: utf-8 -*-
"""Regression test for BUG-002: Docker backend source paths (T-013).

The backend image build copies ``backend/pom.xml`` to the image and then runs
Maven. ``backend/pom.xml`` declares its sources/resources relative to the pom's
basedir (``${project.basedir}/../src/main/java``). This test asserts that the
Dockerfile copies each source tree to exactly the absolute path the pom
expects, so Maven finds the sources and ``mvn package`` can produce a jar.

Static and offline: it parses the Dockerfile and the pom, no Docker required.
It fails on the buggy Dockerfile (WORKDIR /app + COPY to /app/src/... while the
pom expects /src/...) and passes after the fix.
"""

from __future__ import annotations

import posixpath
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DOCKERFILE = ROOT / "Dockerfile"
POM = ROOT / "backend" / "pom.xml"


def _pom_source_and_resources() -> tuple[str, str]:
    """Return (sourceDirectory, resources.directory) expressions from the pom."""
    text = POM.read_text(encoding="utf-8")
    source = re.search(r"<sourceDirectory>\s*([^<]+?)\s*</sourceDirectory>", text)
    resource = re.search(
        r"<resources>.*?<directory>\s*([^<]+?)\s*</directory>", text, re.S
    )
    assert source, "backend/pom.xml has no <sourceDirectory>"
    assert resource, "backend/pom.xml has no <resources><directory>"
    return source.group(1), resource.group(1)


def _build_stage(text: str) -> str:
    """Return the text of the ``... AS build`` stage up to the next FROM."""
    start = re.search(r"^FROM\s+.+?\s+AS\s+build\s*$", text, re.M)
    assert start, "Dockerfile has no build stage (FROM ... AS build)"
    rest = text[start.end():]
    nxt = re.search(r"^FROM\s+", rest, re.M)
    return rest[: nxt.start()] if nxt else rest


def _workdir(stage: str) -> str:
    match = re.search(r"^WORKDIR\s+(\S+)", stage, re.M)
    assert match, "Dockerfile build stage has no WORKDIR"
    return match.group(1).rstrip("/") or "/"


def _resolve_in_image(workdir: str, expr: str) -> str:
    expr = expr.strip().replace("${project.basedir}", workdir)
    return posixpath.normpath(expr)


def _copy_destinations(stage: str) -> dict[str, set[str]]:
    """Map COPY source -> absolute destinations inside the build stage image."""
    workdir = _workdir(stage)
    copies: dict[str, set[str]] = {}
    for src, dest in re.findall(r"^COPY\s+(\S+)\s+(\S+)\s*$", stage, re.M):
        absolute = dest if dest.startswith("/") else posixpath.normpath(
            posixpath.join(workdir, dest)
        )
        copies.setdefault(src, set()).add(absolute)
    return copies


def test_dockerfile_copies_sources_where_pom_expects() -> None:
    """Sources/resources must land at the absolute paths the pom resolves."""
    stage = _build_stage(DOCKERFILE.read_text(encoding="utf-8"))
    workdir = _workdir(stage)
    source_expr, resource_expr = _pom_source_and_resources()
    expected_source = _resolve_in_image(workdir, source_expr)
    expected_resource = _resolve_in_image(workdir, resource_expr)
    copies = _copy_destinations(stage)

    assert expected_source in copies.get("src/main/java", set()), (
        "Dockerfile must COPY src/main/java to %s (pom sourceDirectory); got %r"
        % (expected_source, sorted(copies.get("src/main/java", set())))
    )
    assert expected_resource in copies.get("src/main/resources", set()), (
        "Dockerfile must COPY src/main/resources to %s (pom resources); got %r"
        % (expected_resource, sorted(copies.get("src/main/resources", set())))
    )


def test_runtime_stage_copies_the_built_jar() -> None:
    """The runtime stage must copy the jar the build stage actually produces."""
    text = DOCKERFILE.read_text(encoding="utf-8")
    workdir = _workdir(_build_stage(text))
    expected_prefix = posixpath.normpath(workdir + "/target") + "/"
    match = re.search(r"^COPY\s+--from=build\s+(\S+)\s+(\S+)\s*$", text, re.M)
    assert match, "runtime stage must COPY --from=build the jar"
    jar_source = posixpath.normpath(match.group(1))
    assert jar_source.startswith(expected_prefix), (
        "runtime jar source %r must live under %s" % (jar_source, expected_prefix)
    )
