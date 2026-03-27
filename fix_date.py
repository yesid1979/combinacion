import zipfile, re, shutil

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'
out = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION_TMP.docx'
with zipfile.ZipFile(src, 'r') as zin, zipfile.ZipFile(out, 'w') as zout:
    for item in zin.infolist():
        content = zin.read(item.filename)
        if item.filename == 'word/document.xml':
            xml = content.decode('utf-8')
            
            p_match = re.search(r'<w:p\b[^>]*>.*?En constancia.*?</w:p>', xml, re.DOTALL | re.IGNORECASE)
            if p_match:
                pg = p_match.group(0)
                clean_pg = '<w:p><w:pPr><w:jc w:val="both"/><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr><w:t xml:space="preserve">En constancia se firma la presente modificación en la ciudad de Santiago de Cali, </w:t></w:r><w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr><w:t>{{FECHA_CONSTANCIA_HOY}}.</w:t></w:r></w:p>'
                xml = xml.replace(pg, clean_pg)
                print('Replaced entirely!')
            else:
                print('Not found')
            
            zout.writestr(item, xml.encode('utf-8'))
        else:
            zout.writestr(item, content)

shutil.move(out, src)
print('Done!')
