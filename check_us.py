import zipfile, re, os

src = r'c:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas\MODIFICACION_2_ACEPTACION.docx'

with zipfile.ZipFile(src, 'r') as zin:
    content = zin.read('word/document.xml').decode('utf-8')
    all_files = {name: zin.read(name) for name in zin.namelist()}

# We have stuff like 'MODIFICACI\u00d3N No. ______' etc.
# Note that they are spread in XML: <w:t>MODIFICACIN No. </w:t> ... <w:t>______</w:t>
# Or inside a single <w:t>
# Let's write a robust regex to just match underscores and replace with {{NUMERO_MODIFICACION}}. 
# Wait, replacing all underscores could break other things.
# Let's find "No." followed by underscores, ignoring XML tags.

def replace_no_underscores(xml_str):
    # This is a bit tricky. Let's find all sequences of 4 or more underscores inside <w:t> tags
    # and replace them IF they are preceded by "No." or "Modificacin No."
    
    # Simpler approach: find any stretch of 3 or more underscores inside <w:t>...</w:t>
    
    # In Word, underscores might be typed directly as '_____'
    # Let's search for "No." or "No " then we replace underscores.
    
    # Just replacing all chunks of 4+ underscores inside w:t tags with {{NUMERO_MODIFICACION}} 
    # might be dangerous if there are underscores for other lines (like signatures).
    # Signatures usually have more than 20 underscores.
    # Lines for dates have _____. e.g. "a los _____ dias del mes ___"
    pass

# Let's find exactly the text nodes with underscores that need replacement.
# They are near "Modificacin" or "MODIFICACIN".

matches = re.finditer(r'MODIFICACI\u00d3N\s(al contrato[^\<]+)?No\.\s*(<[^>]+>)*\s*_{3,8}', content, flags=re.IGNORECASE)
for m in matches:
    print('Found pattern:', m.group(0))

# Instead of complex regex, let's just do an exact XML string replacement.
print('---')
# Find "No. " followed by underscores inside <w:t>
idx = 0
while True:
    idx = content.find('No.', idx)
    if idx == -1: break
    
    # Context of 50 chars after
    ctx = content[idx:idx+150]
    plain = re.sub(r'<[^>]+>', '', ctx)
    if '_' in plain and 'MODIFICACI' in content[max(0, idx-100):idx+100].upper():
        print(f"Context matches '{plain[:40]}'")
        
        # Replace the underscores in this specific context
        # Let's look exactly at how the underscores are encoded.
        m_us = re.search(r'>(\s*_{4,9}\s*)<', ctx)
        if m_us:
            old_str = m_us.group(1)
            print("Found underscores to replace:", old_str)
            # content = content[:idx] + content[idx:idx+150].replace(old_str, '{{NUMERO_MODIFICACION}}') + content[idx+150:]
            
    idx += 3

