# GUÍA DE ESTRUCTURA PARA CARGA MASIVA DE CONTRATOS

## 📋 Formato de Archivo
- **Formatos aceptados:** `.xlsx`, `.xls`, `.csv`
- **Separador CSV:** Punto y coma (`;`)
- **Codificación CSV:** ISO-8859-1 o UTF-8

## 📊 Columnas Requeridas (en orden)

### ⚠️ IMPORTANTE
Las columnas **NO tienen que estar en este orden exacto**. El sistema identifica automáticamente cada columna por su nombre, pero los nombres deben ser EXACTOS (sin agregar espacios extras).

### 1-8: ORDENADOR DEL GASTO
| # | Nombre de Columna | Ejemplo | Requerido |
|---|---|---|---|
| 1 | Organismo | SECRETARÍA DE SALUD | No |
| 2 | TRD del proceso y consecutivo | TRD-2026-001 | No |
| 3 | Dirección del organismo | Calle 10 # 20-30 | No |
| 4 | Nombre del ordenador del gasto | Juan Pérez García | **SÍ** |
| 5 | Cédula del ordenador del gasto | 1234567890 | No |
| 6 | Cargo del ordenador del gasto | Secretario de Salud | No |
| 7 | Decreto de nombramiento | Decreto 123 de 2025 | No |
| 8 | Acta de posesión | Acta 456 | No |

### 9-14: ESTRUCTURADORES
| # | Nombre de Columna | Ejemplo | Requerido |
|---|---|---|---|
| 9 | Profesional jurídico estructurador del EP | María López | No |
| 10 | Cargo Profesional Jurídico estructurador EP | Abogado | No |
| 11 | Profesional técnico estructurador del EP | Carlos Ramírez | No |
| 12 | Cargo Profesional Técnico estructurador EP | Ingeniero | No |
| 13 | Profesional financiero estructurador del EP | Ana Gómez | No |
| 14 | Cargo Profesional Financiero estructurador EP | Contador | No |

### 15-26: PRESUPUESTO
| # | Nombre de Columna | Ejemplo | Requerido |
|---|---|---|---|
| 15 | Nombre del proyecto y Ficha +O:TEBI (si aplica) | Proyecto Salud Rural | No |
| 16 | Objetivo general de la Ficha EBI (si aplica) | Mejorar atención en salud | No |
| 17 | Encabezado actividades Ficha EBI | Actividades principales | No |
| 18 | Actividades de la Ficha EBI (si aplica) | Atención médica, vacunación | No |
| 19 | Inversión (si aplica) | 50000000 | No |
| 20 | Funcionamiento (si aplica) | 30000000 | No |
| 21 | Número y fecha del CDP | CDP-001 del 15/01/2026 | No |
| 22 | Fecha del CDP | 2026-01-15 | No |
| 23 | Valor del CDP | 80000000 | No |
| 24 | Fecha de vencimiento del CDP | 2026-12-31 | No |
| 25 | Apropiación presupuestal | 001-2026 | No |
| 26 | ID en el PAA | PAA-2026-001 | No |

### 27-51: CONTRATO
| # | Nombre de Columna | Ejemplo | Requerido |
|---|---|---|---|
| 27 | Codigo DANE | 68001 | No |
| 28 | Número de contrato | 001-2026 | **SÍ** |
| 29 | Tipo contrato xxx | Prestación de servicios | No |
| 30 | Tipo de contrato (Profesional o de Apoyo a la Gestión) | Profesional | No |
| 31 | Profesional | SÍ | No |
| 32 | Apoyo a la gestión | NO | No |
| 33 | Nivel | Profesional | No |
| 34 | NOMBRE DEL CONTRATISTA | Pedro Martínez | **SÍ** |
| 35 | Cédula del contratista | 9876543210 | **SÍ** |
| 36 | DV | 1 | No |
| 37 | PERIODO | 2026 | No |
| 38 | ESTADO CONTRATO | ACTIVO | No |
| 39 | Formación y título académica | Profesional | No |
| 40 | Descripcion de la formación y título académico | Médico Cirujano | No |
| 41 | Tarjeta o Matrícula Profesional | MP-12345 | No |
| 42 | Descripcion Tarjeta o Matricula | Médico general | No |
| 43 | Descripcion de experiencia | Trabajo detallado | No |
| 44 | Experiencia | 5 años | No |
| 45 | Objeto contractual | Prestar servicios médicos | **SÍ** |
| 46 | Valor total del contrato en letras | Diez millones | No |
| 47 | Valor total contrato en numeros | 10000000 | No |
| 48 | Valor antes de IVA | 8403361 | No |
| 49 | VALOR IVA | 1596639 | No |
| 50 | Plazo de ejecución | 11 meses | No |
| 51 | Actividades y, si aplica, entregables | Consultas médicas | No |

