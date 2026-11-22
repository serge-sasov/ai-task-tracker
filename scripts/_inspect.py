# -*- coding: utf-8 -*-
import yaml, json, sys
from pathlib import Path

root = Path(__file__).parent.parent
doc = root / "docs" / "analysis" / "e2e-test-data-ai-task-tracker.yaml"
data = yaml.safe_load(doc.read_text(encoding="utf-8-sig"))

cal = data["fixtures"]["calendar"]
print("=== calendar fixture keys ===")
print(list(cal.keys()))

for key in ["day_2026-11-11-models", "day_2026-11-10-models"]:
    if key in cal:
        print(f"\n=== {key} ===")
        print(json.dumps(cal[key], ensure_ascii=False, indent=2))

print("\n=== sessions (summary) ===")
sessions = data["fixtures"]["sessions"]
for s in sessions:
    usage = s.get("usage")
    models = []
    if usage:
        for u in usage:
            models.append((u.get("model"), u.get("input_tokens"), u.get("output_tokens"), u.get("cache_tokens")))
    print(f"{s['session_id']} task={s.get('task_id')} status={s.get('final_status')} usage={models}")
