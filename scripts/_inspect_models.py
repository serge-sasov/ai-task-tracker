import yaml, json
d = yaml.safe_load(open('docs/analysis/e2e-test-data-ai-task-tracker.yaml', encoding='utf-8-sig'))
cf = d['datasets']['e2e']['fixtures']['calendar']
for k in ['day_2026-11-11-models', 'day_2026-11-10-models']:
    print('=== ' + k + ' ===')
    print(json.dumps(cf[k], ensure_ascii=False, indent=2))
