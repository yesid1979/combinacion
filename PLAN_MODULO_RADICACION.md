# Plan de Desarrollo: Módulo de Radicación, Validación y Aprobación de Cuentas de Cobro

Este documento servirá como hoja de ruta para la construcción del nuevo módulo de radicación y seguimiento de cuentas de cobro, integrando los roles y flujos definidos por la entidad.

## 1. Roles Involucrados en el Flujo
El sistema ya cuenta con roles predefinidos. Para este módulo, la interacción se define de la siguiente manera:
*   **Administrador / Contratación:** Encargados de crear usuarios en el sistema (ej. dar acceso al Contratista).
*   **Contratista:** Ingresa al sistema, genera sus informes/cuentas de cobro y realiza la radicación. **Nota clave:** Al momento de radicar, el contratista debe poder *seleccionar de una lista a la persona encargada de revisar su cuenta*.
*   **Revisor (Apoyo a la supervisión):** Persona delegada para hacer la revisión inicial. **Importante:** Esta persona puede tener el rol base de "Contratista" en el sistema, pero actúa con funciones de revisión para las cuentas que le son asignadas.
*   **Contratación (Revisor Final):** Área que da el visto bueno definitivo tras la aprobación del Revisor.
*   **Supervisor:** Define inicialmente quiénes son los revisores. En el sistema su participación es menor en el flujo digital; su acción principal ocurre al final, firmando físicamente el documento impreso una vez tiene el visto bueno de Contratación.

## 2. Flujo de Estados Propuesto (Ciclo de Vida)
1.  **Generada (Borrador):** La cuenta de cobro fue creada por el Contratista en el sistema, pero aún no ha sido radicada.
2.  **Radicada:** El Contratista envía oficialmente los documentos y **selecciona al Revisor** correspondiente. La cuenta pasa a la bandeja de ese Revisor.
3.  **En Revisión (Revisor):** El Revisor asignado analiza los soportes.
    *   Si hay errores ➔ Pasa a estado **Devuelta (con observaciones)**. El Contratista debe subsanar/corregir.
    *   Si está correcta ➔ El Revisor la aprueba y avanza de estado.
4.  **En Revisión Final (Contratación):** El área de Contratación revisa la cuenta pre-aprobada por el Revisor.
    *   Si hay errores ➔ Se devuelve (con observaciones).
    *   Si está correcta ➔ Se otorga el Visto Bueno definitivo.
5.  **Aprobada para Impresión (Visto Bueno):** La cuenta cumplió todas las validaciones en el sistema y está lista para imprimirse y recolectar la firma física del Supervisor.

## 3. Requerimientos Técnicos Iniciales
*   **Base de Datos:**
    *   Crear tabla `historial_radicacion` para mantener trazabilidad exacta (ej: id_cuenta, id_usuario_cambio, estado_anterior, estado_nuevo, fecha_cambio, observaciones).
    *   Ajustar tablas de cuentas/informes para almacenar el `id_revisor_asignado` (el que el contratista selecciona al radicar).
    *   Adaptar la tabla de usuarios (o crear una tabla de permisos) para identificar fácilmente quiénes pueden aparecer en la lista desplegable de "Revisores", independientemente de su rol original.
*   **Interfaz de Usuario (UI):**
    *   Modificar el formulario (modal) de radicación para incluir el selector (dropdown) del Revisor.
    *   Bandeja de entrada (Dashboard) dinámica: Si un usuario es designado (o seleccionado) como Revisor, debe ver una pestaña/tabla de "Cuentas por Revisar".
    *   Bandeja para el área de Contratación con las cuentas "Pre-aprobadas".
    *   Visualización de línea de tiempo (Timeline) para el Contratista.

## 4. Fases de Implementación
*   **Fase 1: Base de Datos:** Crear tabla de historial, ajustar tabla de informes con el ID del revisor, y establecer la consulta/lógica SQL para listar a los revisores.
*   **Fase 2: Backend (DAOs y Servlets):** Crear los métodos para cambiar el estado, registrar el historial, y obtener la lista de revisores desde la BD.
*   **Fase 3: Frontend (Vistas):** Agregar el selector de revisores al botón de radicar, crear la bandeja de revisión para Revisores y Contratación.
*   **Fase 4: Pruebas del Flujo Completo:** Simular la creación, selección de revisor, aprobación y visto bueno.
