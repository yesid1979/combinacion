import zipfile, re, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

count = 0
new_content = ""
idx = 0
while True:
    found = content.lower().find('odificaci', idx)
    if found == -1:
        new_content += content[idx:]
        break
    
    # Check if 'no.' is nearby
    no_found = content.lower().find('no.', found, found + 300)
    if no_found != -1:
        chunk = content[no_found:no_found+400]
        # Look for underscores
        m = re.search(r'<w:t(?:[^>]*)>(\s*_{4,10}\s*)</w:t>', chunk)
        if m:
            sub = m.group(1)
            replaced = chunk.replace(sub, '001', 1)
            new_content += content[idx:no_found] + replaced
            idx = no_found + len(chunk)
            count += 1
            print(f'Replaced underscores "{sub}" near pos {found}')
            continue
    
    # advance to next
    new_content += content[idx:found+10]
    idx = found + 10

if count > 0:
    all_files['word/document.xml'] = new_content.encode('utf-8')
    tmp = src + '.tmp'
    with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
        for name, data in all_files.items():
            zout.writestr(name, data)
    os.replace(tmp, src)
    print("Saved modified docx.")
