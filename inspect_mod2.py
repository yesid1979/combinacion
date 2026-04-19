import zipfile, re

path = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'
with zipfile.ZipFile(path) as z:
    with z.open('word/document.xml') as f:
        content = f.read().decode('utf-8')

# Placeholders {{}}
ph1 = re.findall(r'\{\{[^}]+\}\}', content)
# Placeholders <<>>
ph2 = re.findall(r'<<[^>]+>>', content)
# MERGEFIELDs
ph3 = re.findall(r'MERGEFIELD\s+\S+', content)
# Placeholders dolar
ph4 = re.findall(r'\$\{[^}]+\}', content)

print('=== PLACEHOLDERS {{}} ===')
for p in sorted(set(ph1)):
    print(p)

print()
print('=== PLACEHOLDERS <<>> ===')
for p in sorted(set(ph2)):
    print(p)

print()
print('=== MERGEFIELDS ===')
for p in sorted(set(ph3)):
    print(p)

print()
print('=== PLACEHOLDERS dollar{} ===')
for p in sorted(set(ph4)):
    print(p)

# Texto visible completo
print()
print('=== TEXTO VISIBLE DEL DOCUMENTO ===')
clean = re.sub(r'<[^>]+>', ' ', content)
clean = re.sub(r' +', ' ', clean).strip()
print(clean[:8000])
