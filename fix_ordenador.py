import zipfile, shutil

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'
out = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION_TMP.docx'
with zipfile.ZipFile(src, 'r') as zin, zipfile.ZipFile(out, 'w') as zout:
    for item in zin.infolist():
        content = zin.read(item.filename)
        if item.filename == 'word/document.xml':
            xml = content.decode('utf-8')
            
            # The naked texts:
            xml = xml.replace('</w:t></w:r>, de conformidad con la delegaci', '</w:t></w:r><w:r><w:t xml:space="preserve">, de conformidad con la delegaci')
            xml = xml.replace('</w:t></w:r>quien en lo sucesivo', '</w:t></w:r><w:r><w:t xml:space="preserve">quien en lo sucesivo')
            xml = xml.replace('</w:t></w:r>, quien en lo sucesivo', '</w:t></w:r><w:r><w:t xml:space="preserve">, quien en lo sucesivo')

            zout.writestr(item, xml.encode('utf-8'))
        else:
            zout.writestr(item, content)

shutil.move(out, src)
print('Fixed!')
