import os
import win32com.client as win32

plantillas_dir = r"C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas"
doc_path = os.path.join(plantillas_dir, "MODIFICACION_1_JUSTIFICACION.docx")

try:
    word = win32.Dispatch("Word.Application")
    word.Visible = False
    
    doc = word.Documents.Open(doc_path)
    
    word.Selection.HomeKey(Unit=6) # wdStory = 6
    find = doc.Content.Find
    
    find.Execute("«Incluir si existen aclaraciones y fecha de la misma»", False, False, False, False, False, True, 1, False, "{{ACLARACION}}", 2)
    find.Execute("«Incluir las suspensiones (Fecha de inicio y finalización)»", False, False, False, False, False, True, 1, False, "{{SUSPENSION}}", 2)
    find.Execute("«Incluir las reanudaciones (Fecha de inicio y finalización)»", False, False, False, False, False, True, 1, False, "{{REANUDACION}}", 2)
    
    doc.Save()
    doc.Close(False)
    print("Done fixes on F001 part 2")
except Exception as e:
    print(f"Error: {e}")
finally:
    try:
        word.Quit()
    except:
        pass
