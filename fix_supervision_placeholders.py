import zipfile, re, shutil, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\INFORME_SUPERVISION_TEMPLATE.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

# We want to replace "Adición:" with "Adición: ${ADICIONES}"
# But they could be split into multiple XML tags.
# Since it's hard to deal with XML tags, we'll just replace the whole <w:p> containing "Adición:"
# Or easier: just use a simple regex if we remove tags to find it, then map back...
# Actually, since Word templates often have text cleanly inside <w:t>, let's check.
import re

def insert_placeholder(xml, label_regex, new_text):
    # Find the <w:t> tags containing the label or part of it
    # Easiest is to just do this: find <w:p> containing "Adición:" when tags are stripped
    paragraphs = re.findall(r'<w:p\b.*?</w:p>', xml, re.DOTALL)
    for p in paragraphs:
        clean_p = re.sub(r'<[^>]+>', '', p)
        if re.search(label_regex, clean_p):
            # We found the paragraph!
            # Let's see if the placeholder is already there
            if new_text in clean_p:
                continue
            
            # Find where to append the placeholder: 
            # We can just append it at the end of the paragraph before </w:p>
            # Or after the label.
            # To be safe, just append it at the end of the paragraph.
            new_p = p.replace('</w:p>', f'<w:r><w:t xml:space="preserve"> {new_text}</w:t></w:r></w:p>')
            xml = xml.replace(p, new_p)
    return xml

content = insert_placeholder(content, r'Adición:', '${ADICIONES}')
content = insert_placeholder(content, r'Prórroga:', '${PRORROGAS}')

# For Recibo and Paz y Salvo, we might need to add a whole new paragraph if they don't exist
paragraphs = re.findall(r'<w:p\b.*?</w:p>', content, re.DOTALL)
found_recibo = False
for p in paragraphs:
    if 'Recibo a Satisfacción de Servicios:' in re.sub(r'<[^>]+>', '', p):
        found_recibo = True
        break

if not found_recibo:
    # Let's insert a new paragraph after Prórroga
    for p in paragraphs:
        if 'Prórroga:' in re.sub(r'<[^>]+>', '', p):
            new_p = p + f'<w:p><w:pPr><w:pStyle w:val="Normal"/></w:pPr><w:r><w:t xml:space="preserve">Recibo a Satisfacción de Servicios: </w:t></w:r><w:r><w:t xml:space="preserve">${{RECIBO_SATISFACCION}}</w:t></w:r></w:p>'
            new_p += f'<w:p><w:pPr><w:pStyle w:val="Normal"/></w:pPr><w:r><w:t xml:space="preserve">Constancia de Paz y Salvo: </w:t></w:r><w:r><w:t xml:space="preserve">${{CONSTANCIA_PAZ_SALVO}}</w:t></w:r></w:p>'
            content = content.replace(p, new_p)
            break

all_files['word/document.xml'] = content.encode('utf-8')

tmp = src + '.tmp'
with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
    for name, data in all_files.items():
        zout.writestr(name, data)

os.replace(tmp, src)
print("Updated placeholders in INFORME_SUPERVISION_TEMPLATE.docx")
