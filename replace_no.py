import zipfile, re, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

count = 0
matches = list(re.finditer(r'<w:t(?: xml:space=\"preserve\")?>(\s*_{4,10}\s*)</w:t>', content))
offset = 0

for m in matches:
    sub = m.group(1)
    start_pos = m.start() + offset
    ctx_back = content[max(0, start_pos - 150):start_pos].lower()
    
    if 'odific' in ctx_back or 'no.' in ctx_back:
        new_tag = m.group(0).replace(sub, '{{NUMERO_MODIFICACION}}')
        content = content[:start_pos] + new_tag + content[m.end() + offset:]
        offset += len(new_tag) - len(m.group(0))
        count += 1
        print(f"Replaced '{sub}'")

print(f"Total replaced: {count}")

if count > 0:
    all_files['word/document.xml'] = content.encode('utf-8')
    tmp = src + '.tmp'
    with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
        for name, data in all_files.items():
            zout.writestr(name, data)
    os.replace(tmp, src)
    print("Saved modified docx.")
