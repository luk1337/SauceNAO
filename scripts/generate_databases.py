#!/usr/bin/env python3
import lxml.html
import os
import re
import urllib.request

root = lxml.html.fromstring(urllib.request.urlopen('https://saucenao.com').read())

databases = []

for child in root.findall('.//input[@name="dbs[]"]'):
    text, value = child.tail, int(child.get('value'))

    if value == 999:
        continue

    databases.append((text, value))

assert(len(databases) > 0)
databases.sort(key=lambda tup: tup[0].lower())

v = [x[1] for x in databases]

with open(os.path.join(os.path.dirname(os.path.realpath(__file__)), '../app/src/main/res/values/arrays.xml'), 'r+') as f:
    data = f.read()

    databases_entries = '<string-array name="databases_entries">'

    for k in [x[0] for x in databases]:
        databases_entries += f'\n        <item>{k}</item>'

    databases_entries += '\n    </string-array>'
    data = re.sub(r'<string-array name="databases_entries">[^>]*>[^~]*?<\/string-array>', databases_entries, data)

    databases_values = '<integer-array name="databases_values">'

    for v in [x[1] for x in databases]:
        databases_values += f'\n        <item>{v}</item>'

    databases_values += '\n    </integer-array>'
    data = re.sub(r'<integer-array name="databases_values">[^>]*>[^~]*?<\/integer-array>', databases_values, data)

    f.seek(0)
    f.write(data)
    f.truncate()
