import urllib.request
for url in ["http://127.0.0.1:8081/", "http://127.0.0.1:8081/assets/index-_avqqFjt.js"]:
    try:
        with urllib.request.urlopen(url, timeout=15) as r:
            body = r.read().decode("utf-8", "replace")
            print(f"OK {url}: len={len(body)}")
            for m in ["Модель", "Нет данных", "model-cell"]:
                print(f"  marker {m!r}: count={body.count(m)}")
    except Exception as e:
        print(f"ERR {url}: {e}")
