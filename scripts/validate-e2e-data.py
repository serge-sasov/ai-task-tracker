#!/usr/bin/env python3
"""Validate a harness-e2e/1 E2E dataset (standalone task artifact).

Checks schema/version, use_cases ids (UC-E2E-\\d{3}), per-use-case steps
(action), requirements (requirements OR source_requirements), cleanup,
traceability.test_ids, requirement ids (R-\\d{3}), fixture anchors, and an
optional cross-check against a Markdown use-cases document.

Exit codes: 0 = no errors, 1 = validation errors, 2 = usage/dependency error.
"""
import argparse
import re
import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    print("ERROR: PyYAML is required (pip install pyyaml)", file=sys.stderr)
    sys.exit(2)

UC_RE = re.compile(r"UC-E2E-\d{3}")
REQ_RE = re.compile(r"R-\d{3}")
ANCHOR_RE = re.compile(r"#fixtures\.([A-Za-z0-9_.-]+)")
SCHEMA = "harness-e2e/1"


def collect_strings(value):
    if isinstance(value, str):
        yield value
    elif isinstance(value, dict):
        for item in value.values():
            yield from collect_strings(item)
    elif isinstance(value, list):
        for item in value:
            yield from collect_strings(item)


def main():
    parser = argparse.ArgumentParser(description="Validate a harness-e2e/1 dataset.")
    parser.add_argument("yaml_path", nargs="?",
                        help="Path to the harness-e2e/1 dataset YAML.")
    parser.add_argument("--use-cases-md", default=None,
                        help="Markdown use-cases document for id/requirement cross-check.")
    parser.add_argument("--strict", action="store_true",
                        help="Treat requirement-id cross-check gaps as errors.")
    args = parser.parse_args()

    if not args.yaml_path:
        args.yaml_path = str(Path(__file__).resolve().parent.parent /
                             "docs" / "analysis" / "e2e-test-data-ai-task-tracker.yaml")

    errors = []
    warnings = []
    yaml_ids = set()
    yaml_reqs = set()

    path = Path(args.yaml_path)
    data = None
    if not path.exists():
        errors.append(f"ERROR: YAML not found: {path}")
    else:
        try:
            data = yaml.safe_load(path.read_text(encoding="utf-8-sig"))
        except yaml.YAMLError as exc:
            errors.append(f"ERROR: YAML parse error: {exc}")
        except OSError as exc:
            errors.append(f"ERROR: cannot read YAML: {exc}")

    if data is not None:
        if not isinstance(data, dict):
            errors.append("ERROR: top-level YAML must be a mapping")
        else:
            schema = data.get("schema")
            if schema != SCHEMA:
                errors.append(f"ERROR: schema must be {SCHEMA!r}, got {schema!r}")
            if "version" not in data or data.get("version") is None:
                errors.append("ERROR: missing 'version' field")

            use_cases = data.get("use_cases")
            if not isinstance(use_cases, list) or not use_cases:
                errors.append("ERROR: 'use_cases' must be a non-empty list")
            else:
                for index, use_case in enumerate(use_cases):
                    if not isinstance(use_case, dict):
                        errors.append(f"ERROR: use_cases[{index}] must be a mapping")
                        continue
                    uid = str(use_case.get("id", ""))
                    if not uid or not UC_RE.fullmatch(uid):
                        errors.append(f"ERROR: use_cases[{index}].id is not {UC_RE.pattern}: {uid!r}")
                    if uid and uid in yaml_ids:
                        errors.append(f"ERROR: duplicate use case id: {uid}")
                    if uid:
                        yaml_ids.add(uid)

                    requirements = use_case.get("requirements")
                    if requirements is None:
                        requirements = use_case.get("source_requirements")
                    if not isinstance(requirements, list) or not requirements:
                        warnings.append(f"WARN: {uid} or use_cases[{index}]: no requirements/source_requirements")
                    else:
                        for item in requirements:
                            req = str(item)
                            if not REQ_RE.fullmatch(req):
                                errors.append(f"ERROR: {uid}: invalid requirement id {req!r}")
                            yaml_reqs.add(req)

                    steps = use_case.get("steps")
                    if not isinstance(steps, list) or not steps:
                        errors.append(f"ERROR: {uid}: steps must be a non-empty list")
                    else:
                        for step_index, step in enumerate(steps):
                            if not isinstance(step, dict):
                                errors.append(f"ERROR: {uid}: steps[{step_index}] must be a mapping")
                                continue
                            if not step.get("action"):
                                errors.append(f"ERROR: {uid}: steps[{step_index}].action is missing or empty")
                            if "expected" not in step:
                                warnings.append(f"WARN: {uid}: steps[{step_index}] has no expected")

                    if not use_case.get("cleanup"):
                        warnings.append(f"WARN: {uid}: cleanup is missing or empty")
                    traceability = use_case.get("traceability")
                    test_ids = traceability.get("test_ids") if isinstance(traceability, dict) else None
                    if not test_ids:
                        warnings.append(f"WARN: {uid}: traceability.test_ids is missing or empty")

            fixtures = data.get("fixtures")
            if fixtures is not None and not isinstance(fixtures, dict):
                errors.append("ERROR: 'fixtures' must be a mapping")
            top_keys = set(str(k) for k in fixtures) if isinstance(fixtures, dict) else set()
            for text in collect_strings(data):
                for match in ANCHOR_RE.finditer(text):
                    anchor = match.group(1)
                    if anchor.split(".")[0] not in top_keys:
                        errors.append(f"ERROR: unresolved fixture anchor: #fixtures.{anchor}")

    if args.use_cases_md:
        md_path = Path(args.use_cases_md)
        if not md_path.exists():
            errors.append(f"ERROR: use-cases Markdown not found: {md_path}")
        else:
            try:
                md_text = md_path.read_text(encoding="utf-8-sig")
            except OSError as exc:
                errors.append(f"ERROR: cannot read Markdown: {exc}")
            else:
                md_ids = set(UC_RE.findall(md_text))
                missing_ids = sorted(md_ids - yaml_ids)
                extra_ids = sorted(yaml_ids - md_ids)
                if missing_ids:
                    errors.append(f"ERROR: UC ids in Markdown but not YAML: {', '.join(missing_ids)}")
                if extra_ids:
                    errors.append(f"ERROR: UC ids in YAML but not Markdown: {', '.join(extra_ids)}")
                md_reqs = set(REQ_RE.findall(md_text))
                missing_reqs = sorted(md_reqs - yaml_reqs)
                if missing_reqs:
                    target = errors if args.strict else warnings
                    target.append(f"WARN: requirements in Markdown but not YAML: {', '.join(missing_reqs)}")

    for line in warnings:
        print(line)
    for line in errors:
        print(line)
    print(f"checked {len(yaml_ids)} use case(s), {len(yaml_reqs)} requirement link(s), "
          f"{len(errors)} error(s), {len(warnings)} warning(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
