# DEPARTAMENTO ADMINISTRATIVO DE GESTIÓN JURÍDICA PÚBLICA

**DISEÑAR Y DESARROLLAR UN SISTEMA DE INFORMACIÓN PARA LA RADICACIÓN, VALIDACIÓN Y APROBACIÓN DE CUENTAS DE COBRO E INFORMES DE SUPERVISIÓN PARA EL DISTRITO DE SANTIAGO DE CALI**

Santiago de Cali, noviembre del 2024

---

## 1. LA RAZONABILIDAD DEL DOCUMENTO TÉCNICO

Los conceptos que se comparten aquí sirven como base de referencia para el cumplimiento de lo que se solicita en el marco de la GUÍA PARA EL DESARROLLO E IMPLEMENTACIÓN DEL SISTEMAS DE INFORMACIÓN del Distrito de Cali.

Se presenta en forma resumida algunas características que debe tener el sistema:

*   Es un sistema que apenas inicia su ciclo de vida. Está en sus primeras etapas de desarrollo y utilización por parte de la entidad.
*   El espectro tecnológico no es complejo, pero requiere integraciones documentales robustas.
*   Debe contener una interfaz para usuario final (contratistas y supervisores) donde podrán generar, radicar y consultar el estado de sus informes y cuentas de cobro.
*   Debe contener una interfaz para la parte administrativa y directiva (Ordenadores de gasto, revisores).
*   **Módulo de mantenimiento:** donde se registrarán los datos básicos para la parametrización del sistema (gestión de contratistas, supervisores y parámetros anuales).
*   **Módulo de Radicación y Validación:** para la recepción, revisión y aprobación o devolución de cuentas de cobro e informes.
*   **Módulo de Trazabilidad:** para llevar el historial y la línea de tiempo de cada cuenta de cobro (Radicada -> En Revisión -> Devuelta -> Aprobada).
*   **Módulo de Generación Documental:** para la combinación y exportación automatizada de plantillas (DOCX, PDF) y carga masiva de datos.
*   Módulo de creación y asignación de usuarios en el sistema con sus respectivos roles (Administrador, Supervisor, Contratista, Apoyo, Ordenador).
*   Los códigos fuentes serán totalmente de propiedad de la Alcaldía de Santiago de Cali - DAGJP, el cual garantizará que el sistema sea actualizado en cualquier momento sin contar con la autorización del desarrollador.

---

## 2. CONTENIDO Y DESARROLLO

### 2.1 CUMPLIMIENTO DE ESTÁNDARES Y NORMATIVA VIGENTE
El sistema no está sometido al cumplimiento de algún marco legal restrictivo, más allá de los preceptos de transparencia, control interno y lineamientos del Modelo de prevención del Daño Antijurídico en la gestión de cuentas de cobro.

### 2.2 DIMENSIONAMIENTO DEL SISTEMA

El personal de sistemas del DAGJP realizó estimaciones que arrojan lo siguiente:

**Métricas de usuarios**

| Elemento | Primer año | Estable en próximos años |
| :--- | :--- | :--- |
| **Número de usuarios administradores** | 1 Funcional<br>1 Técnico | 1 Funcional<br>1 Técnico |
| **Número total de usuarios en el sistema.** | Max 150 (entre contratistas, supervisores y apoyos) | estimado 200 internos Alcaldía |
| **Número de usuarios concurrentes en el Sistema** | Max 100 | 50 normales y 100 en pico en casos excepcionales como fechas de cierre o cortes de facturación mensual |

**Métricas de almacenamiento**

| Métrica | Estimación |
| :--- | :--- |
| **Número de documentos, emails, videos, imágenes, etc.** | En esta primera fase se cargarán y generarán archivos DOCX, PDF, CSV (Plantillas, soportes, informes) |
| **Tamaño promedio de documentos (KB/MB)** | 1 a 30 MB |
| **Crecimiento estimado de la base de datos** | 10% - 2 GB para el primer año, teniendo en cuenta la carga masiva y generación documental continua. |

*NOTA EXPLICATIVA:* El crecimiento de la BD se estimará en relación directa con los movimientos que se presenten cada vez que se radique un nuevo informe y se adjunten soportes.

**Métricas de transacciones**
*   **Estimación del número de transacciones registradas en una hora:** Se estima con base en experiencias del DAGJP que ocurran potencialmente unas 500 transacciones/mes, especialmente concentradas en las fechas de corte (ej. los primeros 5 días del mes).

### 2.3 DESCRIPCIÓN DETALLADA DE LOS REQUERIMIENTOS FUNCIONALES Y NO FUNCIONALES DE LA SOLUCIÓN

**Requisitos funcionales derivados:**
Elaborar el diseño y desarrollo de un sistema de información para la alcaldía de Santiago de Cali que permita:
*   Implementar en una base de datos relacional.
*   Que sea interactiva y transaccional.
*   Permitir consulta, radicación, validación, actualización y generación de documentos.
*   Gestionar un flujo de estados (Ciclo de Vida) para las cuentas de cobro.

**Requisitos NO funcionales derivados:**
Debería satisfacer:
*   Que cumpla el estándar SQL.
*   Que pueda funcionar en cualquier sistema operativo.
*   Que pueda funcionar con cualquier explorador web moderno.

