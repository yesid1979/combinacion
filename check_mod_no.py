import zipfile, re, os

paths = [
    r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_1_JUSTIFICACION.docx',
    r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'
]

for path in paths:
    filename = os.path.basename(path)
    print(f'Checking {filename}')
    try:
        with zipfile.ZipFile(path, 'r') as z:
            with z.open('word/document.xml') as f:
                content = f.read().decode('utf-8')
        
        # Searching for 'MODIFICACIÓN No.' or variations
        plain = re.sub(r'<[^>]+>', '', content)
        matches = re.finditer(r'MODIFICAC.*?N\s+No\.\s*[_]*', plain, flags=re.IGNORECASE)
        for m in matches:
            print('  Found:', m.group(0))
            
    except Exception as e:
        print('  Error:', e)
