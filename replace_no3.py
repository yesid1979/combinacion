import zipfile, re, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

# the user wants text like "______001" or just "001".
# we will replace sequence of `____` inside `<w:t>` with `001` directly if preceded by 'no.'
# Let's iterate backwards so indices don't shift.

idxs = [m.start() for m in re.finditer('odificaci', content.lower())]
count = 0

for idx in reversed(idxs):
    idx_no = content.lower().find('no.', idx, idx + 300)
    if idx_no != -1:
        m = re.search(r'<w:t(?:[^>]*)>(\s*_{4,15}\s*)</w:t>', content[idx_no:idx_no+400])
        if m:
            sub = m.group(1)
            actual_start = idx_no + m.start(1)
            content = content[:actual_start] + '001' + content[actual_start+len(sub):]
            count += 1
            print(f'Replaced underscores at {actual_start} with 001')

print(f"Total replaced: {count}")

if count > 0:
    all_files['word/document.xml'] = content.encode('utf-8')
    tmp = src + '.tmp'
    with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
        for name, data in all_files.items():
            zout.writestr(name, data)
    os.replace(tmp, src)
    print("Saved modified docx.")
