import zipfile, re, shutil, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

changes = []

# =========================================================
# Reemplazar << TEXTO >> que puede estar dividido en multiples tags
# =========================================================
def replace_split_tags(content, search_pattern, replacement):
    # Search for &lt;&lt; followed by anything until &gt;&gt; 
    # capturing the exact regex matches to replace
    # We will build a regex that matches the search_pattern ignoring XML tags
    
    # Simple approach: find all &lt;&lt; ... &gt;&gt; sequences
    matches = list(re.finditer(r'&lt;&lt;(.*?)&gt;&gt;', content, flags=re.DOTALL))
    offset = 0
    new_content = content
    for m in matches:
        start = m.start() + offset
        end = m.end() + offset
        raw_match = new_content[start:end]
        
        # Clean the match to see what text it represents
        clean_text = re.sub(r'<[^>]+>', '', raw_match).strip()
        
        # If the clean text matches our target
        if re.search(search_pattern, clean_text):
            # We want to replace this entire XML stretch 'raw_match'
            # with a single run just containing the replacement text
            # But we should keep the formatting of the first text node.
            
            # Find the first rPr inside the match, or before it
            rpr_match = re.search(r'<w:rPr>.*?</w:rPr>', raw_match)
            rpr = rpr_match.group(0) if rpr_match else ''
            
            new_run = f'&lt;&lt;</w:t></w:r><w:r><w:t>{replacement}</w:t></w:r><w:r><w:t>&gt;&gt;'  # temporary
            # A much safer replacement that won't break the XML structure:
            # We just replace the text inside the FIRST <w:t> tag of the sequence,
            # and empty out the text of all other <w:t> tags in the sequence.
            
            # Find all <w:t> tags in raw_match
            t_tags = list(re.finditer(r'(<w:t(?: [^>]+)?>)(.*?)(</w:t>)', raw_match))
            if t_tags:
                replaced_raw = raw_match
                for i, t in enumerate(t_tags):
                    full_t = t.group(0)
                    if i == 0:
                        # Keep formatting, use replacement text
                        new_t = f'{t.group(1)}{replacement}{t.group(3)}'
                    else:
                        # Empty out subsequent tags
                        new_t = f'{t.group(1)}{t.group(3)}'
                    replaced_raw = replaced_raw.replace(full_t, new_t, 1)
                
                # Further replace &lt;&lt; and &gt;&gt; if they are outside w:t tags but part of raw text?
                # Actually, &lt;&lt; and &gt;&gt; ARE inside w:t tags.
                # So replaced_raw will now have &lt;&lt;replacement&gt;&gt; spread out.
                # Let's just remove the &lt;&lt; and &gt;&gt; from the text.
                # Wait, if we replace the first tag with `replacement` and empty the others,
                # any &lt;&lt; and &gt;&gt; inside those tags will be overwritten.
                # Except if &lt;&lt; is in the first tag, it will be overwritten by `replacement`.
                
                # Let's use a simpler approach for raw_match:
                # regex to replace text ONLY inside <w:t> tags and text outside tags
                # wait, raw_match is just an XML string.
                
                # The safest way is to rebuild it as:
                # Find the first w:r start tag before the text, and keep it.
                pass
            
            # Simple & robust way:
            # Just create a new <w:r> string with the first found properties
            # Wait, raw_match itself might contain paragraph boundaries? No, shouldn't.
            
            # Let's just create a new text run
            replacement_runs = f'</w:t></w:r><w:r>{rpr}<w:t>{replacement}</w:t></w:r><w:r><w:t>'
            
            new_content = new_content[:start] + replacement_runs + new_content[end:]
            offset += len(replacement_runs) - len(raw_match)
            changes.append(f'Replaced {clean_text} -> {replacement}')
            print(f'Replaced "{clean_text}" -> "{replacement}"')
            
    return new_content

content = replace_split_tags(content, r'NOMBRE DEL ORDENADOR DEL GASTO', '{{NOMBRE_ORDENADOR_GASTO}}')
content = replace_split_tags(content, r'TIPO DE CONTRATO', '{{TIPO_CONTRATO}}')

# =========================================================
# 3. Checkboxes (ADICIN, PRRROGA, ACLARACIN, CESIN)
# =========================================================
# They are empty table cells. Let's find them.
# "ADICIN" is followed by an empty cell for the checkbox.
# We will inject {{ADICION_X}} into the empty cell next to ADICIN
# In Word XML: <w:tc>...ADICIN...</w:tc> <w:tc> <w:p> ... </w:p> </w:tc>
idx_adicion = content.find('ADICI\u00d3N')
if idx_adicion >= 0:
    # Find the NEXT </w:tc>
    tc1_end = content.find('</w:tc>', idx_adicion)
    if tc1_end > 0:
        # The next cell starts after tc1_end
        tc2_start = content.find('<w:tc>', tc1_end)
        tc2_end = content.find('</w:tc>', tc2_start)
        if tc2_start > 0 and tc2_end > tc2_start:
            # Inject {{ADICION_X}} inside its <w:p>
            cell_xml = content[tc2_start:tc2_end+7]
            # Replace empty <w:p ...></w:p> or add inside <w:p>
            if '<w:t>' not in cell_xml:
                new_cell = cell_xml.replace('</w:pPr></w:p>', '</w:pPr><w:r><w:t>{{ADICION_X}}</w:t></w:r></w:p>')
                if new_cell != cell_xml:
                    content = content[:tc2_start] + new_cell + content[tc2_end+7:]
                    changes.append('Agregado {{ADICION_X}} en checkbox')

# =========================================================
# FECHAS suspension y reanudacion 
# =========================================================
# The template has FECHA DE SUSPENSION NO. 1 -> {{FECHA_ACTA_INICIO}}
# We should remove the placeholder and leave it as N/A or blank since it doesn't apply by default.
# But looking at User request, maybe just replace {{FECHA_ACTA_INICIO}} with N/A exactly for those two rows
for row_text in ['FECHA DE SUSPENSION NO. 1', 'FECHA ACTA DE REANUDACI\u00d3N NO. 1']:
    idx_row = content.find(row_text)
    if idx_row >= 0:
        # Find the next {{FECHA_ACTA_INICIO}}
        idx_ph = content.find('{{FECHA_ACTA_INICIO}}', idx_row, idx_row + 1000)
        if idx_ph >= 0:
            content = content[:idx_ph] + 'N/A' + content[idx_ph+len('{{FECHA_ACTA_INICIO}}'):]
            changes.append(f'Reemplazado {{{{FECHA_ACTA_INICIO}}}} por N/A en {row_text}')


# =========================================================
# MODIFICACION No. ______
# =========================================================
# In User screenshot, it says: "MODIFICACIÓN No. __________"
# We could replace the underscores with {{NUMERO_MODIFICACION}} if needed, but the user doesn't seem to have a placeholder for this in the servlet yet. 


all_files['word/document.xml'] = content.encode('utf-8')

tmp = src + '.tmp'
with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
    for name, data in all_files.items():
        zout.writestr(name, data)

os.replace(tmp, src)
print(f'OK Plantilla actualizada: {src}')
for c in changes:
    print(f'  - {c}')
