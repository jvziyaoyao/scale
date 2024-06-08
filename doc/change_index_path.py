import os

target_file = 'docs/index.md'

with open(target_file, 'r') as src:
    content = src.read()

content = content.replace('doc/docs/image/', 'image/')
content = content.replace('/CHANGELOG.md', 'change_log.md/')

with open(target_file, 'w') as tgt:
    tgt.write(content)