**Entendimiento del requerimiento funcional:**
Acerca de la interactividad en un contexto de bases de datos:
*   Que satisfaga las funcionalidades de persistencia -CRUD-.
*   Que gestione usuarios, cualesquiera que sean sus roles.
*   Se utilizará la metodología MVC (Modelo Vista Controlador).

**Funcionalidades ofrecidas:**
Para cada uno de los módulos se realizarán las siguientes operaciones de interactividad CRUD sobre la BD:
*   Crear
*   Leer
*   Actualizar/Modificar
*   Eliminar
*   Consultar
*   Combinar y Exportar Documentos (Integración con Apache POI / XDocReport).

### 2.4 USABILIDAD Y ACCESIBILIDAD

Como es una aplicación 100% web, en efecto se deben cumplir las siguientes condiciones:
a. La solución implementada debe ser 100% WEB.
b. La solución debe presentarse al usuario en sus interfaces y/o formulario en idioma español.
c. El contenido del sistema debe cumplir con las pautas de accesibilidad de la W3C (WCAG nivel AA) y el "Manual de Gobierno Digital".
d. Cumplir con la norma NTC 5854, sobre accesibilidad.
e. Diseño responsivo adaptable a dispositivos móviles (uso de Bootstrap).
f. Los URLs amigables deben ser únicos.
g. Cumplimiento de las 10 heurísticas de Nielsen.
h. Funcionamiento en navegadores Firefox, Chrome, Opera, Safari, Edge en su última versión estable.

### 2.5 AUTENTICACIÓN Y CONTROL DE ACCESO

El sistema de información debe permitir integración con API de Google (Google OAuth / Drive API) para la gestión documental y notificaciones, así como un sistema propio de autenticación parametrizable para usuarios locales.
*   Bloqueo de usuario tras intentos fallidos.
*   Cierre de sesión automático por inactividad.

### 2.6 SEGURIDAD

*   Módulo de auditoría que registre transacciones detalladas (fecha, hora, módulo, acción, usuario, IP) para conocer el histórico de modificaciones, radicación y cambios de estado.
*   Módulo para definir roles (Administrador, Supervisor, Apoyo, Contratista) y permisos por funcionalidad.
*   Comunicación bajo protocolo seguro HTTPS.
*   Arquitectura separada por capas (MVC).

### 2.7 ARQUITECTURA, METODOLOGÍA, FASES Y PLATAFORMA TECNOLÓGICA

*   Separar por capas: backend, base de datos y la interfaz de usuario.
*   Desarrollo iterativo incremental (Metodología Ágil - XP).
*   **Patrón de Arquitectura:** MVC (Modelo Vista Controlador).
    *   **Controlador:** Servlets (Java EE).
    *   **Modelo:** Conexión y persistencia mediante JDBC a la base de datos.
    *   **Vistas:** JSP.

**Componentes:**
| Capa | Tecnologías |
| :--- | :--- |
| **Front end** | JSP, Bootstrap, CSS, HTML5, JQuery |
| **Back end** | Java (Java EE), Apache Tomcat |
| **Base de datos** | PostgreSQL |
| **Generación Documental** | Apache POI, XDocReport, Jsoup |
| **Integraciones** | Google API Client (Drive, Gmail) |

### 2.8 SISTEMA CON APERTURA DE DATOS
La implementación de este componente debe incluir las orientaciones dadas por el Departamento administrativo de planeación y la oficina de transparencia.

### 2.9 TRAZABILIDAD DE LA INFORMACIÓN Y DE LAS OPERACIONES
El sistema contará con un módulo de auditoría y un historial de estados (Timeline) para las cuentas de cobro, registrando las actividades de los usuarios, radicación, observaciones y aprobaciones.
El rol administrador es el único autorizado para acceder, consultar y exportar toda esta información de forma global.

### 2.10 ADMINISTRACIÓN DEL SISTEMA
A cargo del usuario de rol administrador, incluyendo:
*   Gestión de roles y usuarios.
*   Mantenimiento de datos paramétricos (Formatos, Cuotas, Resoluciones).
*   Privilegios CRUD completos sobre el sistema.

### 2.11 COMPONENTE DE INTEGRACIÓN / INTEROPERABILIDAD
Integración con las APIs de Google Workspace (Gmail y Drive) para el almacenamiento de soportes de cuentas de cobro y el envío de notificaciones por correo electrónico ante cambios de estado en las revisiones.

### 2.12 MIGRACIÓN DE LOS DATOS
Se debe ofrecer un cargue de datos inicial a través del módulo de *Carga Masiva* (mediante archivos CSV/Excel) para la importación inicial de contratistas, obligaciones, y ordenadores de gasto.

### 2.13 PRUEBAS Y ASEGURAMIENTO DE CALIDAD
*   Pruebas de funcionalidad e integración del sistema.
*   Pruebas de rendimiento y generación documental concurrente.
*   Pruebas de seguridad (Habeas data).

### 2.14 TRANSFERENCIA DE CONOCIMIENTO
Capacitaciones dirigidas:
*   Administradores y Supervisores (Min 2 h).
*   Contratistas (Usuarios Funcionales) (Min 2 h).

### 2.15 PROPIEDAD INTELECTUAL Y LICENCIAMIENTO
Los derechos patrimoniales sobre el sistema corresponden en su totalidad a la Administración Central del Municipio Santiago de Cali – Departamento Administrativo de Gestión Jurídica Pública. El CTO conservará los derechos morales de autor y entregará el código fuente en su totalidad.
