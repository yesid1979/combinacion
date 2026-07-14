# Plan de Desarrollo: Módulo de Radicación, Validación y Aprobación de Cuentas de Cobro

Este documento servirá como hoja de ruta para la construcción del nuevo módulo, una vez finalizados los ajustes actuales.

## 1. Objetivos del Módulo
*   **Radicación:** Permitir la recepción oficial de la cuenta de cobro y sus anexos.
*   **Validación:** Proceso de revisión por parte de los supervisores o revisores encargados para garantizar que la cuenta de cobro cumple con los requisitos técnicos, jurídicos y financieros.
*   **Aprobación:** Flujo de autorizaciones y firmas necesarias para dar luz verde al pago.
*   **Trazabilidad:** Mantener un historial del estado de cada cuenta de cobro (Radicada -> En Revisión -> Observada/Rechazada -> Aprobada).

## 2. Flujo de Estados Propuesto (Ciclo de Vida)
1.  **Generada/Borrador:** La cuenta de cobro fue creada pero no enviada oficialmente.
2.  **Radicada:** El contratista (o quien radique) envía oficialmente los documentos.
3.  **En Revisión:** El revisor/supervisor está analizando los soportes.
4.  **Con Observaciones (Devuelta):** Se encontraron inconsistencias y el contratista debe subsanar.
5.  **Aprobada (Lista para pago):** Cumple con todos los requisitos y pasa a tesorería/pagos.

## 3. Requerimientos Técnicos Iniciales
*   **Base de Datos:**
    *   Agregar columna de `estado_radicacion` a la tabla de `informes_supervision` o crear una tabla de historial de estados.
    *   Tabla de `observaciones_cuentas` para guardar los comentarios de rechazo o corrección.
*   **Interfaz de Usuario (UI):**
    *   Bandeja de entrada (Dashboard) para Revisores/Supervisores con las cuentas pendientes por revisar.
    *   Botones de acción rápida: "Aprobar", "Rechazar/Devolver con comentarios".
    *   Línea de tiempo visual (Timeline) del estado de la cuenta.
*   **Notificaciones:** (Opcional) Envío de alertas por correo cuando una cuenta cambia de estado.

## 4. Fases de Implementación
*   **Fase 1: Estructura de Base de Datos:** Creación de estados, tablas de historial y permisos de usuario.
*   **Fase 2: Backend y Lógica de Negocio:** Creación de los endpoints (servlets) para cambiar de estado y guardar observaciones.
*   **Fase 3: Frontend (Vistas):** Creación del Dashboard de revisión y actualización visual del seguimiento para el contratista.
*   **Fase 4: Pruebas y Ajustes:** Pruebas del flujo completo desde radicación hasta aprobación.

*(Este plan es un borrador inicial y se irá nutriendo con los requerimientos específicos a medida que avancemos)*.
