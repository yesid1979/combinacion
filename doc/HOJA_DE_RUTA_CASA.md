# Hoja de Ruta y Guía para Continuar en Casa

> **Fecha:** 14 de Octubre de 2026  
> **Usuario:** Yesid Piedrahita  
> **Rama de Respaldo:** `respaldo-trabajo-hoy`  
> **Rama de Producción Base:** `master` (en commit `4084421`, 100% sincronizado con `origin/master`)

---

## 1. Contexto Importante para la IA en el Computador de la Casa

1. **Estado de `master`:**  
   La rama `master` está limpia, sincronizada con producción (`4084421`) y compila exitosamente (`BUILD SUCCESS`).
   - Contiene la funcionalidad de **Supervisión por Encargo** en `form_supervision.jsp` (commit `0fd7835`), donde en la pestaña *Aspectos Generales* se puede elegir entre el Supervisor Titular y un Encargado.
   - Contiene la validación estricta de Ficha Técnica y soportes de Cuota 1 (commit `ce1a2d8`).
   - Contiene la opción "Sin Revisor" (commit `be84f94`).

2. **Regla de Oro Acordada con Yesid:**
   - **NO hacer cambios masivos simultáneos.**
   - **NO ejecutar comandos destructivos de git (`reset`, `checkout .`, etc.).**
   - Trabajar **una sola tarea a la vez**, mostrando el diff exacto y validándolo con Yesid antes de aplicar.
   - Escuchar primero el error que Yesid observó antes de salir de la oficina para revisarlo juntos frente a la pantalla.

3. **Ubicación de Archivos de Respaldo de Hoy:**
   Todos los archivos con el código desarrollado hoy se encuentran en:
   `doc/backup_today/` (en esta misma rama `respaldo-trabajo-hoy`).

---

## 2. Las 3 Tareas Planificadas

### Tarea 1: Sincronización del Perfil hacia la tabla `contratistas`
- **Objetivo:** Cuando un contratista actualiza su información personal en `perfil.jsp` (o en `UsuarioService.java`), sincronizar automáticamente:
  - Correo electrónico
  - Celular / Teléfono
  - Dirección
  - Fecha de nacimiento
  - Edad
  Hacia la tabla `contratistas`, buscando por la cédula limpia de caracteres no numéricos.
- **Archivos de referencia en backup:**
  - `doc/backup_today/ContratistaDAO.java`
  - `doc/backup_today/UsuarioService.java`
  - `doc/backup_today/perfil.jsp`

### Tarea 2: Nuevo Flujo con Supervisor (`EN REVISION SUPERVISOR`)
- **Objetivo:**
  1. Cuando Contratación aprueba en `procesar_revision.jsp`, la cuenta no pasa directo a radicada/aprobada final, sino que avanza al estado `EN REVISION SUPERVISOR`.
  2. Se envía notificación por correo electrónico al Supervisor asignado a ese informe (sea el Titular o el Encargado).
  3. El Supervisor revisa y da el visto bueno final (o devuelve con observaciones).
- **Archivos de referencia en backup:**
  - `doc/backup_today/InformeSupervisionService.java`
  - `doc/backup_today/InformeSupervisionDAO.java`
  - `doc/backup_today/procesar_revision.jsp`
  - `doc/backup_today/lista_informes.jsp`

### Tarea 3: Validación de Actividades vs Evidencias Opcionales
- **Objetivo:**
  - Las descripciones de texto de las actividades **sí son obligatorias** al radicar.
  - Los archivos adjuntos de evidencia (`soporte_evidencia_*`) **son opcionales**.
  - Proteger contra errores de navegador (`ERR_FILE_NOT_FOUND`) si un contratista elimina un archivo local antes del envío.
- **Archivos de referencia en backup:**
  - `doc/backup_today/form_supervision.jsp`

---

## 3. Mensaje para Copiar y Pegar en el Chat de la Casa

Cuando Yesid abra Antigravity en el computador de su casa, debe copiar y pegar el siguiente texto:

```text
Hola, soy Yesid. Vengo de la oficina y creamos la rama "respaldo-trabajo-hoy" donde dejamos el archivo "doc/HOJA_DE_RUTA_CASA.md" y la carpeta "doc/backup_today/".
Por favor lee "doc/HOJA_DE_RUTA_CASA.md" antes de hacer nada.
Primero quiero comentarte un error que vi antes de salir para que lo revisemos juntos paso a paso, sin tocar código masivo ni hacer resets.
```
