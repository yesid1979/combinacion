import os

def update_service():
    path = r"c:\Users\Soporte y Desarrollo\Documents\NetBeansProjects\combinacion\src\main\java\com\combinacion\services\InformeSupervisionService.java"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Map Form To Model
    if "info.setEstadoRadicacion(f.estadoRadicacion);" not in content:
        content = content.replace(
            "info.setSoportesJson(f.soportesJson);",
            "info.setSoportesJson(f.soportesJson);\n        info.setEstadoRadicacion(f.estadoRadicacion);\n        info.setIdRevisorAsignado(f.idRevisorAsignado);"
        )
        
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("InformeSupervisionService actualizado.")

if __name__ == '__main__':
    update_service()
