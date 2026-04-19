import os
import win32com.client as win32

plantillas_dir = r"C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas"
doc_path = os.path.join(plantillas_dir, "MODIFICACION_1_JUSTIFICACION.docx")

try:
    word = win32.Dispatch("Word.Application")
    word.Visible = False
    
    doc = word.Documents.Open(doc_path)
    
    # Checkboxes:
    # Let's find FormFields where Type = 71
    form_fields = doc.FormFields
    print(f"Total FormFields: {form_fields.Count}")
    
    i = 1
    # Often ADICION is the first checkbox. However, doc.FormFields object isn't perfectly iterating in a normal way
    # Let's just iterate by index because COM collections are 1-based
    
    count = form_fields.Count
    for idx in range(count, 0, -1):
        ff = form_fields(idx)
        if ff.Type == 71: # Checkbox
            # If we know Adicion is the very first one:
            # But let's look at the paragraph text
            para_text = ff.Range.Paragraphs(1).Range.Text.upper()
            if "ADICION" in para_text or "ADICIÓN" in para_text:
                if "SUSPENSION" not in para_text: # Wait they are on same line maybe?
                    pass
            
    # Given they are in a table, the first CheckBox is ADICION.
    if count >= 1:
        # Reemplazar ADICION
        form_fields(1).Range.Text = " {{ADICION_X}}"
    
    doc.Save()
    doc.Close(False)
    print("Checkboxes fixed")
except Exception as e:
    print(f"Error: {e}")
finally:
    try:
        word.Quit()
    except:
        pass
