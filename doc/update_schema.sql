-- SCRIPT DE ACTUALIZACIÓN DE BASE DE DATOS
-- Ejecutar este script para agregar las columnas faltantes necesarias para la Carga Masiva.

-- 1. Actualizar tabla SUPERVISORES
ALTER TABLE supervisores ADD COLUMN IF NOT EXISTS cedula VARCHAR(20);
ALTER TABLE supervisores ADD COLUMN IF NOT EXISTS cargo VARCHAR(100);

-- Si la cedula debe ser única, intentar agregar la restricción (puede fallar si hay duplicados, por eso es opcional)
-- ALTER TABLE supervisores ADD CONSTRAINT unique_cedula_supervisor UNIQUE (cedula);

-- 2. Actualizar tabla ORDENADORES_GASTO
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS cedula_ordenador VARCHAR(20);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS cargo_ordenador VARCHAR(100);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS decreto_nombramiento VARCHAR(100);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS acta_posesion VARCHAR(100);

-- 3. Actualizar tabla ESTRUCTURADORES
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS juridico_cargo VARCHAR(100);
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS tecnico_cargo VARCHAR(100);
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS financiero_cargo VARCHAR(100);

-- 4. Actualizar tabla PRESUPUESTO_DETALLES
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS ficha_ebi_nombre TEXT;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS ficha_ebi_objetivo TEXT;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS ficha_ebi_actividades TEXT;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS inversion VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS funcionamiento VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS apropiacion_presupuestal VARCHAR(100);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS id_paa VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS codigo_dane VARCHAR(50);

-- 5. Actualizar tabla CONTRATISTAS
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS correo VARCHAR(100);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS direccion VARCHAR(255);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS telefono VARCHAR(50);

-- 6. Actualizar tabla CONTRATOS (Campos adicionales detectados en CSV)
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS trd_proceso VARCHAR(100);
