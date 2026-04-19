import zipfile, re, shutil

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'
out = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION_TMP.docx'
bak = src + '.bak_date'

# Restore from the backup where paragraph was intact
shutil.copy(bak, src)

with zipfile.ZipFile(src, 'r') as zin, zipfile.ZipFile(out, 'w') as zout:
    for item in zin.infolist():
        content = zin.read(item.filename)
        if item.filename == 'word/document.xml':
            xml = content.decode('utf-8')
            
            p_match = re.search(r'<w:p\b[^>]*>.*?En constancia.*?</w:p>', xml, re.DOTALL | re.IGNORECASE)
            if p_match:
                pg = p_match.group(0)
                
                idx_cali = pg.find('<w:t>Cali</w:t></w:r>')
                if idx_cali != -1:
                    fmt = '<w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr>'
                    pg_new = pg[:idx_cali + len('<w:t>Cali</w:t></w:r>')] + '<w:r>' + fmt + '<w:t>, </w:t></w:r><w:r>' + fmt.replace('</w:rPr>', '<w:lang w:val="es-ES"/></w:rPr>') + '<w:t>{{FECHA_CONSTANCIA_HOY}}.</w:t></w:r></w:p>'
                    
                    xml = xml.replace(pg, pg_new)
                    print("Re-injected with correct Arial formatting!")
                else:
                    print("Could not find Cali")
            else:
                print('Not found')
            
            zout.writestr(item, xml.encode('utf-8'))
        else:
            zout.writestr(item, content)

shutil.move(out, src)
print('Done!')
