import zipfile
import re
import os
import shutil
from lxml import etree

def fix_fragmented_placeholders(file_path):
    """
    Fix DOCX files where placeholders like {{PLACEHOLDER}} are split across multiple runs
    """
    print(f"\nProcessing: {file_path}")
    
    temp_dir = "temp_fix_docx"
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir)
    
    try:
        # Extract DOCX
        with zipfile.ZipFile(file_path, 'r') as zip_ref:
            zip_ref.extractall(temp_dir)
        
        xml_path = os.path.join(temp_dir, 'word', 'document.xml')
        
        # Parse XML properly
        tree = etree.parse(xml_path)
        root = tree.getroot()
        
        # Define namespaces
        namespaces = {
            'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
        }
        
        # Find all paragraphs
        paragraphs = root.findall('.//w:p', namespaces)
        
        fixes_made = 0
        
        for para in paragraphs:
            # Get all runs in this paragraph
            runs = para.findall('.//w:r', namespaces)
            
            if len(runs) < 2:
                continue
            
            # Collect text from all runs
            full_text = ""
            for run in runs:
                t_elements = run.findall('.//w:t', namespaces)
                for t in t_elements:
                    if t.text:
                        full_text += t.text
            
            # Check if there are placeholders in the full text
            if '{{' in full_text and '}}' in full_text:
                # Extract placeholders
                placeholders = re.findall(r'\{\{[^}]+\}\}', full_text)
                
                if placeholders:
                    print(f"  Found fragmented placeholder(s): {placeholders}")
                    
                    # Remove all runs except the first one
                    first_run = runs[0]
                    for run in runs[1:]:
                        para.remove(run)
                    
                    # Clear text in first run
                    for t in first_run.findall('.//w:t', namespaces):
                        first_run.remove(t)
                    
                    # Create new text element with full text
                    new_t = etree.SubElement(first_run, '{http://schemas.openxmlformats.org/wordprocessingml/2006/main}t')
                    new_t.set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
                    new_t.text = full_text
                    
                    fixes_made += 1
        
        print(f"  Fixed {fixes_made} fragmented placeholders")
        
        # Write back XML
        tree.write(xml_path, encoding='utf-8', xml_declaration=True, standalone=True)
        
        # Re-zip
        with zipfile.ZipFile(file_path, 'w', zipfile.ZIP_DEFLATED) as zip_out:
            for root_dir, dirs, files in os.walk(temp_dir):
                for file in files:
                    full_path = os.path.join(root_dir, file)
                    arcname = os.path.relpath(full_path, temp_dir)
                    zip_out.write(full_path, arcname)
        
        print("  [OK] Success!")
        
    except Exception as e:
        print(f"  [ERROR] {e}")
        import traceback
        traceback.print_exc()
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
            fix_fragmented_placeholders(f)
        else:
            print(f"File not found: {f}")
    
    print("\n[OK] All templates fixed!")
