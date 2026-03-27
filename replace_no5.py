import zipfile, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

# We know the first one is '> ______<'
# Other are '>_______<' etc. but we need to ensure we only replace the ones for MODIFICACIÓN No.
# Instead of regex, simple string replace since we only have a few cases.

content = content.replace('> ______</w:t>', '>001</w:t>', 1)
content = content.replace('w:t>_______</w:t>', 'w:t>001</w:t>', 1)
content = content.replace('w:t>_____</w:t>', 'w:t>001</w:t>', 2)
content = content.replace('w:t>______</w:t>', 'w:t>001</w:t>', 2)

all_files['word/document.xml'] = content.encode('utf-8')
tmp = src + '.tmp'
with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
    for name, data in all_files.items():
        zout.writestr(name, data)
os.replace(tmp, src)
print("Saved modified docx.")
