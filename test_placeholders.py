import zipfile
import re
import xml.etree.ElementTree as ET

def extract_potential_placeholders(filepath):
    try:
        with zipfile.ZipFile(filepath, 'r') as z:
            xml_content = z.read('word/document.xml')
            root = ET.fromstring(xml_content)
            
            namespaces = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
            
            texts = []
            for t in root.iterfind('.//w:t', namespaces):
                if t.text:
                    texts.append(t.text)
            
            full_text = "".join(texts)
            
            placeholders = re.findall(r'\{\{.*?\}\}|\$\{.*?\}', full_text)
            brackets = re.findall(r'\[.*?\]', full_text)
            guillemets = re.findall(r'«.*?»', full_text)
            
            # Additional heuristic: uppercase words separated by underscores
            caps = re.findall(r'\b[A-Z]+(?:_[A-Z]+)+\b', full_text)
            
            all_found = sorted(list(set(placeholders + brackets + guillemets + caps)))
            return all_found
    except Exception as e:
        return str(e)

files = [
    r"c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_1_JUSTIFICACION.docx",
    r"c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx"
]

for f in files:
    print(f"======================")
    print(f"Archivo: {f.split('\\')[-1]}")
    pl = extract_potential_placeholders(f)
    if isinstance(pl, list):
        if not pl:
            print("  (No se encontraron posibles variables)")
        clean_pl = [p for p in pl if len(p) > 5 and not re.match(r'^\[\d+\]$', p)]
        for p in clean_pl:
            print("  ", p)
    else:
        print("  Error:", pl)
