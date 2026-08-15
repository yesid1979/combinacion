# Plan de Migración: Cuentas de Cobro Manuales desde Google Drive

Este documento describe la estrategia y los requisitos necesarios para realizar la **Carga Inicial de Datos** o migración de las cuentas de cobro (soportes y metadatos) que fueron radicadas manualmente y se encuentran almacenadas en una carpeta de Google Drive.

## 🎯 Objetivo
Automatizar la lectura, extracción de información e inserción en la base de datos del sistema de todas las cuentas de cobro generadas históricamente de forma manual. Esto asegurará que el nuevo sistema tenga un histórico completo antes de salir a producción, permitiendo a los contratistas continuar su proceso 100% digital sin perder trazabilidad.

---

## 1. Contexto Actual
- Las cuentas de cobro antiguas están ubicadas en una carpeta específica dentro de la cuenta de Google Drive.
- **Ventaja clave:** El proyecto actual ya cuenta con integración y credenciales configuradas para conectarse a esa misma cuenta de Google Drive, lo que elimina la necesidad de configurar autenticaciones o permisos desde cero.
- **Carpeta Única Permanente:** No es necesario descargar ni mover los archivos a otro servidor. Las cuentas antiguas se quedarán en esa misma carpeta de Drive, ya que la idea es que las **nuevas cuentas de cobro** que se generen en el sistema también se sigan guardando allí. Drive seguirá siendo el repositorio oficial.

---

## 2. Requisitos Previos

Para que el script de migración funcione correctamente, se necesita tener a disposición lo siguiente:

1. **ID de la Carpeta de Drive:** 
   - El identificador único de la carpeta raíz que contiene las cuentas manuales (se obtiene de la URL de Drive: `https://drive.google.com/drive/folders/ESTE_ES_EL_ID`).

2. **Acceso a la Conexión Actual de Google:**
   - Reutilizar el archivo de credenciales JSON (Service Account) o los tokens OAuth2 que ya están siendo utilizados en el sistema para la autenticación con las APIs de Google.

3. **Herramientas de Extracción de Datos (OCR / Inteligencia Artificial):**
   - Dependiendo de si los archivos son PDF o Imágenes, se requiere integrar una herramienta de reconocimiento.
   - **Opción recomendada (Gemini / Google Cloud Vision / DocumentAI):** Permite procesar la imagen/PDF de la cuenta de cobro y extraer inteligentemente y con precisión datos clave en formato estructurado (JSON), como:
     - Nombre del contratista o Cédula.
     - Número de la cuenta de cobro.
     - Fecha de radicación.
     - Valor total a cobrar.
     - Concepto o descripción (opcional).

4. **Mapeo de la Base de Datos:**
   - Identificar con exactitud la tabla en la base de datos (por ejemplo, `radicacion_cuentas` o similar) y las columnas donde se insertarán estos registros históricos.
   - Establecer un "estado" por defecto para estas cuentas migradas (ej. `MIGRADA`, `RADICADA_HISTORICA` o `PAGADA` dependiendo del caso).

---

## 3. Fases del Proceso de Migración

### Fase 1: Lectura del Directorio
- Ejecutar una llamada a la **API de Google Drive (`drive.files.list`)** filtrando por el `ID de la Carpeta` padre.
- Obtener un listado de todos los archivos (IDs y Nombres).

### Fase 2: Lectura y Procesamiento (Sin mover archivos)
- Iterar sobre la lista de archivos.
- Por cada archivo, enviar su contenido en memoria (o mediante su enlace/ID si la IA lo soporta nativamente) directamente a la API de IA (Ej. Gemini API) sin necesidad de guardarlo localmente.
- Pedirle a la IA/OCR que analice el documento y retorne un objeto estructurado con las variables de la cuenta de cobro.

### Fase 3: Validación e Inserción en Base de Datos (BD)
- Validar que los datos extraídos no estén vacíos y que correspondan a un contratista existente en el sistema.
- Hacer un `INSERT` en la base de datos relacionando al contratista.
- Guardar el enlace o ID original del archivo en Drive dentro del registro de la base de datos, para que el soporte original se pueda consultar o descargar desde la interfaz web.

### Fase 4: Reporte de Errores (Log)
- Si algún archivo no es legible, está corrupto o la IA no logró extraer la cédula/valor, se debe guardar en un archivo de texto (`log_errores_migracion.txt`) indicando el nombre del archivo en Drive para revisión humana.

---

## 4. Consideraciones Finales antes de Salir a Producción
1. **Ambiente de Prueba:** Realizar la migración primero en una base de datos de pruebas (Staging) tomando solo unos 10 archivos de la carpeta para comprobar la fiabilidad del OCR/IA.
2. **Carga Única:** Una vez comprobado el éxito, ejecutar el script completo contra la base de datos de Producción horas antes del lanzamiento oficial.
3. **Continuidad:** Una vez realizada la carga masiva, los contratistas ingresarán a la plataforma, verán su historial precargado y podrán crear la *siguiente* cuenta de cobro sin perder el consecutivo o el hilo de su gestión.