### 52-60: CUOTAS Y SUPERVISOR
| # | Nombre de Columna | Ejemplo | Requerido |
|---|---|---|---|
| 52 | Número de cuotas en letras | Once | No |
| 53 | Número de cuotas en número | 11 | No |
| 54 | Valor cuota en letras | Un millón | No |
| 55 | Valor cuota en número | 1000000 | No |
| 56 | Valor media cuota en letras | Quinientos mil | No |
| 57 | Valor media cuota en número | 500000 | No |
| 58 | Nombre del supervisor | Laura Díaz | No |
| 59 | Cargo del supervisor | Coordinadora | No |
| 60 | Cédula del supervisor | 5555555555 | No |

### 61-79: DATOS ADICIONALES
| # | Nombre de Columna | Ejemplo | Requerido |
|---|---|---|---|
| 61 | Número y fecha del Acuerdo de liquidación | Acuerdo 10 de 2025 | No |
| 62 | Número del artículo del Acuerdo de liquidación | Art. 5 | No |
| 63 | Número y fecha del Decreto de liquidación | Decreto 200 de 2025 | No |
| 64 | Número de la circular sobre la tabla de honorarios | Circular 003 | No |
| 65 | Certificado de Insuficiencia de Personal | Cert-2025-100 | No |
| 66 | Fecha del Certificado de Insuficiencia de Personal | 2025-12-01 | No |
| 67 | Registro Presupuestal (RPC) | RP-2026-001 | No |
| 68 | Fecha del RPC | 2026-01-20 | No |
| 69 | Fecha ARL | 2026-01-25 | No |
| 70 | Fecha de aprobación | 2026-01-10 | No |
| 71 | Feche de Ejejcución | 2026-02-01 | No |
| 72 | NÚMERO TELEFÓNICO | 3001234567 | No |
| 73 | CORREO ELECTRONICO | contratista@ejemplo.com | No |
| 74 | DIRECCIÓN | Calle 50 # 10-20 | No |
| 75 | RESTRICCIONES | Ninguna | No |
| 76 | DIA NACIMIENTO | 15 | No |
| 77 | MES NACIMIENTO | 06 | No |
| 78 | AÑO NACIMIENTO | 1980 | No |
| 79 | EDAD | 45 | No |

---

## ✅ VALIDACIÓN MÍNIMA
Para que una fila se procese correctamente, debe tener AL MENOS:
- **Cédula del contratista** O **Nombre del contratista**
- **Número de contrato**

Si una fila no cumple estos requisitos mínimos, será omitida.

---

## 📅 FORMATO DE FECHAS
Las fechas pueden estar en cualquier formato común:
- `YYYY-MM-DD` (Recomendado): `2026-01-15`
- `DD/MM/YYYY`: `15/01/2026`
- `DD-MM-YYYY`: `15-01-2026`

---

## 💰 FORMATO DE VALORES NUMÉRICOS
Los valores monetarios pueden incluir:
- Separador de miles: `.` o `,`
- Símbolo de moneda: `$`
- Ejemplo: `$10.000.000` o `10000000`

El sistema limpiará automáticamente estos caracteres.

---

## 🔍 DEBUGGING
Si la carga falla:
1. Verifica que los nombres de las columnas sean EXACTOS
2. Revisa que no haya espacios dobles en los encabezados
3. Asegúrate de que la primera fila sea el encabezado
4. Verifica que no haya filas completamente vacías al inicio

---

## 📝 PLANTILLA
Usa el archivo `MATRIZ PRESTADORES DE SERVICIOS 2026 CON DATOS.csv` como referencia para la estructura correcta.
