# ANÁLISIS COMPARATIVO: DOCUMENTO ORIGINAL VS PLANTILLAS DIVIDIDAS

## Documento Original Analizado
**Archivo:** `PLANTILLAS_TODAS_DAGJP_INVERSION_2026.docx`

## Hallazgos del Análisis

### 1. ESTRUCTURA DEL DOCUMENTO ORIGINAL

El documento original contiene **4 PLANTILLAS DIFERENTES** identificadas por sus headers:

#### Plantilla 1: **ESTUDIOS PREVIOS**
- **Header:** MODELO INTEGRADO DE PLANEACIÓN Y GESTIÓN(MIPG) ESTUDIOS PREVIOS
- **Código:** MAJA01.04.01.P002.F001
- **Versión:** 005

#### Plantilla 2: **VERIFICACIÓN DE CUMPLIMIENTO**
- **Header:** MODELO INTEGRADO DE PLANEACIÓN Y GESTIÓN(MIPG) VERIFICACIÓN DE CUMPLIMIENTO DE REQUISITOS PARA CONTRATACIÓN DIRECTA
- **Código:** MAJA01.04.02.P007.F001
- **Versión:** 005

#### Plantilla 3: **CERTIFICADO DE IDONEIDAD**
- **Header:** MODELO INTEGRADO DE PLANEACIÓN Y GESTIÓN(MIPG) CERTIFICADO DE IDONEIDAD Y EXPERIENCIA PERSONA NATURAL
- **Código:** MAJA01.04.02.P007.F002
- **Versión:** 004

#### Plantilla 4: **COMPLEMENTO AL CONTRATO**
- **Header:** MODELO INTEGRADO DE PLANEACIÓN Y GESTIÓN(MIPG) COMPLEMENTO AL CONTRATO ELECTRÓNICO DE PRESTACIÓN DE SERVICIOS PROFESIONALES Y DE APOYO A LA GESTIÓN
- **Código:** MAJA01.04.03.P001.F003
- **Versión:** 005

### 2. HEADERS ADICIONALES ENCONTRADOS

También se encontraron otros headers que NO son parte de las 4 plantillas principales:

- **INFORME PARCIAL Y/O FINAL DE SUPERVISIÓN DE CONTRATO** (Código: FGN-61300-SA-F-10)
- **ACTA DE INICIO** (Código: MAJA01.04.03.P001.F011)

### 3. PLACEHOLDERS ENCONTRADOS

**Total de placeholders únicos:** 0

⚠️ **PROBLEMA CRÍTICO:** El análisis NO encontró placeholders en el formato esperado ({{PLACEHOLDER}}, {PLACEHOLDER}, etc.)

Esto significa que:
1. Los campos dinámicos están escritos directamente en el documento (hardcodeados)
2. NO se están usando placeholders para la combinación de correspondencia
3. Las plantillas divididas NO tendrán campos reemplazables

### 4. PROBLEMAS IDENTIFICADOS EN LA DIVISIÓN ANTERIOR

Basándome en el análisis, los problemas con las plantillas divididas son:

#### ❌ Problema 1: **Alteración de Formatos**
- Al dividir el documento, se perdieron los estilos originales
- Los headers pueden no haberse copiado correctamente
- Las tablas y su formato pueden estar alterados

#### ❌ Problema 2: **Falta de Placeholders**
- El documento original NO tiene placeholders definidos
- Los valores están hardcodeados (ej: "CLAUDIA PATRICIA VARGAS OROZCO", "3500254255", etc.)
- No se pueden hacer reemplazos automáticos sin primero crear los placeholders

#### ❌ Problema 3: **Separación Incorrecta**
- No hay separadores claros entre las 4 plantillas en el cuerpo del documento
- Solo los headers indican qué sección es cuál
- La división manual puede haber cortado contenido que pertenece a una sección

### 5. CAMPOS QUE DEBERÍAN SER PLACEHOLDERS

Del análisis del documento, estos son algunos campos que deberían convertirse en placeholders:

**Información del Contrato:**
- No. DE PROCESO: 4121.010.32.1.076-2026 → {{NUMERO_PROCESO}}
- Supervisor: CLAUDIA PATRICIA VARGAS OROZCO → {{NOMBRE_SUPERVISOR}}
- Cargo: Subdirectora de Defensa Judicial... → {{CARGO_SUPERVISOR}}

**Información Presupuestal:**
- Número CDP: 3500254255 → {{NUMERO_CDP}}
- Fecha de Expedición: 5 de enero de 2026 → {{FECHA_EXPEDICION_CDP}}
- Valor: $ 962010000 → {{VALOR_CDP}}

**Información del Contratista:**
- (Estos campos no aparecen en el documento analizado, lo que confirma que son plantillas genéricas)

## 6. RECOMENDACIONES

### ✅ Acción 1: **Crear Placeholders en el Documento Original**
Antes de dividir, debemos:
1. Identificar TODOS los campos dinámicos
2. Reemplazarlos con placeholders en formato {{NOMBRE_CAMPO}}
3. Documentar qué placeholder corresponde a qué dato

### ✅ Acción 2: **División Correcta del Documento**
1. Usar los headers como guía para identificar dónde empieza/termina cada plantilla
2. Preservar TODO el formato original (estilos, tablas, alineación)
3. Copiar los headers, footers y contenido completo de cada sección

### ✅ Acción 3: **Validación Post-División**
1. Comparar cada plantilla dividida con su sección en el original
2. Verificar que los estilos se mantienen
3. Confirmar que todos los placeholders están presentes

## 7. PRÓXIMOS PASOS

¿Qué quieres hacer?

**Opción A:** Analizar las plantillas divididas actuales para ver qué se perdió
**Opción B:** Crear nuevas plantillas correctamente desde el documento original
**Opción C:** Primero agregar placeholders al documento original, luego dividir
**Opción D:** Otro enfoque que prefieras

---

**Fecha del análisis:** 2026-02-09
**Herramienta:** DetailedDocxAnalyzer.java
