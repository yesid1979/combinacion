
import zipfile
import re
import os
import shutil

def process_docx(file_path, replacements):
    print(f"Processing: {file_path}")
    
    temp_dir = "temp_docx_edit"
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    os.makedirs(temp_dir)
    
    try:
        with zipfile.ZipFile(file_path, 'r') as zip_ref:
            zip_ref.extractall(temp_dir)
            
        xml_path = os.path.join(temp_dir, 'word', 'document.xml')
        with open(xml_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Regex to find runs with highlight
        # We look for <w:r ...> ... <w:highlight w:val="yellow"/> ... </w:r>
        # This is complex because attributes order varies.
        # We will iterate over all <w:r> tags using a non-greedy wildcard.
        
        # Simplified approach: Split into runs, process each run
        # Note: This simple splitting might break if tags act unexpectedly, but standard docx structure usually keeps <w:r> distinct.
        
        new_content = ""
        last_pos = 0
        
        # Pattern to find starts of runs
        run_pattern = re.compile(r'(<w:r(?: [^>]*)?>)(.*?)(</w:r>)', re.DOTALL)
        
        for match in run_pattern.finditer(content):
            # Append text before this run
            new_content += content[last_pos:match.start()]
            
            run_tag_start = match.group(1)
            run_body = match.group(2)
            run_tag_end = match.group(3)
            
            # Check for yellow highlight in this run's properties (w:rPr)
            if 'w:val="yellow"' in run_body and '<w:highlight' in run_body:
                
                # Extract text to identify which placeholder to use
                text_match = re.search(r'<w:t(?: [^>]*)?>(.*?)</w:t>', run_body, re.DOTALL)
                current_text = text_match.group(1) if text_match else ""
                
                # Logic to determine replacement
                replacement_placeholder = None
                
                # Normalize text for matching (remove encoding artifacts if any)
                clean_text = current_text.lower()
                
                print(f"  Found highlighted text: '{current_text}'")

                # Mapping Logic
                if "profesional" in clean_text or "apoyo" in clean_text:
                    replacement_placeholder = "{{NIVEL_CONTRATO}}"
                elif "formación" in clean_text or "título" in clean_text or "titulo" in clean_text:
                    replacement_placeholder = "{{PERFIL_FORMACION}}"
                elif "experiencia" in clean_text:
                     replacement_placeholder = "{{PERFIL_EXPERIENCIA}}"
                elif "objeto" in clean_text:
                    replacement_placeholder = "{{OBJETO_CONTRACTUAL}}"
                elif "actividades" in clean_text:
                    if "ebi" in clean_text:
                        replacement_placeholder = "{{ACTIVIDADES_FICHA_EBI}}"
                    else:
                        replacement_placeholder = "{{ACTIVIDADES_CONTRACTUALES}}"
                elif "valor" in clean_text and "total" in clean_text:
                    if "letras" in clean_text: # Specific for Inv 2
                         replacement_placeholder = "{{VALOR_CONTRATO_LETRAS}}"
                    else:
                         replacement_placeholder = "{{VALOR_CONTRATO}}"
                elif "valor" in clean_text and "cuota" in clean_text:
                    replacement_placeholder = "{{VALOR_CUOTA_NUMERO}}"
                elif "supervisor" in clean_text:
                    replacement_placeholder = "{{NOMBRE_SUPERVISOR}}"
                elif "juridico" in clean_text:
                    replacement_placeholder = "{{NOMBRE_ESTRUCTURADOR_JURIDICO}}"
                elif "financiero" in clean_text:
                    replacement_placeholder = "{{NOMBRE_ESTRUCTURADOR_FINANCIERO}}"

                if replacement_placeholder:
                    print(f"    -> Replaced with: {replacement_placeholder}")
                    
                    # 1. Remove highlight tag
                    run_body = re.sub(r'<w:highlight [^>]*/>', '', run_body)
                    
                    # 2. Replace text
                    # We rebuild the w:t tag with the new placeholder
                    run_body = re.sub(r'<w:t(?: [^>]*)?>.*?</w:t>', f'<w:t>{replacement_placeholder}</w:t>', run_body, flags=re.DOTALL)
                else:
                    print("    -> No matching placeholder found, keeping original.")
            
            new_content += run_tag_start + run_body + run_tag_end
            last_pos = match.end()
            
        new_content += content[last_pos:]
        
        # Write back XML
        with open(xml_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
            
        # Re-zip
        with zipfile.ZipFile(file_path, 'w', zipfile.ZIP_DEFLATED) as zip_out:
            for root, dirs, files in os.walk(temp_dir):
                for file in files:
                    full_path = os.path.join(root, file)
                    arcname = os.path.relpath(full_path, temp_dir)
                    zip_out.write(full_path, arcname)
                    
        print("Success!\n")

    except Exception as e:
        print(f"Error processing {file_path}: {e}")
    finally:
        if os.path.exists(temp_dir):
            shutil.rmtree(temp_dir)

if __name__ == "__main__":
    files = [
        "plantillas/INVERSION_1_ESTUDIOS_PREVIOS.docx",
        "plantillas/INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx",
        "plantillas/INVERSION_4_COMPLEMENTO_CONTRATO.docx" # Skipping 3 as no highlights found
    ]
    
    for f in files:
        if os.path.exists(f):
            process_docx(f, {})
        else:
            print(f"File not found: {f}")
