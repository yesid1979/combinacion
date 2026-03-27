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
            
            # The paragraph content we want to edit:
            # `<w:t xml:space="preserve">, a </w:t></w:r><w:r w:rsidR="00E8...><w:rPr>...<w:t xml:space="preserve">los </w:t>`
            # We want to replace everything from `>los </w:t>` up to `____.</w:t>`
            # Let's target: `>los </w:t>.*?</w:r>.*?<w:t[^>]*?>.*?</w:t>.*?</w:r>.*>____.</w:t>`
            # Wait, `>los </w:t>` is unique inside this paragraph.
            # So let's find the paragraph first:
            
            p_match = re.search(r'<w:p\b[^>]*>.*?En constancia.*?</w:p>', xml, re.DOTALL | re.IGNORECASE)
            if p_match:
                pg = p_match.group(0)
                # Now string replace inside this paragraph ONLY.
                # Replace everything from `a </w:t>` down to `.</w:t>` with ` </w:t></w:r><w:r><w:rPr><w:lang w:val="es-ES"/></w:rPr><w:t>{{FECHA_CONSTANCIA_HOY}}.</w:t>`
                # Note: `a </w:t>` matches `xml:space="preserve">, a </w:t>`
                # Also `.</w:t>` matches the end `____.</w:t>`
                
                # Using regex to target the exact sub-block:
                pg_new = re.sub(r'(<w:t xml:space="preserve">,\s*a\s*</w:t></w:r>).*?(<w:t[^>]*?>_{2,6}\.</w:t>)', r'\1<w:r><w:t xml:space="preserve"> {{FECHA_CONSTANCIA_HOY}}.</w:t></w:r><!-- end -->', pg, flags=re.DOTALL|re.IGNORECASE)
                
                # Did we match?
                if pg_new != pg:
                    print("Replaced chunk correctly!")
                else:
                    print("Chunk missing?")
                    
                # To be absolutely sure, it might be simpler to just erase all runs after the `Santiago de Cali` and insert our own.
                # The text ends at: `<w:t xml:space="preserve">Santiago de </w:t></w:r><w:r...><w:t>Cali</w:t></w:r>`
                # So we can just find `<w:t>Cali</w:t></w:r>` and slice everything off until `</w:p>`
                
                # Yes!
                idx_cali = pg.find('<w:t>Cali</w:t></w:r>')
                if idx_cali != -1:
                    pg_new = pg[:idx_cali + len('<w:t>Cali</w:t></w:r>')] + '<w:r><w:t>, </w:t></w:r><w:r><w:rPr><w:lang w:val="es-ES"/></w:rPr><w:t>{{FECHA_CONSTANCIA_HOY}}.</w:t></w:r></w:p>'
                    print("Sliced safely!")
                
                xml = xml.replace(pg, pg_new)
                
            else:
                print('Not found')
            
            zout.writestr(item, xml.encode('utf-8'))
        else:
            zout.writestr(item, content)

shutil.move(out, src)
print('Done!')
