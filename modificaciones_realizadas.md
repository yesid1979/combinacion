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

---

### 5. Flujo "Sin Revisor" (Revisión Inicial de Contratación y Paso a Revisor)
* **[lista_informes.jsp](file:///src/main/webapp/lista_informes.jsp)** y **[form_supervision.jsp](file:///src/main/webapp/form_supervision.jsp)**:
  * **Opciones para Contratación en cuenta radicada sin revisor (`contratacion_sin_revisor`):**
    1. **Dar Visto Bueno Previo / Pasar a Revisor (`VISTO BUENO CONTRATACION`)**: Abre selector dinámico para asignar el revisor deseado.
    2. **Mandar a Imprimir / Aprobación Definitiva (`APROBADA PARA IMPRESION`)**: Permite aprobar la cuenta directamente para impresión sin requerir paso adicional.
    3. **Mandar al Supervisor (`EN REVISION SUPERVISOR`)**: Permite remitir directamente al supervisor si así se requiere.
    4. **Devolver al Contratista (`DEVUELTA`)**: Requiere observación obligatoria.
  * **Opciones para el Revisor tras Visto Bueno de Contratación (`revisor_post_contratacion`):**
    1. **Mandar al Supervisor (`EN REVISION SUPERVISOR`)**
    2. **Dar Visto Bueno para Imprimir (`APROBADA PARA IMPRESION`)**
    3. **Devolver al Contratista (`DEVUELTA`)**
  * Esto aplica exclusivamente al flujo "Sin Revisor", manteniendo intacto el flujo regular donde un revisor inicial pasa a contratación.

---

### 6. Consistencia Absoluta de Valores Económicos en Contratos con Adición (Punto 4)
* **[Contrato.java](file:///src/main/java/com/combinacion/models/Contrato.java)**:
  * Se añadieron los métodos centralizados:
    * `isTieneAdicion()` / `getTieneAdicion()`: Normalización robusta de tildes y mayúsculas (`SI`, `SÍ`, `X`, `true`) y validación de adición con saldo numérico mayor a cero.
    * `getValorVigente()`: Retorna automáticamente `valorContratoMasAdicion` si el contrato cuenta con adición aprobada; de lo contrario `valorTotalNumeros`.
    * `getValorVigenteLetras()`: Retorna `valorContratoMasAdicionLetras` o `valorTotalLetras`.
* **[form_supervision.jsp](file:///src/main/webapp/form_supervision.jsp)**:
  * El campo **"Valor Total del Contrato ($)"** ahora enlaza directamente a `${contrato.valorVigente}`, mostrando de manera idéntica y consistente el valor total con adición (ej. `$33.978.000`) tanto en vista de lectura/revisión (`action=view`) como en edición.
  * Se corrigió la lógica en JavaScript para que no sobreescriba `#valor_total` con el valor base en cuotas intermedias, garantizando que el saldo por cancelar (`$11.326.000`), el valor acumulado (`$16.989.000`), la cuota (`$5.663.000`) y el valor total (`$33.978.000`) cuadren exactamente (33.978.000 - 16.989.000 - 5.663.000 = 11.326.000).
* **[SupervisionReportGenerator.java](file:///src/main/java/com/combinacion/util/SupervisionReportGenerator.java)** y **[CuentaCobroGenerator.java](file:///src/main/java/com/combinacion/util/CuentaCobroGenerator.java)**:
  * Se sincronizó la emisión de documentos Word y Excel para utilizar `contrato.getValorVigente()` y `contrato.getValorVigenteLetras()`, unificando cifras y textos en letras en todos los entregables generados.

---

## Fecha: 23 de Octubre de 2026

### 1. [InformeSupervisionService.java](file:///src/main/java/com/combinacion/services/InformeSupervisionService.java)
* **Protección de Cuentas Históricas basada en Contenido (`esCuotaManualHistorica`):**
  * Se reemplazó la validación basada únicamente en la fecha de creación de la carpeta en Drive (`createdTime < 15 de Octubre`) por una inspección basada en archivos oficiales existentes (`DS-`, `INFORME`, `CUENTA`, `FACTURA`) con fecha previa al corte.
  * Si la carpeta en producción ya contenía documentos oficiales previos cerrados, se mantiene protegida al 100% (evitando que cuotas viejas tramitadas manualmente sean sobreescritas).
  * Si la carpeta fue creada con anticipación pero no contiene documentos oficiales previos (como cuotas actuales vacías o solo con evidencias), el sistema reconoce que es la cuenta en curso y deposita los formatos oficiales en la carpeta de producción correspondiente.
* **Sincronización Directa y Transparente con Google Drive (`sincronizarFormatosDrive`):**
  * La acción manual de sincronización ahora se ejecuta directamente con respuesta garantizada, permitiendo informar en pantalla el éxito o el error exacto de la operación.
  * Se añadió soporte para redirección inteligente según el origen (`from=lista` o desde el formulario de edición).

### 2. [form_supervision.jsp](file:///src/main/webapp/form_supervision.jsp) y [lista_informes.jsp](file:///src/main/webapp/lista_informes.jsp)
* **Feedback Visual en el Botón "Sincronizar Drive":**
  * Se integró estado de carga dinámico (`spinner-border` y deshabilitación al hacer clic) para informar al usuario que la sincronización con Google Drive está en progreso, evitando clics duplicados y asegurando que al recargar la página los archivos ya estén disponibles en Drive.

### 3. [InformeSupervisionService.java](file:///src/main/java/com/combinacion/services/InformeSupervisionService.java) - Optimización de Descarga de Documentos Aprobados para Impresión (`descargarInforme`)
* **Datos 100% Actualizados y Vigentes:**
  * Al descargar el paquete ZIP cuando la cuenta está aprobada para impresión, el sistema consulta los datos frescos y vigentes del contrato e informe directamente desde la base de datos, regenerando al vuelo el Word de Supervisión, el Excel/PDF de Cuenta de Cobro y el Word/PDF de Gestión con la última información guardada.
  * **Preservación del Informe de Supervisión en Word (`.docx`):** Se mantiene deliberadamente en formato editable Word para permitir que el contratista lo imprima o entregue físicamente/digitalmente para la firma del supervisor, antes de subir el PDF firmado definitivo.
* **Homologación de Nomenclatura para Cuotas de Adición:**
  * Se unificó la numeración de los archivos dentro del ZIP descargado para que coincida con la estructura en Google Drive (`actualizarFormatosEnDrive`):
    * `6. INFORME DE SUPERVISIÓN CUOTA X - Nombre.docx`
    * `4. DS-[Contrato]-[Consecutivo] CUOTA X Nombre.xlsx` / `.pdf`
    * `13. INFORME DE GESTIÓN CUOTA X - Nombre.docx` / `.pdf`
  * Extracción robusta del número base del contrato (`shortContrato`) para evitar inconsistencias en el prefijo `DS-`.
* **Resiliencia del Flujo ZIP ante Errores de Google Drive:**
  * Se corrigió la apertura prematura de entradas ZIP (`zos.putNextEntry`): ahora solo se inicializa la entrada si el flujo (`InputStream`) del anexo en Drive se obtuvo exitosamente y no es nulo.
  * Se encapsuló la escritura de cada entrada en bloques `try-finally` para asegurar que `zos.closeEntry()` siempre se invoque, evitando que excepciones de red o timeouts corrompan el ZIP con errores `ZipException: entry already open` o caídas HTTP 500/504.
* **Limpieza de Recursos y Archivos Temporales:**
  * Se incorporó un bloque `finally` al final del proceso de descarga para eliminar de forma inmediata todos los archivos temporales (`docxFile`, `xlsxFile`, `xlsxPdfFile`, `gestionFile`, `gestionPdfFile`, `tempSegSoc`, `mergedFile`), previniendo fugas de almacenamiento en el servidor.

### 4. [SupervisionReportGenerator.java](file:///src/main/java/com/combinacion/util/SupervisionReportGenerator.java) - Ajuste en Firmas de Apoyo a la Supervisión
* **Eliminación de la Frase Condicional:**
  * Se eliminó el texto `(Incluir cuando aplique)` del rótulo de firma del apoyo a la supervisión `${TEXTO_APOYO}`.
  * Ahora se genera de manera limpia y profesional: `Nombre y firma del Apoyo a la Supervisión`, en concordancia con el rótulo del supervisor (`Nombre y firma del Supervisor`).



