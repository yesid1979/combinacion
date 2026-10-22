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
