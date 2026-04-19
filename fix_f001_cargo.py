import os
import win32com.client as win32

plantillas_dir = r"C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas"
doc_path = os.path.join(plantillas_dir, "MODIFICACION_1_JUSTIFICACION.docx")

try:
    word = win32.Dispatch("Word.Application")
    word.Visible = False
    doc = word.Documents.Open(doc_path)
    find = doc.Content.Find

    # Reemplazar el campo CARGO en el documento
    replacements = [
        ("CARGO:", "CARGO: {{SUPERVISOR_CARGO}}"),
    ]
    for old_text, new_text in replacements:
        result = find.Execute(old_text, False, False, False, False, False, True, 1, False, new_text, 2)
        print(f"  '{old_text}' -> '{new_text}' | Encontrado: {result}")

    doc.Save()
    doc.Close(False)
    print("Listo - campo CARGO actualizado.")
except Exception as e:
    print(f"Error: {e}")
finally:
    try:
        word.Quit()
    except:
        pass
