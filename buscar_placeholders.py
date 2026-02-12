#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import zipfile
import xml.etree.ElementTree as ET

def buscar_en_docx(archivo):
    """Busca placeholders en un archivo DOCX"""
    try:
        with zipfile.ZipFile(archivo, 'r') as zip_ref:
            # Leer el documento principal
            xml_content = zip_ref.read('word/document.xml')
            
            # Decodificar y buscar
            texto = xml_content.decode('utf-8')
            
            # Buscar NIVEL_CONTRATO
            if 'NIVEL_CONTRATO' in texto:
                print("✓ Encontrado 'NIVEL_CONTRATO' en el documento")
                
                # Contar ocurrencias
                count = texto.count('{{NIVEL_CONTRATO}}')
                print(f"  - Ocurrencias de '{{{{NIVEL_CONTRATO}}}}': {count}")
                
                # Buscar contexto
                lines = texto.split('>')
                for i, line in enumerate(lines):
                    if 'NIVEL_CONTRATO' in line:
                        print(f"\n  Contexto línea {i}:")
                        # Mostrar líneas alrededor
                        start = max(0, i-2)
                        end = min(len(lines), i+3)
                        for j in range(start, end):
                            if 'NIVEL_CONTRATO' in lines[j]:
                                print(f"    >>> {lines[j][:200]}")
                            else:
                                print(f"        {lines[j][:200]}")
            else:
                print("✗ No se encontró 'NIVEL_CONTRATO' en el documento")
                
            # Buscar PROFESIONAL
            if 'PROFESIONAL' in texto:
                count_prof = texto.count('PROFESIONAL')
                print(f"\n✓ Encontrado 'PROFESIONAL': {count_prof} veces")
                
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    archivo = "plantillas/INVERSION_1_ESTUDIOS_PREVIOS.docx"
    print(f"Buscando placeholders en: {archivo}\n")
    buscar_en_docx(archivo)
