import os
import win32com.client as win32
import time

base_dir = r"C:\Users\yesid.piedrahita\Documents\NetBeansProjects\combinacion"
doc_dir = os.path.join(base_dir, "doc")
plantillas_dir = os.path.join(base_dir, "plantillas")

files = [
    "MAJA01.04.03.P003.F001 - JUSTIFICACIÓN PARA LA MODIFICACIÓN DE CONTRATOS ajustado (3).docx",
    "MAJA01.04.03.P003.F002 -MODIFICACION DE CONTRATO - CONVENIO - ACEPTACIÓN DE OFERTA AJUSTADO (1).docx"
]

replacements_f001 = [
    ("«INCLUIR EL NOMBRE DEL ORGANISMO»", "{{ORGANISMO_ORDENADOR}}"),
    ("«Incluir el nombre o razón social del contratista»", "{{CONTRATISTA_NOMBRE}}"),
    ("«Incluir el número de identificación del Contratista»", "{{CONTRATISTA_CEDULA}}"),
    ("«Incluir el número del Contrato / Orden de Compra / Convenio»", "{{NUMERO_CONTRATO}}"),
    ("«Incluir el objeto del del Contrato / Orden de Compra / Convenio»", "{{OBJETO_CONTRACTUAL}}"),
    ("«Incluir el valor inicial pactado del Contrato / Orden de Compra / Convenio en letras y números»", "{{VALOR_CONTRATO_LETRAS}} mcte. ({{VALOR_CONTRATO}})"),
    ("«Incluir el valor total del contrato a la fecha de suscripción del presente documento»", "{{VALOR_CONTRATO_MAS_ADICION_LETRAS}} mcte. ({{VALOR_CONTRATO_MAS_ADICION}})"),
    ("«Incluir la fecha de inicio (Acta de inicio) del Contrato / Orden de Compra / Convenio»", "{{FECHA_ACTA_INICIO}}"),
    ("«Incluir la fecha de terminación a la fecha de suscripción del presente documento»", "{{FECHA_FIN_CONTRATO}}"),
    ("«Incluir la fecha de terminación del Contrato / Orden de Compra / Convenio»", "{{FECHA_FIN_CONTRATO}}"),
    ("«Incluir la fecha en que se suscribe el Contrato / Orden de Compra / Convenio»", "{{FECHA_SUSCRIPCION}}"),
    ("«Incluir si existen adiciones en letras y números»", "{{VALOR_TOTAL_ADICION_LETRAS}} ({{VALOR_TOTAL_ADICION}})"),
    ("«No_CDP_adicion»", "{{CDP_ADICION}}"),
    ("«No_RP_adicion»", "{{RP_ADICION}}"),
    ("«Fecha_del_RP_adicion»", "{{FECHA_RP_ADICION}}")
]

replacements_f002 = [
    ("«NOMBRE_DEL_CONTRATISTA»", "{{CONTRATISTA_NOMBRE}}"),
    ("«Cedula_de_ciudadania_»", "{{CONTRATISTA_CEDULA}}"),
    ("«No_Contrato, convenio o aceptación de oferta»", "{{NUMERO_CONTRATO}}"),
    ("«No_Contrato»", "{{NUMERO_CONTRATO}}"),
    ("«Fecha_Suscripción_de_contrato_»", "{{FECHA_SUSCRIPCION}}"),
    ("«fecha_acta_de_inicio_»", "{{FECHA_ACTA_INICIO}}"),
    ("«fecha_terminación_»", "{{FECHA_FIN_CONTRATO}}"),
    ("«Valor_de_la_adicion_letra_»", "{{VALOR_TOTAL_ADICION_LETRAS}}"),
    ("«Valor_de_la_adicion_numero__»", "{{VALOR_TOTAL_ADICION}}"),
    ("«objeto»", "{{OBJETO_CONTRACTUAL}}"),
    ("«No_CDP_adicion»", "{{CDP_ADICION}}"),
    ("«No_RP_adicion»", "{{RP_ADICION}}"),
    ("«Fecha_del_RP_adicion»", "{{FECHA_RP_ADICION}}")
]

try:
    word = win32.Dispatch("Word.Application")
    word.Visible = False
    word.DisplayAlerts = False
    
    for filename in os.listdir(doc_dir):
        if "MODIFICACIÓN" in filename.upper() or "MODIFICACION" in filename.upper():
            if not filename.endswith(".docx") or filename.startswith("~"):
                continue
            
            source_path = os.path.join(doc_dir, filename)
            
            if "F001" in filename:
                dest_file = "MODIFICACION_1_JUSTIFICACION.docx"
                reps = replacements_f001
            elif "F002" in filename:
                dest_file = "MODIFICACION_2_ACEPTACION.docx"
                reps = replacements_f002
            else:
                continue
                
            dest_path = os.path.join(plantillas_dir, dest_file)
            print(f"Procesando: {filename} -> {dest_file}")
            
            doc = word.Documents.Open(source_path)
            
            for find_text, replace_text in reps:
                find = doc.Content.Find
                find.ClearFormatting()
                find.Replacement.ClearFormatting()
                find.Execute(find_text, False, False, False, False, False, True, 1, False, replace_text, 2)
            
            doc.SaveAs(dest_path)
            doc.Close(False)
            print(f"  Guardado exitosamente.")
except Exception as e:
    print(f"Error: {e}")
finally:
    try:
        word.Quit()
    except:
        pass
    print("Done")
