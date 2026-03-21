import os
import win32com.client as win32

base_dir = r"C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion"
plantillas_dir = os.path.join(base_dir, "plantillas")
doc_path = os.path.join(plantillas_dir, "MODIFICACION_1_JUSTIFICACION.docx")

replacements = [
    ("«Incluir las prorrogas", "{{PRORROGAS}}"),
    ("«Incluir si existen prorrogas fecha de inicio de la prórroga y nueva fecha de terminación»", "{{PRORROGAS}}"),
    ("NOMBRE COMPLETO SUPERVISOR", "{{SUPERVISOR_NOMBRE_COMPLETO}}"), # Or whatever the document says. Let's see what the document says... wait, I'll ask the user to just put it in themselves or do a generic replacement.
]

# Wait, if I do blindly "NOMBRE COMPLETO SUPERVISOR" it might not match exact text. Let me ask the user to change those specific things manually because Word often breaks them into w:t pieces. Or I can do it via COM Selection which sometimes works for simple text.
