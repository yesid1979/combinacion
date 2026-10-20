# Registro de Modificaciones Realizadas

Este documento mantiene un registro cronológico detallado de todos los cambios, mejoras, correcciones y nuevas funcionalidades implementadas en el sistema, organizado por fecha.

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
