import zipfile, re, os, shutil

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

original = content

# Replace 4 to 8 underscores with {{NUMERO_MODIFICACION}} but ONLY when part of "Modificación No."
# Let's find "No." in the text, then look ahead 100 XML chars for underscores inside <w:t>
idx = 0
changes = 0
new_content = ""
while True:
    found = content.find('No.', idx)
    if found == -1:
        new_content += content[idx:]
        break
    
    # Check if context backwards is "MODIFICACI"
    ctx_back = content[max(0, found-100):found]
    if 'MODIFICACI' in ctx_back.upper():
        chunk = content[found:found+200]
        # Find the first sequence of ________ inside > <
        m = re.search(r'>(\s*_{4,10}\s*)<', chunk)
        if m:
            old_str = m.group(1)
            # Reconstruct the string with {{NUMERO_MODIFICACION}}
            replaced = chunk.replace(f'>{old_str}<', '>{{NUMERO_MODIFICACION}}<', 1)
            new_content += content[idx:found] + replaced
            idx = found + 200
            changes += 1
            print(f"Replaced underscores at pos {found}: '{old_str}'")
            continue
    
    # If no match or not modified
    new_content += content[idx:found+3]
    idx = found + 3

if changes > 0:
    all_files['word/document.xml'] = new_content.encode('utf-8')

    tmp = src + '.tmp'
    with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
        for name, data in all_files.items():
            zout.writestr(name, data)

    os.replace(tmp, src)
    print(f'OK: Updated {changes} occurrences in {src}')
else:
    print('No occurrences found/replaced.')
