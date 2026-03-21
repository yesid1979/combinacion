import os
import win32com.client as win32

plantillas_dir = r"C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas"
doc_path = os.path.join(plantillas_dir, "MODIFICACION_1_JUSTIFICACION.docx")

try:
    word = win32.Dispatch("Word.Application")
    word.Visible = False
    
    doc = word.Documents.Open(doc_path)
    find = doc.Content.Find

    # Revertir labels al texto original (sin etiqueta pegada)
    replacements = [
        ("Nombre del Cesionario: {{CESIONARIO_NOMBRE}}", "Nombre del Cesionario:"),
        ("Identificación del Cesionario: {{CESIONARIO_IDENTIFICACION}}", "Identificación del Cesionario:"),
        ("Fecha de Cesión: {{FECHA_CESION}}", "Fecha de Cesión:"),
        # Tambien limpiar si quedaron etiquetas sueltas en las celdas de valor
        ("{{CESIONARIO_NOMBRE}}", "N/A"),
        ("{{CESIONARIO_IDENTIFICACION}}", "N/A"),
        ("{{FECHA_CESION}}", "N/A"),
    ]

    for old_text, new_text in replacements:
        find.Execute(old_text, False, False, False, False, False, True, 1, False, new_text, 2)
        print(f"  '{old_text}' -> '{new_text}'")
    
    doc.Save()
    doc.Close(False)
    print("Listo - campos de cesion corregidos.")
except Exception as e:
    print(f"Error: {e}")
finally:
    try:
        word.Quit()
    except:
        pass
