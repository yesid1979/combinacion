import zipfile
import re
from lxml import etree

def show_placeholder_context(docx_path):
    """
    Show context around each placeholder to understand where they are
    """
    print(f"\n{'='*60}")
    print(f"File: {docx_path}")
    print('='*60)
    
    try:
        with zipfile.ZipFile(docx_path, 'r') as docx:
            xml_content = docx.read('word/document.xml')
            
        # Parse XML
        root = etree.fromstring(xml_content)
        
        # Namespaces
        ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
        
        # Find all paragraphs
        paragraphs = root.findall('.//w:p', ns)
        
        placeholder_count = 0
        
        for i, para in enumerate(paragraphs):
            # Get full text of paragraph
            text_parts = []
            for t in para.findall('.//w:t', ns):
                if t.text:
                    text_parts.append(t.text)
            
            full_text = ''.join(text_parts)
            
            # Check if it contains placeholders
            if '{{' in full_text and '}}' in full_text:
                placeholders = re.findall(r'\{\{[^}]+\}\}', full_text)
                if placeholders:
                    placeholder_count += len(placeholders)
                    print(f"\nParagraph {i+1}:")
                    print(f"  Placeholders: {placeholders}")
                    # Show context (truncate if too long)
                    context = full_text[:200] + ('...' if len(full_text) > 200 else '')
                    print(f"  Context: {context}")
        
        print(f"\nTotal placeholders found: {placeholder_count}")
        
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    files = [
        "plantillas/INVERSION_2_VERIFICACION_CUMPLIMIENTO.docx",
        "plantillas/INVERSION_4_COMPLEMENTO_CONTRATO.docx"
    ]
    
    for f in files:
        show_placeholder_context(f)
