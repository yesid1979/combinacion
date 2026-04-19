import zipfile, re, os, shutil

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'
bak = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION_bak.docx'

# Use backup to get fresh XML
with zipfile.ZipFile(bak, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

# ---------- 1. MERGEFIELD VALOR DE CONTRATO ----------
idx_mf = content.find('MERGEFIELD valor_de_enero_a_agosto_letras_')
if idx_mf >= 0:
    tc_start = content.rfind('<w:tc>', 0, idx_mf)
    tc_end = content.find('</w:tc>', idx_mf) + len('</w:tc>')
    old_tc = content[tc_start:tc_end]
    tcp_match = re.search(r'<w:tcPr>.*?</w:tcPr>', old_tc, re.DOTALL)
    tcp = tcp_match.group(0) if tcp_match else ''
    ppr_match = re.search(r'<w:pPr>.*?</w:pPr>', old_tc, re.DOTALL)
    ppr = ppr_match.group(0) if ppr_match else ''
    new_tc = (
        f'<w:tc>{tcp}'
        f'<w:p>{ppr}'
        f'<w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:color w:val="A6A6A6" w:themeColor="background1" w:themeShade="A6"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t xml:space="preserve">{{{{VALOR_CONTRATO_LETRAS}}}} ({{{{VALOR_CONTRATO}}}})</w:t></w:r>'
        f'</w:p></w:tc>'
    )
    content = content[:tc_start] + new_tc + content[tc_end:]

# ---------- 2. TAGS << >> ----------
def replace_split_tags(content, search_pattern, replacement):
    matches = list(re.finditer(r'&lt;&lt;(.*?)&gt;&gt;', content, flags=re.DOTALL))
    offset = 0
    new_content = content
    for m in matches:
        start = m.start() + offset
        end = m.end() + offset
        raw_match = new_content[start:end]
        clean_text = re.sub(r'<[^>]+>', '', raw_match).strip()
        if re.search(search_pattern, clean_text):
            rpr_match = re.search(r'<w:rPr>.*?</w:rPr>', raw_match)
            rpr = rpr_match.group(0) if rpr_match else ''
            replacement_runs = f'</w:t></w:r><w:r>{rpr}<w:t>{replacement}</w:t></w:r><w:r><w:t>'
            new_content = new_content[:start] + replacement_runs + new_content[end:]
            offset += len(replacement_runs) - len(raw_match)
    return new_content

content = replace_split_tags(content, r'NOMBRE DEL ORDENADOR DEL GASTO', '{{NOMBRE_ORDENADOR_GASTO}}')
content = replace_split_tags(content, r'TIPO DE CONTRATO', '{{TIPO_CONTRATO}}')
content = replace_split_tags(content, r'NOMBRE DEL ORGANISMO', '{{ORGANISMO}}')

# ---------- 3. CHECKBOX ADICIÓN ----------
idx_adicion = content.find('ADICI\u00d3N')
if idx_adicion >= 0:
    tc1_end = content.find('</w:tc>', idx_adicion)
    if tc1_end > 0:
        tc2_start = content.find('<w:tc>', tc1_end)
        tc2_end = content.find('</w:tc>', tc2_start)
        if tc2_start > 0 and tc2_end > tc2_start:
            cell_xml = content[tc2_start:tc2_end+7]
            if '<w:t>' not in cell_xml:
                new_cell = cell_xml.replace('</w:pPr></w:p>', '</w:pPr><w:r><w:t>{{ADICION_X}}</w:t></w:r></w:p>')
                content = content[:tc2_start] + new_cell + content[tc2_end+7:]

# ---------- 4. FECHAS DE SUSPENSION ----------
for row_text in ['FECHA DE SUSPENSION NO. 1', 'FECHA ACTA DE REANUDACI\u00d3N NO. 1']:
    idx_row = content.find(row_text)
    if idx_row >= 0:
        idx_ph = content.find('{{FECHA_ACTA_INICIO}}', idx_row, idx_row + 1000)
        if idx_ph >= 0:
            content = content[:idx_ph] + 'N/A' + content[idx_ph+len('{{FECHA_ACTA_INICIO}}'):]

# ---------- 5. MODIFICACION No. 001 ----------
# Simple exact replacement for the exact underscore strings found in the raw XML
# We previously dumped exactly these strings!
replace_dict = {
    'l:space="preserve"> ______</w:t></w:r></': 'l:space="preserve"> 001</w:t></w:r></',
    's-ES"/></w:rPr><w:t>_____</w:t></w:r><w:': 's-ES"/></w:rPr><w:t>001</w:t></w:r><w:',
    's-ES"/></w:rPr><w:t>____</w:t></w:r><w:r': 's-ES"/></w:rPr><w:t>001</w:t></w:r><w:r',
    's-ES"/></w:rPr><w:t>______</w:t></w:r><w': 's-ES"/></w:rPr><w:t>001</w:t></w:r><w'
}

for old, new in replace_dict.items():
    content = content.replace(old, new)
    
# There is one more specific to the arabic part: 
content = content.replace('astAsia="ar-SA"/></w:rPr><w:t>_______</w:t>', 'astAsia="ar-SA"/></w:rPr><w:t>001</w:t>')

all_files['word/document.xml'] = content.encode('utf-8')
tmp = src + '.tmp'
with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
    for name, data in all_files.items():
        zout.writestr(name, data)
os.replace(tmp, src)
print("ALL DONE. MODIFICACION_2_ACEPTACION updated properly.")
