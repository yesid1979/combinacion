import zipfile, re, shutil, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

original_len = len(content)
changes = []

# =========================================================
# 1. Reemplazar MERGEFIELD valor_de_enero_a_agosto_letras_ 
#    por {{VALOR_CONTRATO_LETRAS}}
# La celda contiene: fldChar(begin) + instrText(MERGEFIELD letras) + 
#                    fldChar(separate) + display_text + fldChar(end)
#                    (ESPACIO) ($  fldChar(begin) instrText(numero) fldChar(sep) display fldChar(end) )
# Quiero reemplazar TODO ese bloque por:
#   run("{{VALOR_CONTRATO_LETRAS}} (${{VALOR_CONTRATO}})")
# =========================================================

# Patron del MERGEFIELD completo de letras (begin...end)
pat_letras_begin = r'<w:r[^>]*><w:rPr>[^<]*(?:<[^/][^>]*/?>)*</w:rPr><w:fldChar w:fldCharType="begin"/></w:r><w:r[^>]*><w:rPr>[^<]*(?:<[^/][^>]*/?>)*</w:rPr><w:instrText[^>]*>\s*MERGEFIELD valor_de_enero_a_agosto_letras_\s*</w:instrText></w:r>'

# Mejor estrategia: buscar el <w:p> que contiene el MERGEFIELD y reemplazar el contenido de esa celda
# La celda tiene color A6A6A6 y tiene los MERGEFIELDs
# Encontremos el <w:tc> completo que contiene el MERGEFIELD

idx_mf = content.find('MERGEFIELD valor_de_enero_a_agosto_letras_')
if idx_mf < 0:
    print('ERROR: MERGEFIELD letras no encontrado')
else:
    # Buscar el inicio del <w:tc> que contiene este MERGEFIELD
    tc_start = content.rfind('<w:tc>', 0, idx_mf)
    # Buscar el fin de ese <w:tc>
    tc_end = content.find('</w:tc>', idx_mf) + len('</w:tc>')
    
    old_tc = content[tc_start:tc_end]
    print(f'TC encontrado: pos {tc_start} a {tc_end} ({tc_end-tc_start} chars)')
    
    # Obtener el <w:tcPr> del TC original para preservar formato de celda
    tcp_match = re.search(r'<w:tcPr>.*?</w:tcPr>', old_tc, re.DOTALL)
    tcp = tcp_match.group(0) if tcp_match else ''
    
    # Obtener el <w:pPr> del parrafo original
    ppr_match = re.search(r'<w:pPr>.*?</w:pPr>', old_tc, re.DOTALL)
    ppr = ppr_match.group(0) if ppr_match else ''
    
    # Construir la celda nueva con los placeholders como texto normal
    # Usar el mismo estilo de fuente (Arial, sz 22, color A6A6A6)
    rpr = '<w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:color w:val="A6A6A6" w:themeColor="background1" w:themeShade="A6"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr>'
    
    new_tc = (
        f'<w:tc>{tcp}'
        f'<w:p>'
        f'{ppr}'
        f'<w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t xml:space="preserve">{{{{VALOR_CONTRATO_LETRAS}}}} ({{{{VALOR_CONTRATO}}}})</w:t></w:r>'
        f'</w:p>'
        f'</w:tc>'
    )
    
    content = content[:tc_start] + new_tc + content[tc_end:]
    changes.append('MERGEFIELD VALOR_CONTRATO reemplazado por {{VALOR_CONTRATO_LETRAS}} ({{VALOR_CONTRATO}})')
    print('OK: MERGEFIELD reemplazado')

# =========================================================
# 2. Reemplazar &lt;&lt;NOMBRE DEL ORDENADOR DEL GASTO&gt;&gt;
#    por {{NOMBRE_ORDENADOR_GASTO}}
# =========================================================
# En el XML aparece como &lt;&lt; ... &gt;&gt;
replacements_text = [
    ('&lt;&lt; NOMBRE DEL ORDENADOR DEL GASTO&gt;&gt;', '{{NOMBRE_ORDENADOR_GASTO}}'),
    ('&lt;&lt;NOMBRE DEL ORGANISMO&gt;&gt;', '{{ORGANISMO}}'),
    ('&lt;&lt; NOMBRE DEL ORGANISMO&gt;&gt;', '{{ORGANISMO}}'),
    ('&lt;&lt;NOMBRE DEL ORGANISMO &gt;&gt;', '{{ORGANISMO}}'),
    ('&lt;&lt; TIPO DE CONTRATO &gt;&gt;', '{{TIPO_CONTRATO}}'),
    ('&lt;&lt; TIPO DE CONTRATO&gt;&gt;', '{{TIPO_CONTRATO}}'),
    ('&lt;&lt;TIPO DE CONTRATO&gt;&gt;', '{{TIPO_CONTRATO}}'),
]

for old, new in replacements_text:
    count = content.count(old)
    if count > 0:
        content = content.replace(old, new)
        changes.append(f'Reemplazado {count}x: {old} -> {new}')
        print(f'OK: {count}x "{old}" -> "{new}"')
    else:
        print(f'NO encontrado: {old}')

# =========================================================
# 3. Buscar celdas vacias de checkboxes (ADICION, PRORROGA, CESION, ACLARACION)
#    en la fila de checkboxes y agregar placeholders
# =========================================================
# Los checkboxes estan en la primera tabla
# ADICION checkbox -> {{ADICION_X}}
# PRORROGA checkbox -> dejar vacio (N/A fijo)
# CESION checkbox -> dejar vacio (N/A fijo)  
# ACLARACION/OTRA MODIFICACION checkbox -> dejar vacio

# Buscar las celdas de checkboxes en la zona de la tabla de cabecera
# Primero encontremos donde estan ADICION, PRORROGA, CESION
for label in ['ADICI', 'PR\u00d3RROGA', 'CESI\u00d3N', 'ACLARACI\u00d3N']:
    idx = content.find(label)
    if idx >= 0:
        print(f'Encontrado {label} en pos {idx}')
    else:
        print(f'NO encontrado: {label}')

print()
print('=== PLACEHOLDERS resultantes ===')
ph = re.findall(r'\{\{[^}]+\}\}', content)
for p in sorted(set(ph)):
    print(p)

# =========================================================
# 4. Verificar y agregar {{CARGO_ORDENADOR_GASTO}} y {{ORGANISMO}}
#    al mapa de replacements del servlet
# =========================================================

# =========================================================
# Guardar el archivo modificado
# =========================================================
all_files['word/document.xml'] = content.encode('utf-8')

tmp = src + '.tmp'
with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
    for name, data in all_files.items():
        zout.writestr(name, data)

os.replace(tmp, src)
print()
print(f'OK Plantilla actualizada: {src}')
print(f'Cambios realizados: {len(changes)}')
for c in changes:
    print(f'  - {c}')
