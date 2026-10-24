# Registro de Modificaciones Realizadas

Este documento mantiene un registro cronológico detallado de todos los cambios, mejoras, correcciones y nuevas funcionalidades implementadas en el sistema, organizado por fecha.

---

## [2026-10-24]

### 1. Independencia de Red y Migración de CDNs a Assets Locales
* **Eliminación Total de Dependencias de CDNs Externos:**
  * Se descargaron y alojaron de forma local en `src/main/webapp/assets/vendor/` todas las librerías frontend del sistema que antes dependían de CDNs (`cdn.jsdelivr.net`, `code.jquery.com`, `cdn.datatables.net`):
    * **Bootstrap 5.3.0**: CSS y JS Bundle.
    * **Bootstrap Icons 1.10.0**: CSS y fuentes locales (`woff`, `woff2`).
    * **jQuery 3.6.0**: JS minificado.
    * **DataTables 1.13.4 + Bootstrap 5 + Responsive**: CSS, JS y archivo de internacionalización local (`es-ES.json`).
    * **SweetAlert2 v11**: JS bundle unificado.
    * **Summernote Lite 0.8.18**: CSS, JS y paquete de fuentes (`woff`, `woff2`, `ttf`).
* **Centralización de Plantillas (Assets Layouts):**
  * Creación de `src/main/webapp/inc/head_assets.jsp` para centralizar todos los enlaces CSS, preconnects y fuentes.
  * Creación de `src/main/webapp/inc/scripts_assets.jsp` para centralizar los scripts en el orden exacto de dependencias e inicialización en español de DataTables.
  * Migración de más de 30 páginas JSP (tanto en raíz como en `admin/`) para consumir los nuevos includes modulares.
  * **Beneficios:** Carga instantánea en intranets, tolerancia a fallos por cortes de internet corporativo y eliminación de redundancias en las páginas.

### 2. Seguridad, Gestión de Sesión y Manejo de Errores (Web Descriptor)
* **Tiempo de Sesión Extendido y Protección de Cookies:**
  * Se configuró en `src/main/webapp/WEB-INF/web.xml` el parámetro `<session-timeout>` a **120 minutos** (2 horas).
  * Se habilitó `<cookie-config><http-only>true</http-only></cookie-config>` para blindar la cookie `JSESSIONID` contra robo de sesión mediante ataques XSS.
* **Páginas de Error Institucionales (404 y 500):**
  * Creación de `src/main/webapp/error.jsp` con diseño corporativo institucional, navbar y footer.
  * Mapeo de errores HTTP `404` (recurso no encontrado) y `500` (error interno no controlado) en `web.xml`, evitando la exposición de stacktraces de Java o versiones del servidor Tomcat.
* **Autonomía Total en Fotos de Perfil (Eliminación de `ui-avatars.com`):**
  * En `src/main/webapp/inc/navbar.jsp` y `src/main/webapp/perfil.jsp` se reemplazaron las peticiones remotas a `ui-avatars.com` por avatares generados localmente mediante vector SVG y badges CSS con las iniciales del usuario. Carga instantánea y 100% offline.
* **Optimización y Resiliencia en Mi Perfil (`perfil.jsp`):**
  * Limpieza de enlaces redundantes en `<head>`.
  * Incorporación de indicadores de carga interactivos (`Swal.showLoading()`) en la subida de foto de perfil, guardado de datos y cambio de contraseña.
  * Blindaje con control de excepciones `.fail()` en todas las peticiones AJAX de perfil para notificar amigablemente al usuario en caso de desconexión.
  * Validación de peso máximo de 20 MB en el lado del cliente previo al envío de la foto de perfil.
  * **Corrección en Cálculo de Edad con Calendario:** Se reescribió la lógica para escuchar los eventos `input`, `change`, `blur` y `keyup`, parseando directamente el formato ISO `YYYY-MM-DD`. Ahora calcula y refleja la edad al instante tanto al escribir con el teclado como al elegir el día desde el selector/calendario emergente del navegador.
  * **Corrección de Maquetación y Posicionamiento de Avatar:** Se fijó el contenedor `.avatar-section` a 150x150 px y se anclaron los botones flotantes de acción (cámara de subida en la esquina inferior derecha y basurero de eliminación en la inferior izquierda) eliminando el solapamiento que tenían sobre el nombre del usuario. Se corrigió la lectura del atributo `${sessionScope.usuario.fotoUrl}` y se diseñó un badge circular degradado (`.avatar-placeholder`) que muestra limpiamente la inicial en blanco cuando no hay foto cargada.
