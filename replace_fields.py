import os
import win32com.client as win32

base_dir = r"C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion"
doc_dir = os.path.join(base_dir, "doc")
plantillas_dir = os.path.join(base_dir, "plantillas")

replacements_f002 = {
    "NOMBRE_DEL_CONTRATISTA": "{{CONTRATISTA_NOMBRE}}",
    "Cedula_de_ciudadania_": "{{CONTRATISTA_CEDULA}}",
    "No_Contrato": "{{NUMERO_CONTRATO}}",
    "Fecha_Suscripción_de_contrato_": "{{FECHA_SUSCRIPCION}}",
    "fecha_acta_de_inicio_": "{{FECHA_ACTA_INICIO}}",
    "fecha_terminación_": "{{FECHA_FIN_CONTRATO}}",
    "Valor_de_la_adicion_letra_": "{{VALOR_TOTAL_ADICION_LETRAS}}",
    "Valor_de_la_adicion_numero__": "{{VALOR_TOTAL_ADICION}}",
    "objeto": "{{OBJETO_CONTRACTUAL}}"
}

try:
    word = win32.Dispatch("Word.Application")
    word.Visible = False
    word.DisplayAlerts = False
    
    filename = "MAJA01.04.03.P003.F002 -MODIFICACION DE CONTRATO - CONVENIO - ACEPTACIÓN DE OFERTA AJUSTADO (1).docx"
    source_path = os.path.join(doc_dir, filename)
    dest_path = os.path.join(plantillas_dir, "MODIFICACION_2_ACEPTACION.docx")
    
    print(f"Procesando: {filename}")
    doc = word.Documents.Open(source_path)
    
    # Process Merge Fields
    fields_to_delete = []
    
    # Loop over fields
    for field in doc.Fields:
        # Check if it's a MERGEFIELD
        # WdFieldType.wdFieldMergeField is 59
        if field.Type == 59:
            code_text = field.Code.Text
            # Go through replacements
            replaced = False
            for k, v in replacements_f002.items():
                if k in code_text:
                    # Select the field and replace with text
                    field.Select()
                    word.Selection.Text = v
                    replaced = True
                    break
    
    doc.SaveAs(dest_path)
    doc.Close(False)
    print(f"Guardado exitosamente en plantillas/MODIFICACION_2_ACEPTACION.docx")
except Exception as e:
    print(f"Error: {e}")
finally:
    try:
        word.Quit()
    except:
        pass
    print("Done")
