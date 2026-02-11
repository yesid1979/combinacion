import zipfile
import re

def test_replacement(docx_path):
    """
    Test if placeholders can be found in the document
    """
    print(f"\n=== Testing: {docx_path} ===")
    
    try:
        with zipfile.ZipFile(docx_path, 'r') as docx:
            xml_content = docx.read('word/document.xml').decode('utf-8')
            
            # Test placeholders
            test_placeholders = [
                '{{CONTRATISTA_NOMBRE}}',
                '{{CONTRATISTA_CEDULA}}',
                '{{NIVEL_CONTRATO}}',
                '{{PERFIL_FORMACION}}',
                '{{PERFIL_EXPERIENCIA}}',
                '{{ACTIVIDADES_CONTRACTUALES}}',
                '{{VALOR_CONTRATO}}',
                '{{NOMBRE_ORDENADOR_GASTO}}'
            ]
            
            print("\nPlaceholder check:")
            for placeholder in test_placeholders:
                # Check if it exists as a complete string
                if placeholder in xml_content:
                    print(f"  [OK] {placeholder} - Found as complete string")
                else:
                    # Check if it's fragmented
                    # Remove {{ and }} and check if the inner text exists
                    inner = placeholder[2:-2]  # Remove {{ and }}
                    if inner in xml_content:
                        print(f"  [WARN] {placeholder} - Inner text found but likely fragmented")
                    else:
                        print(f"  [MISSING] {placeholder} - Not found at all")
            
            # Check for any remaining fragmentation patterns
            fragments = re.findall(r'\{\{[^}]*</w:t>', xml_content)
            if fragments:
                print(f"\n[WARN] Found {len(fragments)} fragmented placeholders:")
                for frag in fragments[:5]:  # Show first 5
                    print(f"  - {frag[:50]}...")
                    
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    files = [
        "plantillas/INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx",
        "plantillas/INVERSION_4_COMPLEMENTO_CONTRATO.docx"
    ]
    
    for f in files:
        test_replacement(f)