* **Blindaje contra Cruce y Sobreescritura de Firmas en Google Drive:**
  * En `UsuarioService.java`, se sustituyó el nombre de archivo arbitrario del cliente (`submittedFileName`, ej: `firma.png`, `firma-removebg-preview.png`) por un nombre determinístico y único: `firma_user_[id]_[cedula].ext`.
  * Previene que múltiples contratistas que suban archivos con el mismo nombre genérico sobreescriban el archivo en Google Drive o intercambien involuntariamente sus firmas en los informes de gestión y supervisión.
* **Corrección de Orden Cronológico en Historial de Radicación y Revisiones (`form_supervision.jsp`):**
  * Se detectó que la tabla `historialTable` ordenaba la columna de fecha de forma lexicográfica (alfabética como texto), provocando que eventos de la tarde con formato 12 horas (ej. `03:41 PM`) quedaran por debajo de eventos de la mañana (`10:20 AM`).
  * Se agregó el atributo `data-order="${hr.fechaCambio != null ? hr.fechaCambio.time : 0}"` en la celda `<td>` para que DataTables ordene de manera estrictamente numérica por el timestamp en milisegundos.
  * Se ajustó en `HistorialRadicacionDAO.java` la consulta para ordenar por `ORDER BY h.fecha_cambio DESC, h.id DESC`.
* **Visualización Selectiva de Cuentas Devueltas para Revisores y Supervisores (`InformeSupervisionService.java`):**
  * Para mantener la bandeja de entrada limpia (Zero Inbox), las cuentas en estado `DEVUELTA` permanecen ocultas de forma predeterminada para revisores y supervisores.
  * Cuando el usuario activa voluntariamente el filtro de estado en **"Devueltas"** (o busca "DEVUELTA"), el sistema detecta la consulta y carga únicamente las cuentas devueltas que le corresponden, permitiendo consultar el historial, motivos y trazabilidad sin saturar la vista principal de trabajo.

---

## [2026-10-19]

### 1. Interfaz de Usuario (UI) y Gestión de Cuentas / Informes
* **Dinámica de colores en columna Tipo:**
  * Se modificó la columna **Tipo** (`PARCIAL` / `FINAL`) en `src/main/webapp/lista_informes.jsp` para que su distintivo (badge) adopte dinámicamente el color correspondiente al **Estado** de la cuenta:
    * **RADICADA**: Azul (`bg-primary`).
    * **VISTO BUENO (CONTRATACIÓN)**: Verde suave (`bg-success bg-opacity-75 text-white`).
    * **EN REVISIÓN / EN REVISIÓN (CONTRATACIÓN)**: Amarillo (`bg-warning text-dark`).
    * **EN REVISIÓN FINAL / EN REVISIÓN (SUPERVISIÓN)**: Celeste (`bg-info text-dark`).
    * **APROBADA (LISTA PARA FIRMA)**: Verde (`bg-success`).
    * **DEVUELTA**: Rojo (`bg-danger`).
    * **BORRADOR**: Gris (`bg-secondary`).
* **Botón y Modal de Convención de Colores:**
  * Se agregó el botón **Significado de Colores** junto al filtro por estado en `lista_informes.jsp`.
  * Se agregó un ícono de ayuda interactivo `(?)` en la cabecera `<th>Tipo</th>` con prevención de propagación de clic para no alterar el ordenamiento de DataTables.
  * Se diseñó una ventana modal explicativa que detalla la fase y significado de cada color en el flujo de la cuenta.

### 2. Formato y Maquetación de Documentos (Word y Web)
* **Distribución Equitativa 50/50 en Supervisión:**
  * Se ajustó el generador de informes de supervisión (`SupervisionReportGenerator.java`) estableciendo un ancho fijo equitativo (`5050 dxa` / `5050 dxa`, equivalente al 50% cada una) entre la columna de **Obligaciones Contractuales** y **Actividades Realizadas**, evitando el encogimiento de la columna izquierda.
  * Se replicó el mismo ancho proporcional del 50%/50% en la tabla del formulario de supervisión en `form_supervision.jsp`.
* **Actualización de Plantillas Oficiales:**
  * Actualización de número y fecha de decreto en la plantilla `MODIFICACION_2_ACEPTACION`.

