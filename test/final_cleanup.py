import zipfile
import re
import os
import shutil

def final_cleanup(file_path):
    """
    Final cleanup of remaining issues
    """
    print(f"\nFinal cleanup: {file_path}")
    
    temp_dir = "temp_final_docx"
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir)
    
    try:
        # Extract
        with zipfile.ZipFile(file_path, 'r') as zip_ref:
            zip_ref.extractall(temp_dir)
        
        xml_path = os.path.join(temp_dir, 'word', 'document.xml')
        
        # Read
        with open(xml_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Fix specific issues
        fixes = 0
        
        # Fix "{{ {{PLACEHOLDER}}" -> "{{PLACEHOLDER}}"
        before = content
        content = re.sub(r'\{\{\s+\{\{', '{{', content)
        if content != before:
            fixes += 1
            
        # Fix {{Experiencia}} -> {{PERFIL_EXPERIENCIA}}
        if '{{Experiencia}}' in content:
            content = content.replace('{{Experiencia}}', '{{PERFIL_EXPERIENCIA}}')
            fixes += 1
        
        # Fix {{Perfil}} -> {{PERFIL_FORMACION}} if exists
        if '{{Perfil}}' in content:
            content = content.replace('{{Perfil}}', '{{PERFIL_FORMACION}}')
            fixes += 1
            
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
        
        print(f"  Applied {fixes} final fixes")
        print("  [OK] Done!")
        
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
            final_cleanup(f)
        else:
            print(f"File not found: {f}")
    
    print("\n[OK] Final cleanup complete!")
