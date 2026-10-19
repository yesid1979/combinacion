# Reglas de Protección de Cuentas y Google Drive

> **Fecha:** 18 de Octubre de 2026  
> **Autor / Responsable:** Ing. Yesid Javier Piedrahita Correa  
> **Objetivo:** Definir y preservar la lógica de negocio para la gestión de cuentas históricas (manuales de julio-septiembre) vs cuentas nuevas en la plataforma digital.

---

## 1. Contexto del Problema y Antecedentes
1. En Google Drive en la carpeta de **Producción** ya reposan las carpetas oficiales de las cuotas que fueron liquidadas y pagadas manualmente (julio, agosto, septiembre 2026).
   * Ejemplo: `CUOTA 1 123`, `CUOTA 2 123`, `CUOTA 3 123`.
2. Si un contratista desordenado intenta radicar una de esas cuotas viejas (ej. Cuota 1 o 2) en la plataforma web:
   * **Peligro:** El sistema podría ingresar a la carpeta oficial de Producción y meter archivos generados nuevos encima de los documentos manuales históricos ya archivados.
3. **Regla acordada con Yesid:**
   * Si alguien intenta radicar una **cuenta vieja manual** que ya existe en Producción:
     * El sistema **DEBE DESVIAR los archivos a la carpeta de Pruebas** (`DRIVE_CARPETA_PRUEBAS`).
     * De esta forma, la carpeta oficial de Producción queda **100% blindada e intacta**.
4. **Cuidado esencial con las cuentas nuevas (Caso María Fernanda):**
   * Una cuenta **NUEVA** (ej. Cuota 4 o Cuota 5 del mes corriente) **NUNCA debe auto-aprobarse sola**.
   * Debe crearse en Producción y quedar en **`BORRADOR`** (si solo se guarda) o en **`RADICADA`** (para que su supervisor la revise formalmente).

---

## 2. Diferenciación Técnica entre Creación (`INSERT`) y Actualización (`UPDATE`)

### A. Al Actualizar una Cuenta Existente (`actualizar(...)` con `id > 0`):
* La cuenta ya nació dentro de la plataforma digital y está registrada en `informes_supervision`.
* Da igual si fue creada hoy, ayer o la semana pasada; o si está en `BORRADOR`, `RADICADA` o `DEVUELTA`.
* **Regla:** **Siempre opera en su carpeta oficial asignada en Producción**.
* Los archivos que se reemplacen o corrijan se actualizan en esa misma carpeta oficial sin desvíos y respetando el flujo formal de supervisión.

### B. Únicamente al Crear una Cuenta (`insertar(...)`):
* Si el contratista intenta crear una cuota:
  * El sistema consulta si en la carpeta de Producción de Drive existe una carpeta histórica para esa cuota con **fecha de creación anterior al corte del sistema digital** (Octubre 2026).
  * **Si es histórica manual:** Aplica el desvío acordado hacia `DRIVE_CARPETA_PRUEBAS`. Producción queda blindada.
  * **Si es cuenta nueva:** Se crea en Producción normalmente, y al radicar pasa a **`RADICADA`** para revisión del supervisor.