### 3. Motor de Conjugación de Verbos (Informe de Supervisión a Informe de Gestión)
* **Ampliación de Verbos en Base de Datos:**
  * Inclusión de 14 nuevos verbos conjugados en la tabla `verbos_conjugacion` de PostgreSQL y en `DatabasePatcher.java`:
    * *socializar*, *estructurar*, *monitorear*, *capacitar*, *formular*, *diseñar*, *subsanar*, *recopilar*, *ajustar*, *revisar*, *tramitar*, *presentar*, *identificar*, *acompañar*.
* **Mejoras en el Algoritmo de Conversión (`GestionReportGenerator.java`):**
  * Limpieza y saneamiento previo de texto (eliminación de pasiva refleja `se`, corrección de errores ortográficos comunes).
  * Limpieza posterior de redundancias (supresión de *"El contratista"*, prefijos temporales y fechas innecesarias) logrando redacción fluida en primera persona ("Realicé...", "Acompañé...").

### 4. Procesamiento de Imágenes y Conversión HTML a Word XML
* **Soporte Base64 Robusto en `HtmlToWordXmlConverter.java`:**
  * Corrección de la excepción `java.lang.IllegalArgumentException: Illegal base64 character 20`.
  * Saneamiento de cadenas base64: eliminación de espacios en blanco, retornos de carro, saltos de línea y entidades URL (`%20`).
  * Soporte dual para decodificación estándar, MIME y URL.

### 5. Seguridad y Automatizaciones
* **Protección de Carga Masiva:**
  * Validación y blindaje en la sincronización desde Google Sheets.
* **Informes y Consecutivos:**
  * Normalización del cargo del supervisor en informes y control de permisos en la asignación de consecutivos.

### 6. Transición Masiva de Cuentas Devueltas de Meses Anteriores (BD)
* **Aprobación de Cuentas Históricas (Julio, Agosto, Septiembre):**
  * Se identificaron y pasaron a estado **APROBADA PARA IMPRESION** las cuentas que permanecían en estado `DEVUELTA` correspondientes a periodos anteriores a octubre (IDs 260, 262, 271, 272 y 298).
  * Se registró la respectiva trazabilidad en la tabla `historial_radicacion` con observación: *"Aprobación de cuentas devueltas de meses anteriores (Julio, Agosto, Septiembre)"* manteniendo intacta la auditoría del flujo.
* **Aprobación de Cuentas Históricas - Contrato 4121.010.26.1.086 (FRANCY JOHANNA FÚQUENE CARVAJAL):**
  * Se pasaron a estado **APROBADA PARA IMPRESION** las cuotas 1 (Julio - ID 320), 2 (Agosto - ID 321) y 3 (Septiembre - ID 322).
  * Se preservó intacta en estado `RADICADA` la cuota 4 (Octubre - ID 323) por corresponder al periodo en curso.
  * Se insertó en `historial_radicacion` la constancia de cada cambio de estado hacia aprobación definitiva.

---

## [2026-10-18]

* **Filtros Avanzados en Informes:**
  * Se agregó la opción **Visto Bueno (Contratación)** al selector de filtro por estado en `lista_informes.jsp`.
* **Seguridad Social y Gestión:**
  * Unificación del procesamiento del informe de gestión y seguridad social.
  * Protección de cuotas históricas contra sobreescritura accidental.
  * Optimización del servicio de conversión con LibreOffice.
* **Documentación:**
  * Actualización del archivo de consultas SQL para cambio de estados (`doc/sql para cambio de estados.txt`).

---

## [2026-10-16]

* **Flujo de Supervisión y Aprobación:**
  * Implementación del nuevo flujo de revisión con rol de Supervisor, notificaciones y asignación de supervisores.
  * Validación obligatoria del campo de actividades al radicar la cuenta, manteniendo las evidencias adjuntas como opcionales.
* **Sincronización de Usuarios:**
  * Sincronización automática de datos entre el perfil de usuario del sistema y los registros de contratistas.
* **Resiliencia de Archivos:**
  * Prevención del error `ERR_FILE_NOT_FOUND` al enviar formularios con referencias a archivos ya eliminados en disco.
  * Corrección en el parseo de obligaciones con mención a años y preservación de valores `N/A` en informe jurídico.

---

## [2026-10-15]

* **Cálculo Financiero y Otrosíes:**
  * Obtención de valor de operación y valor de contrato en letras y números desde la base de datos con soporte para otrosíes.
  * Soporte para media cuota en formatos Excel y reportes financieros.
