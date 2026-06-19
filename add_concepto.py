import zipfile, re, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\INFORME_SUPERVISION_TEMPLATE.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

# The document has "Concepto Supervisor:" 
# We should find the paragraph containing "Concepto Supervisor:" 
# If it does not contain a placeholder, we add ${CONCEPTO_SUPERVISOR}

paragraphs = re.findall(r'<w:p\b.*?</w:p>', content, re.DOTALL)
for p in paragraphs:
    clean_p = re.sub(r'<[^>]+>', '', p)
    if 'Concepto Supervisor' in clean_p:
        if '${CONCEPTO_SUPERVISOR}' not in clean_p:
            new_p = p.replace('</w:p>', f'<w:r><w:t xml:space="preserve"> ${{CONCEPTO_SUPERVISOR}}</w:t></w:r></w:p>')
            content = content.replace(p, new_p)
            break

all_files['word/document.xml'] = content.encode('utf-8')

tmp = src + '.tmp'
with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
    for name, data in all_files.items():
        zout.writestr(name, data)

os.replace(tmp, src)
print("Updated placeholders for Concepto Supervisor in INFORME_SUPERVISION_TEMPLATE.docx")
