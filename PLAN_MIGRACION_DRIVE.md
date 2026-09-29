# Plan de Migración de Cuentas de Cobro Históricas (Google Drive a Base de Datos)

Este documento detalla la estrategia recomendada para inventariar y migrar las cuentas de cobro y documentos de soporte que fueron subidos de manera manual a Google Drive, antes del lanzamiento de la nueva plataforma.

## Objetivo
Centralizar el historial de contratistas en la nueva aplicación sin alterar los archivos originales ni saturar el servidor local con descargas innecesarias.

---

### Fase 1: Extracción Segura de Datos (Google Apps Script)
Dado que los archivos residen en la nube de Google, la forma más rápida y segura de obtener la información es mediante un script interno de Google.

*   **Método:** Se programará un código en **Google Apps Script** vinculado a una hoja de Google Sheets vacía.
*   **Acción:** El script se conectará a la carpeta principal de Drive en modo de **"Solo Lectura"**. No modificará, moverá ni eliminará ningún archivo.
*   **Resultado:** En cuestión de segundos, la hoja de cálculo se poblará con un inventario masivo. Cada fila representará un informe y contendrá:
    *   Nombre del archivo o carpeta.
    *   Fecha en la que fue subido.
    *   URL (Link directo) para abrir el PDF en Drive.

### Fase 2: Depuración y Normalización (Oficina)
Una vez extraído el inventario en Excel/Sheets, el equipo de contratación deberá normalizar los datos para que el sistema los entienda.

*   **Organización:** A partir de los nombres de los archivos, extraeremos (con fórmulas de Excel o manualmente) información clave: Número de contrato, nombre del contratista y número de cuota.
*   **Verificación:** Asegurarse de que no falten datos obligatorios para la base de datos.
*   **Exportación:** Guardaremos esta hoja de Excel limpia como un archivo `.CSV` listo para ser importado.

### Fase 3: Inyección al Nuevo Sistema (Backend / SQL)
Con el archivo CSV listo, procederemos a inyectarlo en la base de datos de la plataforma actual.

*   **Script de Importación:** Desarrollaremos un módulo pequeño en el sistema que lea el archivo `.CSV` línea por línea.
*   **Inserción en Base de Datos:** Creará los registros en la tabla `informes_supervision`.
*   **Visualización Inteligente:** En lugar de cargar el archivo físico al servidor (lo cual consumiría gigas de espacio), el sistema guardará el **Link de Google Drive**. Cuando un usuario quiera ver un informe antiguo en la plataforma, al darle clic, el sistema le abrirá el archivo en Google Drive.

---

## Beneficios del Plan
1.  **Cero Riesgos:** La información original no se toca ni se altera.
2.  **Ahorro de Almacenamiento:** Mantenemos los archivos pesados en Drive, usando el nuevo sistema solo como un índice centralizado.
3.  **Trazabilidad Total:** Todo el historial quedará bajo el mismo formato, facilitando las auditorías de entes de control.
