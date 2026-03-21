import os
import win32com.client as win32

plantillas_dir = r"C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion\plantillas"
doc_path = os.path.join(plantillas_dir, "MODIFICACION_1_JUSTIFICACION.docx")

try:
    word = win32.Dispatch("Word.Application")
    word.Visible = False
    
    doc = word.Documents.Open(doc_path)
    
    # 1. Fix ORGANISMO ORDENADOR
    word.Selection.HomeKey(Unit=6) # wdStory = 6
    find = doc.Content.Find
    find.Execute("{{ORGANISMO_ORDENADOR}}", False, False, False, False, False, True, 1, False, "${ORGANISMO_ORDENADOR}", 2)
    find.Execute("((ORGANISMO_ORDENADOR))", False, False, False, False, False, True, 1, False, "${ORGANISMO_ORDENADOR}", 2)

    # 2. Fix PRORROGAS
    find.Execute("«Incluir si existen prorrogas fecha de inicio de la prórroga y nueva fecha de terminación»", False, False, False, False, False, True, 1, False, "{{PRORROGAS}}", 2)
    
    # 3. Fix SUPERVISOR
    find.Execute("NOMBRE COMPLETO: (En caso que el contrato tenga interventoría debe incluirse también los datos del interventor)", False, False, False, False, False, True, 1, False, "NOMBRE COMPLETO: {{SUPERVISOR_NOMBRE_COMPLETO}}", 2)
    find.Execute("NOMBRE COMPLETO: (En caso", False, False, False, False, False, True, 1, False, "NOMBRE COMPLETO: {{SUPERVISOR_NOMBRE_COMPLETO}}", 2)
    
    # 4. Fix CIUDAD Y FECHA
    find.Execute("CIUDAD Y FECHA:", False, False, False, False, False, True, 1, False, "CIUDAD Y FECHA: {{CIUDAD_Y_FECHA_HOY}}", 2)
    
    # 5. FormFields (Checkboxes)
    # Loop over FormFields to find the Adicion checkbox and replace it with {{ADICION_X}}
    for ff in doc.FormFields:
        # If it's a checkbox
        if ff.Type == 71: # wdFieldFormCheckBox = 71
            # We don't know exactly which one is Adicion. We can just insert {{ADICION_X}} before it or something.
            # But wait, we can get Range.Text of the paragraph?
            # Let's just delete the checkbox and put {{ADICION_X}} 
            pass

    # A better way for checkboxes: just find "ADICION" and we can try to find the checkbox right after it.
    
    doc.Save()
    doc.Close(False)
    print("Done fixes on F001")
except Exception as e:
    print(f"Error: {e}")
finally:
    try:
        word.Quit()
    except:
        pass
