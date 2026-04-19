# Mejoras Pendientes — Carga Masiva desde Excel

> Registrado: 2026-03-30
> Estado: **PENDIENTE** — Para implementar cuando el sistema crezca

---

## Contexto

El módulo de Carga Masiva (`CargaMasivaService.java`) actualmente procesa los
archivos Excel/CSV correctamente pero puede volverse lento o poco usable a medida
que aumente el volumen de datos.

La lógica ya fue extraída del Servlet a su propio Service (refactorización MVC),
lo que facilita implementar estas mejoras sin romper nada existente.

---

## Mejoras Identificadas

### 1. 🔁 Procesamiento por Lotes (Batch Insert)
- **Problema actual:** cada fila del Excel genera un `INSERT` individual a la BD.
- **Mejora:** acumular las entidades en listas y ejecutar inserciones masivas
  (`addBatch()` / `executeBatch()` en JDBC).
- **Impacto esperado:** reducción drástica del tiempo con archivos de 100+ filas.
- **Dificultad:** Media.

---

### 2. 📊 Barra de Progreso en Tiempo Real
- **Problema actual:** el usuario sube el archivo y espera sin saber qué pasa.
- **Mejora:** mostrar progreso en tiempo real usando:
  - **Opción A:** Polling periódico (AJAX cada 1 seg al servidor)
  - **Opción B:** WebSocket (más moderno, actualización instantánea)
- **Impacto esperado:** mejor experiencia de usuario en archivos grandes.
- **Dificultad:** Media-Alta.

---

### 3. ✅ Validación Previa (Dry Run)
- **Problema actual:** los errores se descubren a mitad de inserción.
- **Mejora:** agregar un modo "validar sin guardar" que recorra todo el archivo,
  detecte problemas (cédulas vacías, fechas inválidas, columnas faltantes) y
  genere un reporte antes de tocar la base de datos.
- **Impacto esperado:** mayor confiabilidad y menos datos incorrectos en BD.
- **Dificultad:** Media.

---

### 4. ⏩ Subida Asíncrona (Procesamiento en Background)
- **Problema actual:** el servidor HTTP bloquea la conexión durante todo el
  procesamiento (puede dar timeout en archivos grandes).
- **Mejora:** procesar en un hilo separado (`ExecutorService`) y retornar
  inmediatamente al usuario con un ID de tarea. El usuario puede consultar
  el estado cuando quiera.
- **Impacto esperado:** evita timeouts y libera el hilo HTTP.
- **Dificultad:** Alta.

---

## Archivos a modificar cuando se implemente

| Archivo | Cambio |
|---|---|
| `CargaMasivaService.java` | Agregar batch, dry-run, procesamiento async |
| `CargaMasivaServlet.java` | Manejar respuesta de progreso / tarea async |
| `carga_masiva.jsp` | Agregar barra de progreso y reporte de validación |
| `DAOs` (todos) | Agregar métodos `insertBatch(List<>)` |

---

## Prioridad sugerida de implementación

1. **Primero:** Validación Previa (bajo riesgo, alto valor)
2. **Segundo:** Batch Insert (mejora de rendimiento inmediata)
3. **Tercero:** Barra de Progreso (mejora de UX)
4. **Último:** Subida Asíncrona (cuando el volumen lo exija)
