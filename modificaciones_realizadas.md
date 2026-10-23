# Registro de Modificaciones Realizadas

## Fecha: 21 de Octubre de 2026
### Módulo: Gestión y Asignación de Consecutivos de Cobro

---

### 1. [ConsecutivoDAO.java](file:///src/main/java/com/combinacion/dao/ConsecutivoDAO.java)
* **Garantía DDL de la Columna `anio`:**
  * Se incluyó la definición de la columna `anio INTEGER` en la instrucción `CREATE TABLE IF NOT EXISTS consecutivos_cobro`.
  * Se añadió la instrucción preventiva:
    ```sql
    ALTER TABLE consecutivos_cobro ADD COLUMN IF NOT EXISTS anio INTEGER;
    ```
    para asegurar la integridad de la base de datos en cualquier nuevo entorno o despliegue.
* **Búsqueda Precisa por Cédula con Fallback (`obtenerConsecutivo`):**
  * Se implementó un flujo de dos niveles para la asignación de consecutivos:
    1. **Nivel 1 (Exacto):** Cruce por `cédula limpia` + `contrato` + `cuota` + `año` utilizando `regexp_replace(cedula, '[^0-9]', '', 'g') = ?`. Esto garantiza que cédulas registradas con puntos o ciudad (por ejemplo, `1.061.733.208 de Popayan`) coincidan exactamente con el valor numérico cargado (`1061733208`), eliminando cualquier riesgo de cruce o falso positivo entre contratos de distintos usuarios.
    2. **Nivel 2 (Fallback):** Búsqueda por `contrato` + `cuota` + `año` para casos donde no se disponga de la cédula.
* **Búsqueda Avanzada en Listado y Paginación (`obtenerTodosPaginados` y `contarTodos`):**
  * Se integró `c.cedula ILIKE ?` en las cláusulas de filtro. Ahora el administrador puede buscar en la tabla de consecutivos tanto por los dígitos de la cédula como por el formato textual original (incluyendo puntos o nombres de ciudad como *"Popayan"*).

---

### 2. [ConsecutivoServlet.java](file:///src/main/java/com/combinacion/servlets/ConsecutivoServlet.java)
* **Normalización en Carga Masiva (`procesarCarga`):**
  * Se añadió limpieza para remover posibles sufijos decimales `.0` procedentes de celdas numéricas formateadas por Excel en los campos `cuota` y `consecutivo`.
* **Depuración en Plantilla de Descarga (`descargarPlantilla`):**
  * Se configuró la exportación de la cédula limpia de caracteres no numéricos (`replaceAll("[^0-9]", "")`) para que el archivo Excel generado presente únicamente los dígitos puros.
  * Se ajustó el filtro de vigencia: al descargar la plantilla para un año determinado (ej. 2026), se listan todos los contratos correspondientes a ese año sin excluir aquellos cuya fecha de terminación ya haya transcurrido con respecto a la fecha actual (`CURRENT_DATE`).

---

### 3. [form_supervision.jsp](file:///src/main/webapp/form_supervision.jsp)
* **Saneamiento y Extracción de Cédula en `fetchConsecutivo()`:**
  * Se implementó el escape seguro con `<c:out>` y la expresión regular `.replace(/[^0-9]/g, '')` en JavaScript para limpiar la cédula antes de realizar la petición AJAX:
    ```javascript
    var rawCedula = '<c:out value="${contrato.contratista != null ? contrato.contratista.cedula : sessionScope.usuario.cedula}" />';
    var cedula = rawCedula ? rawCedula.replace(/[^0-9]/g, '') : '';
    ```
    Esto previene que caracteres especiales, espacios o tildes (como en *"Popayán"*) generen errores de sintaxis en el navegador.
* **Priorización de la Vigencia del Contrato:**
  * Se priorizó `${contrato.anio}` sobre el desglose de `fechaTerminacion` para asegurar que contratos de la vigencia actual que finalicen en enero del año siguiente envíen el año de vigencia correcto a la consulta de consecutivos.

---

## Fecha: 22 de Octubre de 2026

### 1. Plantillas y Generación de Informes de Supervisión en Borrador
* **[INFORME_SUPERVISION_TEMPLATE_BORRADOR.docx](file:///plantillas/INFORME_SUPERVISION_TEMPLATE_BORRADOR.docx)** y **[src/main/webapp/plantillas/INFORME_SUPERVISION_TEMPLATE_BORRADOR.docx](file:///src/main/webapp/plantillas/INFORME_SUPERVISION_TEMPLATE_BORRADOR.docx)**:
  * **Corrección de incompatibilidad con Google Docs ("No se ha podido abrir el archivo"):**
    * Se identificó que la plantilla anterior contenía fragmentación en 3 encabezados y 3 pies de página (`header1`, `header2`, `header3`) generados automáticamente por Microsoft Word sin sus correspondientes archivos de relaciones XML (`.rels`), junto con etiquetas VML con esquemas de tema no reconocidos (`fillcolor="#c0504d [3205]"`), provocando error 500 al abrir/convertir en Google Docs/Drive.
    * Se reconstruyó la plantilla de borrador sobre la base limpia y probada de la plantilla estándar (`INFORME_SUPERVISION_TEMPLATE.docx`), integrando una marca de agua diagonal **"BORRADOR"** con marcado OpenXML estándar, un único encabezado y relaciones completas.
    * Es 100% compatible tanto con Google Docs en la web, como con LibreOffice (conversión a PDF en Linux) y Microsoft Word.
* **[pom.xml](file:///pom.xml)**:
  * Se añadieron las dependencias estándar `xercesImpl` (2.12.2), `xalan` (2.7.3) y `serializer` (2.7.3) para garantizar procesadores DOM, SAX y XSLT estandarizados en entornos headless y servidores de aplicaciones.

---

### 2. [InformeSupervisionService.java](file:///src/main/java/com/combinacion/services/InformeSupervisionService.java)
* **Sincronizador y Restaurador Automático de Soportes y Evidencias en Drive (`actualizarFormatosEnDrive`):**
  * Se añadió lógica para recuperar de la papelera de Google Drive (`trashed = false`) y reubicar en la carpeta de la cuota los documentos soportes del contratista (RPC, Planilla SS, Cédula, etc.) y en la subcarpeta `EVIDENCIAS` los archivos adjuntos a las obligaciones contractuales.
  * Se garantizó que la estructura de carpetas `CUOTA X > EVIDENCIAS` se mantenga organizada automáticamente al sincronizar formatos.
* **Cálculo Consistente de Valores y Adiciones:**
  * Se homologó la lógica de valores de contratos con adición aprobada para que los valores en números y letras coincidan entre la vista de edición y la vista de lectura.

---

### 3. [form_supervision.jsp](file:///src/main/webapp/form_supervision.jsp)
* **Consistencia de Información Económica en Vista de Lectura:**
  * Se sincronizó la presentación de los valores iniciales y valores con adición en letras y números para reflejar con exactitud los datos contractuales guardados.

---

### 4. [ActualizarVerbosMasivoService.java](file:///src/main/java/com/combinacion/services/ActualizarVerbosMasivoService.java)
* **Protección de Encoding UTF-8 en Verbos Rectores:**
  * Se corrigió el manejo de caracteres especiales y acentuados para prevenir corrupción de caracteres en obligaciones y verbos rectores procesados masivamente.

