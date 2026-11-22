# -*- coding: utf-8 -*-
import yaml, json
with open('docs/analysis/e2e-test-data-ai-task-tracker.yaml', encoding='utf-8') as f:
    d = yaml.safe_load(f)
sess = d['fixtures']['sessions']
print('count', len(sess))
print('keys', sorted(sess[0].keys()))
for s in sess:
    print(s['session_id'], s['task_id'], s['started_at'], s['usage'] is None)
