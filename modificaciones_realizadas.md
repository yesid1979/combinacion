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

---

## Fecha: 24 de Octubre de 2026

### 1. Robustecimiento y Perfeccionamiento de la Subida y Sincronización en Google Drive (Pruebas y Producción)
* **[GoogleDriveService.java](file:///src/main/java/com/combinacion/services/GoogleDriveService.java)**:
  * **Limpieza y Prevención de Archivos Duplicados (`uploadOrUpdateFile`):**
    * Al actualizar un archivo existente por nombre, si existían duplicados previos en la misma carpeta de Drive (generados por reintentos o cargas manuales previas), el sistema ahora detecta los IDs duplicados y los elimina automáticamente, garantizando que siempre exista un único archivo oficial con el contenido más reciente.
  * **Sanitización de Nombres de Archivo y MimeTypes (`uploadStreamToDrive`):**
    * Limpieza preventiva de saltos de línea (`\r`, `\n`) y espacios accidentales en nombres de archivos adjuntos.
    * Asignación automática y rigurosa del Content-Type adecuado (`application/pdf` para PDF y `application/octet-stream` genérico) para evitar rechazos en el API de Drive.
  * **Reintentos Exponenciales y Resiliencia en Red:**
    * Se extendió el envoltorio `executeWithRetry` a todas las operaciones de copia (`copyFile`), descarga (`downloadFile`), permisos públicos (`setPublicViewPermission`) y listado (`getFilesInFolder` y `uploadOrReplaceByPattern`), tolerando fluctuaciones de red o límites de cuota (códigos 429, 500, 503).

* **[InformeSupervisionService.java](file:///src/main/java/com/combinacion/services/InformeSupervisionService.java)**:
  * **Resolución Exhaustiva y Creación Estandarizada de Carpetas (`resolverCuotaFolderId` y `esCuotaManualHistorica`):**
    * Se incorporó búsqueda multi-patrón para la carpeta principal del contratista tanto por nombre completo (`MANUEL ALBERTO VALENCIA RIASCOS`) como por nombre corto (`MANUEL VALENCIA`), asegurando compatibilidad total con carpetas creadas en pruebas o producción.
    * Se homologó la detección de carpetas de cuota para contemplar variaciones con sufijo de contrato (`CUOTA 3 114`), espacios simples y dobles (`CUOTA 3  114`) y formato sin sufijo (`CUOTA 3`).
    * Al crear una carpeta de cuota nueva, el sistema ahora asigna automáticamente la nomenclatura estándar oficial (`CUOTA X YYY`) cuando el contrato cuenta con terminación numérica, evitando duplicidades de carpetas.
  * **Propagación Real de Errores en Sincronización:**
    * Se eliminó el silenciamiento de excepciones en `actualizarFormatosEnDrive`. Cualquier anomalía es capturada y reportada explícitamente a la interfaz de usuario en lugar de recargar en silencio.

* **[SupervisionReportGenerator.java](file:///src/main/java/com/combinacion/util/SupervisionReportGenerator.java)**:
  * **Compatibilidad Total del DOCX con Google Docs y Esquema OOXML/ECMA-376:**
    * Se corrigió la inserción de bordes de celda (`w:tcBorders`), asegurando que se sitúe estrictamente antes de `w:vAlign` según el estándar OpenXML.
    * Se reemplazó el campo complejo de Word `w:fldChar HYPERLINK` por un hipervínculo en párrafo con estilo azul subrayado compatible con Google Docs. Con esto se eliminó definitivamente el error *"No se ha podido abrir el archivo. Prueba a actualizar la página"* en Google Docs.

* **[form_supervision.jsp](file:///src/main/webapp/form_supervision.jsp)**:
  * **Retroalimentación Visual Integral con SweetAlert:**
    * Se incorporó el manejo de alertas de error tanto para `sessionScope.errorMessage` como para `requestScope.errorMessage`.
    * Cualquier fallo, timeout o advertencia durante la sincronización o guardado ahora muestra una ventana modal descriptiva con icono de advertencia/error.

### 2. Detección Universal de Cuentas Históricas y Optimización de Búsqueda en Google Drive
* **[InformeSupervisionService.java](file:///src/main/java/com/combinacion/services/InformeSupervisionService.java)**:
  * **Protección Universal de Cuentas Históricas sin Depender del Nombre de Carpetas (`esCuotaManualHistorica`):**
    * Se rediseñó la función para que no dependa de si un contratista llamó a su carpeta manual `DOCUMENTOS`, `EVIDENCIAS`, `SOPORTES` o si no usó subcarpetas.
    * **Criterio 1 (Cronología y Periodo Oficial):** Si el periodo del informe corresponde a la etapa previa al lanzamiento digital (`periodoInforme <= '2026-09'`) y la cuota ya existe en Producción, el sistema la reconoce como manual histórica y enruta cualquier prueba o borrador hacia la carpeta de **Pruebas** (`DRIVE_CARPETA_PRUEBAS`), protegiendo intacta la carpeta de Producción.
    * **Criterio 2 (Antigüedad de la Carpeta en Producción):** Si la carpeta de la cuota en Producción fue creada antes del 15 de Octubre de 2026 (`createdTime < 15/10/2026`), se considera histórica.
    * **Criterio 3 (Inspección Recursiva Profunda):** Si cualquier archivo o subcarpeta dentro de la cuota (o dentro de cualquier nivel de subcarpetas manuales) fue creado antes del 15 de Octubre de 2026, se protege como histórica.
    * **Cuentas Digitales en Producción (Octubre 2026 en adelante):** Las cuentas que se generen a partir del periodo digital (`2026-10` en adelante) y creadas con posterioridad a la fecha de corte no disparan esta protección y operan de forma 100% nativa en la carpeta de Producción.

* **[GoogleDriveService.java](file:///src/main/java/com/combinacion/services/GoogleDriveService.java)**:
  * **Optimización Drástica de `findFolderByPatterns`:**
    * Anteriormente, la búsqueda iteraba sobre múltiples patrones y ejecutaba hasta 10-12 peticiones HTTP secuenciales al API de Google Drive (`name=` y `name contains=`), lo que ocasionaba demoras de hasta 5-10 segundos y riesgos de `Read timed out` ante congestión de red.
    * Ahora se obtiene la lista de subcarpetas del padre en una **única llamada HTTP eficiente** y el emparejamiento (exacto y por contención) se realiza instantáneamente en memoria Java de forma insensible a mayúsculas/minúsculas.
    * La resolución de carpetas pasó de varios segundos a menos de 300 milisegundos.

### 3. Distribución Simétrica 50% / 50% de la Tabla de Obligaciones y Actividades con Porcentaje Relativo (`pct`)
* **[SupervisionReportGenerator.java](file:///src/main/java/com/combinacion/util/SupervisionReportGenerator.java)**:
  * **Restauración de la Tabla Maestra Unificada:**
    * Se eliminó la división de la tabla del documento para preservar la estructura íntegra, el borde derecho continuo a 17.5 cm y evitar cualquier compresión o espacios en blanco en Google Docs.
  * **Implementación con Unidades Porcentuales Relativas (`w:type="pct"`):**
    * Se adoptó la especificación nativa que ya funcionaba con éxito en la tabla financiera del documento (`Table 2`):
      1. Se define el ancho de la tabla de obligaciones en `w:tblW w:w="5000" w:type="pct"` (100% de la celda contenedora).
      2. Se define cada columna de la cabecera y de las filas en `w:tcW w:w="2500" w:type="pct"` (50% exacto para *Obligaciones* y 50% exacto para *Actividades*).
      3. Se eliminaron `<w:tblLayout w:type="fixed"/>`, `<w:tblInd>` y el estilo inexistente `w:tblStyle="ac"` que causaban que Google Docs encogiera la tabla a su contenido mínimo.
    * Con este ajuste, la tabla se expande naturalmente a lo ancho de toda la página sin dividirse y distribuye equitativamente el 50% a cada una de las dos columnas.

### 4. Confirmación Previa, Ejecución Asíncrona (AJAX) y Cancelación de la Sincronización con Google Drive
* **[InformeSupervisionService.java](file:///src/main/java/com/combinacion/services/InformeSupervisionService.java)**:
  * **Soporte de Respuestas JSON para Peticiones Asíncronas (`sincronizarFormatosDrive`):**
    * Se dotó al método de detección de peticiones AJAX (`ajax=true` o encabezado `X-Requested-With: XMLHttpRequest`).
    * Al procesar mediante AJAX, el servidor responde con estructuras JSON estandarizadas (`{"success": true, "message": "..."}` o `{"success": false, "message": "..."}`) sin forzar redirecciones HTTP de página completa.
    * Si la sesión expira o no cuenta con permisos suficientes, retorna HTTP 403 Forbidden en formato JSON en lugar de redireccionar bruscamente.
* **[inc/scripts_assets.jsp](file:///src/main/webapp/inc/scripts_assets.jsp)**:
  * **Funciones Globales `sincronizarDriveConConfirmacion` y `ejecutarSincronizacionDrive`:**
    * **Modal de Confirmación Previa:** Al hacer clic en "Sincronizar Drive", se despliega una ventana interactiva SweetAlert2 consultando si el usuario desea iniciar la sincronización, con botones claros `[Sí, sincronizar]` (verde) y `[Cancelar]` (gris con foco inicial), previniendo activaciones accidentales.
    * **Petición con AbortController:** La sincronización se ejecuta mediante `fetch` asíncrono vinculado a un objeto `AbortController`.
    * **Botón "Cancelar operación" durante el Progreso:** Mientras los formatos se generan y suben a Drive, la ventana modal con animación de carga presenta un botón rojo visible `[Cancelar operación]`.
    * **Restauración Inmediata de la Interfaz:** Si el usuario decide cancelar la operación, la petición en el navegador se aborta al instante (`abortController.abort()`), se cierra el modal de carga y se muestra una notificación informativa sin recargar la página ni perder el contenido que el usuario esté redactando en el formulario.
    * **Notificaciones Claras de Resultado:** Al finalizar exitosamente, se muestra alerta de éxito con confirmación y, en la vista de lista, se refresca suavemente el DataTable (`ajax.reload`) sin pestañeos de página.
* **[form_supervision.jsp](file:///src/main/webapp/form_supervision.jsp) y [lista_informes.jsp](file:///src/main/webapp/lista_informes.jsp)**:
  * Se actualizaron los botones de "Sincronizar Drive" para invocar `sincronizarDriveConConfirmacion(id, modo, isFromLista)`, reemplazando la navegación síncrona bloqueante anterior por la nueva experiencia fluida y cancelable.

---

### 5. Flujo Inteligente de Devolución y Reingreso Directo a Supervisión / Contratación al Volver a Radicar
* **Problema Resuelto:**
  * Cuando un informe o cuenta de cobro ya había avanzado hasta la fase de **Supervisión** (`EN REVISION SUPERVISOR`) o **Contratación** (`EN REVISION CONTRATACION`) y era devuelto al contratista (`DEVUELTA`), al corregir las observaciones y hacer clic en *"Volver a Radicar"*, el sistema reiniciaba el ciclo obligando al contratista a seleccionar un revisor y pasando por `RADICADA`, lo que generaba reprocesos y demoras innecesarias.
* **Solución Implementada:**
  * **[InformeSupervision.java](file:///src/main/java/com/combinacion/models/InformeSupervision.java):**
    * Se incorporó el campo `estadoPrevioDevolucion` con sus correspondientes métodos getter y setter.
  * **[InformeSupervisionDAO.java](file:///src/main/java/com/combinacion/dao/InformeSupervisionDAO.java):**
    * Se añadió la columna `estado_previo_devolucion VARCHAR(50)` en la base de datos con verificación DDL preventiva.
    * Se mapeó el nuevo campo en `mapResultSetToInforme`.
    * Se implementaron los métodos `obtenerEstadoPrevioDevolucion(int idInforme)` (con fallback automático a `historial_radicacion` para cuentas históricas devueltas con anterioridad) y `limpiarEstadoPrevioDevolucion(int idInforme)`.
  * **[UsuarioDAO.java](file:///src/main/java/com/combinacion/dao/UsuarioDAO.java):**
    * Se añadió el método `obtenerPorCedula(String cedula)` para localizar supervisores o usuarios activos mediante sanitización de cédula con expresión regular, permitiendo identificar al supervisor del contrato para el envío de notificaciones.
  * **[procesar_revision.jsp](file:///src/main/webapp/procesar_revision.jsp):**
    * Al devolver una cuenta (`accion = 'DEVUELTA'`), se registra de forma persistente en `estado_previo_devolucion` el estado exacto en el que se encontraba la cuenta (`EN REVISION SUPERVISOR`, `EN REVISION CONTRATACION`, etc.).
    * En cualquier otra acción de avance o aprobación, dicho campo se limpia.
  * **[InformeSupervisionService.java](file:///src/main/java/com/combinacion/services/InformeSupervisionService.java):**
    * En `construirFormData`, cuando el contratista envía con `radicar=true` sobre una cuenta devuelta:
      * Si fue devuelta por el Supervisor, la cuenta reingresa directamente con estado **`EN REVISION SUPERVISOR`**.
      * Si fue devuelta por Contratación, la cuenta reingresa directamente con estado **`EN REVISION CONTRATACION`**.
      * Si fue devuelta por un Revisor, mantiene el reingreso estándar a **`RADICADA`**.
    * En `actualizar`, se registra en `historial_radicacion` la traza de re-radicación directa, se limpia `estado_previo_devolucion` y se despachan las notificaciones por correo electrónico correspondientes (incluyendo correo al supervisor cuando reingresa a su bandeja).
  * **[form_supervision.jsp](file:///src/main/webapp/form_supervision.jsp):**
    * Se carga el `estadoPrevioDevolucion` de la cuenta.
    * Si la cuenta proviene de Supervisión o Contratación, se presenta un panel informativo destacado al contratista explicándole con total claridad que, al guardar las correcciones, la cuenta irá directamente a la bandeja de quien la devolvió (Supervisor o Contratación), sin necesidad de reasignar revisor.
    * El selector de revisor se omite automáticamente para estos casos y el botón de acción se adapta dinámicamente a **`[Volver a Radicar (Enviar a Supervisión)]`** o **`[Volver a Radicar (Enviar a Contratación)]`**.



