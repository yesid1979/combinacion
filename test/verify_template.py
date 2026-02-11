import zipfile
import re

def check_placeholders(docx_path):
    print(f"\n=== Checking: {docx_path} ===")
    
    try:
        with zipfile.ZipFile(docx_path, 'r') as docx:
            xml_content = docx.read('word/document.xml').decode('utf-8')
            
            # Find all {{...}} patterns
            placeholders = re.findall(r'\{\{[^}]+\}\}', xml_content)
            
            if placeholders:
                print(f"Found {len(placeholders)} placeholders:")
                for p in set(placeholders):
                    print(f"  - {p}")
            else:
                print("No {{...}} placeholders found")
                
            # Also check for any remaining highlight tags
            if 'w:highlight' in xml_content:
                print("\n⚠️ WARNING: Still has highlighted text!")
            
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    files = [
        "plantillas/INVERSION_1_ESTUDIOS_PREVIOS.docx",
        "plantillas/INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx",
        "plantillas/INVERSION_4_COMPLEMENTO_CONTRATO.docx"
    ]
    
    for f in files:
        check_placeholders(f)
