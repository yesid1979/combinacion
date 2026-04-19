import zipfile, re, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

original = content

# Replace _______ inside <w:t> tags specifically when related to Modification No.
# Let's just find the <w:t> elements with underscores and see the context.
matches = list(re.finditer(r'<w:t(?:[^>]*)>(\s*_{3,10}\s*)</w:t>', content))
count = 0
offset = 0

for m in matches:
    sub = m.group(1)
    start_pos = m.start() + offset
    ctx_back = content[max(0, start_pos - 150):start_pos].lower()
    
    # Check if 'odificaci' is in the near context, OR 'no.'
    if 'odific' in ctx_back or 'no.' in ctx_back:
        new_tag = m.group(0).replace(sub, '001')
        content = content[:start_pos] + new_tag + content[m.end() + offset:]
        offset += len(new_tag) - len(m.group(0))
        count += 1
        print(f"Replaced '{sub}' at {start_pos}")

print(f"Total replaced: {count}")

if count > 0:
    all_files['word/document.xml'] = content.encode('utf-8')
    tmp = src + '.tmp'
    with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
        for name, data in all_files.items():
            zout.writestr(name, data)
    os.replace(tmp, src)
    print("Saved modified docx.")
