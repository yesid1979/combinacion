import zipfile, re, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

count = 0
# search for 'odificaci\xf3n' or 'odificaci'
idx = 0
while True:
    idx = content.lower().find('odificaci', idx)
    if idx == -1:
        break
    
    # look 300 chars forward for 'no.'
    idx_no = content.lower().find('no.', idx, idx + 300)
    if idx_no != -1:
        # look 300 chars forward for underscores
        m = re.search(r'<w:t(?:[^>]*)>(\s*_{4,10}\s*)</w:t>', content[idx_no:idx_no+400])
        if m:
            sub = m.group(1)
            # Only replace if we haven't already passed another 'odific'
            actual_start = idx_no + m.start(1)
            content = content[:actual_start] + '001' + content[actual_start+len(sub):]
            count += 1
            print(f'Replaced underscores at {actual_start} with 001')
            idx = actual_start + 3
            continue
    idx += 10

print(f"Total replaced: {count}")

if count > 0:
    all_files['word/document.xml'] = content.encode('utf-8')
    tmp = src + '.tmp'
    with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
        for name, data in all_files.items():
            zout.writestr(name, data)
    os.replace(tmp, src)
    print("Saved modified docx.")
