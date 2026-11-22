import json, urllib.request
def get(url):
    return json.load(urllib.request.urlopen(url, timeout=5))

d = get("http://127.0.0.1:8080/api/tasks")
print("total", d.get("total"))
for t in d.get("items", []):
    print(t.get("task_id"), t.get("started_at"), t.get("finished_at"), t.get("models"))

for view, date, args in [("week","2026-11-09","?view=week&date=2026-11-09"),
                          ("day","2026-11-11","?view=day&date=2026-11-11"),
                          ("month","2026-11-01","?view=month&date=2026-11-01")]:
    try:
        b = get("http://127.0.0.1:8080/api/calendar"+args)
    except Exception as e:
        print(view, args, "ERR", e); continue
    days = b.get("days", [])
    print(view, "days", len(days), "| cells:", [(x.get("date"), [y.get("task_id") for y in x.get("tasks",[])]) for x in days])
