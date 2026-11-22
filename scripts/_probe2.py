import urllib.request, re

def get(path):
    with urllib.request.urlopen(path, timeout=10) as r:
        return r.read()

html = get('http://127.0.0.1:8081/assets/index-_avqqFjt.js').decode('utf-8','replace')
print('model-cell count in main:', html.count('model-cell'))

# find chunk refs
refs = re.findall(r'assets/ModelCell-[^"\\]{4,}\.js', html)
print('chunk refs:', refs)

for ref in set(refs):
    try:
        body = ('http://127.0.0.1:8081/' + ref).replace('\\\\','/')
        with urllib.request.urlopen(body, timeout=10) as r:
            t = r.read().decode('utf-8','replace')
        print(ref, '-> model-cell count:', t.count('model-cell'))
    except Exception as e:
        print(ref, 'ERR', e)
