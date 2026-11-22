# -*- coding: utf-8 -*-
"""Probe the built frontend bundle to determine guard chunk discovery."""
import re, json, sys, os
from urllib.request import urlopen
from urllib.error import URLError

BASE = "http://127.0.0.1:8081"
def fetch(path):
    try:
        with urlopen(BASE + path, timeout=10) as r:
            return r.status, r.read()
    except (URLError, OSError) as e:
        return None, repr(e)

_, html = fetch("/")
assert html, "no html"
main_bundles = re.findall(r"""<script[^>]+\bsrc="([^"]+)"[^>]*>""", html.decode("utf-8"))
print("main script refs:", main_bundles[:10])

def main_probe():
    # Search the MAIN bundle for how it references the ModelCell chunk.
    st, main = fetch("/assets/index-_avqqFjt.js")
    if st != 200:
        print("could not fetch main bundle")
        return
    m = main.decode("utf-8", errors="replace")
    # Find chunk specifier: ModelCell-<hash>.js
    import re
    chunk = re.search(r'ModelCell-[^"\\]{4,}\.js', m)
    print("chunk specifier in main bundle:", chunk.group(0) if chunk else "NOT FOUND")
    if chunk:
        print("context:", m[chunk.start()-40:chunk.end()+40])

main_probe()

# Find where model-cell appears and how much context
for href in main_bundles:
    st, body = fetch(href)
    if st == 200:
        decoded = body.decode("utf-8", errors="replace")
        idx = decoded.find("model-cell")
        if idx != -1:
            print(f"found model-cell in {href} at {idx}")
            print("context:", decoded[max(0,idx-80):idx+80])
            break
else:
    print("model-cell not in main bundle")
