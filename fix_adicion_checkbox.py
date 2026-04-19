import zipfile, shutil, os, io

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_1_JUSTIFICACION.docx'
out = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_1_JUSTIFICACION.docx'

# Leer el docx
with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

# El párrafo vacío de la celda ADICION checkbox tiene paraId=1D752A72
# Buscar ese párrafo exacto y agregar {{ADICION_X}}
old_fragment = '1D752A72" w14:textId="77777777" w:rsidR="00AB773A" w:rsidRDefault="00AB773A" w:rsidP="00AB773A"><w:pPr><w:jc w:val="center"/><w:rPr><w:rFonts w:ascii="Arial" w:eastAsia="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="12"/><w:szCs w:val="12"/></w:rPr></w:pPr></w:p>'

new_fragment = '1D752A72" w14:textId="77777777" w:rsidR="00AB773A" w:rsidRDefault="00AB773A" w:rsidP="00AB773A"><w:pPr><w:jc w:val="center"/><w:rPr><w:rFonts w:ascii="Arial" w:eastAsia="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Arial" w:eastAsia="Arial" w:hAnsi="Arial" w:cs="Arial"/><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr><w:t>{{ADICION_X}}</w:t></w:r></w:p>'

if old_fragment in content:
    content_new = content.replace(old_fragment, new_fragment, 1)
    print('OK Reemplazo de ADICION_X exitoso!')
else:
    print('NO se encontro el fragmento exacto.')
    # Mostrar el contenido real para diagnóstico
    idx = content.find('1D752A72')
    if idx >= 0:
        print('Fragmento real encontrado:')
        print(repr(content[idx:idx+300]))
    raise SystemExit(1)

# Actualizar el archivo en memoria
all_files['word/document.xml'] = content_new.encode('utf-8')

# Escribir el nuevo docx
tmp_out = out + '.tmp'
with zipfile.ZipFile(tmp_out, 'w', zipfile.ZIP_DEFLATED) as zout:
    for name, data in all_files.items():
        zout.writestr(name, data)

os.replace(tmp_out, out)
print(f'OK Plantilla actualizada: {out}')
