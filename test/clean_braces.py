import zipfile
import re
import os
import shutil

def clean_double_braces(file_path):
    """
    Clean up double braces {{{{ -> {{ and }}}} -> }}
    """
    print(f"\nCleaning: {file_path}")
    
    temp_dir = "temp_clean_docx"
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir)
    
    try:
        # Extract
        with zipfile.ZipFile(file_path, 'r') as zip_ref:
            zip_ref.extractall(temp_dir)
        
        xml_path = os.path.join(temp_dir, 'word', 'document.xml')
        
        # Read and clean
        with open(xml_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Count occurrences before
        before_count = content.count('{{{{')
        
        # Replace quadruple braces with double
        content = content.replace('{{{{', '{{')
        content = content.replace('}}}}', '}}')
        
        # Also fix any triple braces that might exist
        content = content.replace('{{{', '{{')
        content = content.replace('}}}', '}}')
        
        # Write back
        with open(xml_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        # Re-zip
        with zipfile.ZipFile(file_path, 'w', zipfile.ZIP_DEFLATED) as zip_out:
            for root_dir, dirs, files in os.walk(temp_dir):
                for file in files:
                    full_path = os.path.join(root_dir, file)
                    arcname = os.path.relpath(full_path, temp_dir)
                    zip_out.write(full_path, arcname)
        
        print(f"  Fixed {before_count} double-brace issues")
        print("  [OK] Success!")
        
    except Exception as e:
        print(f"  [ERROR] {e}")
    finally:
        if os.path.exists(temp_dir):
            shutil.rmtree(temp_dir)

if __name__ == "__main__":
    files = [
        "plantillas/INVERSION_1_ESTUDIOS_PREVIOS.docx",
        "plantillas/INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx",
        "plantillas/INVERSION_4_COMPLEMENTO_CONTRATO.docx"
    ]
    
    for f in files:
        if os.path.exists(f):
            clean_double_braces(f)
        else:
            print(f"File not found: {f}")
    
    print("\n[OK] All templates cleaned!")
