#!/usr/bin/env python3
import lxml.html
import urllib.request

root = lxml.html.fromstring(urllib.request.urlopen('https://saucenao.com').read())

databases = []

for child in root.findall('.//input[@name="dbs[]"]'):
    text, value = child.tail, int(child.get('value'))

    if value == 999:
        continue

    databases.append((text, value))

databases.sort(key=lambda tup: tup[0].lower())
databases.insert(0, ('All databases', 999))

v = [x[1] for x in databases]

print('<string-array name="databases_entries">')

for k in [x[0] for x in databases]:
    print(f'    <item>{k}</item>')

print('</string-array>')

print('')

print('<integer-array name="databases_values">')

for v in [x[1] for x in databases]:
    print(f'    <item>{v}</item>')

print('</integer-array>')
