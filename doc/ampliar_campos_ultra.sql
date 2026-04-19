-- =====================================================
-- SCRIPT ULTRA AGRESIVO - AMPLIAR TODO
-- =====================================================
-- Este script convierte TODOS los campos VARCHAR a tamaños grandes
-- para evitar CUALQUIER error de "value too long"

-- ========================================
-- TABLA: ORDENADORES_GASTO
-- ========================================

ALTER TABLE ordenadores_gasto 
  ALTER COLUMN organismo TYPE TEXT;

ALTER TABLE ordenadores_gasto 
  ALTER COLUMN direccion_organismo TYPE TEXT;

ALTER TABLE ordenadores_gasto 
  ALTER COLUMN nombre_ordenador TYPE TEXT;

ALTER TABLE ordenadores_gasto 
  ALTER COLUMN cedula_ordenador TYPE VARCHAR(100);

ALTER TABLE ordenadores_gasto 
  ALTER COLUMN cargo_ordenador TYPE TEXT;

ALTER TABLE ordenadores_gasto 
  ALTER COLUMN decreto_nombramiento TYPE TEXT;

ALTER TABLE ordenadores_gasto 
  ALTER COLUMN acta_posesion TYPE TEXT;

-- ========================================
-- TABLA: CONTRATISTAS
-- ========================================

ALTER TABLE contratistas 
  ALTER COLUMN cedula TYPE VARCHAR(100);

ALTER TABLE contratistas 
  ALTER COLUMN dv TYPE VARCHAR(10);

ALTER TABLE contratistas 
  ALTER COLUMN nombre TYPE TEXT;

ALTER TABLE contratistas 
  ALTER COLUMN telefono TYPE VARCHAR(200);

ALTER TABLE contratistas 
  ALTER COLUMN correo TYPE VARCHAR(500);

ALTER TABLE contratistas 
  ALTER COLUMN direccion TYPE TEXT;

ALTER TABLE contratistas 
  ALTER COLUMN formacion_titulo TYPE TEXT;

ALTER TABLE contratistas 
  ALTER COLUMN descripcion_formacion TYPE TEXT;

ALTER TABLE contratistas 
  ALTER COLUMN tarjeta_profesional TYPE VARCHAR(200);

ALTER TABLE contratistas 
  ALTER COLUMN descripcion_tarjeta TYPE TEXT;

ALTER TABLE contratistas 
  ALTER COLUMN experiencia TYPE TEXT;

ALTER TABLE contratistas 
  ALTER COLUMN descripcion_experiencia TYPE TEXT;

ALTER TABLE contratistas 
  ALTER COLUMN restricciones TYPE TEXT;

-- ========================================
-- CONFIRMACIÓN
-- ========================================

SELECT 'TODOS los campos ampliados a TEXT o VARCHAR(500)' AS resultado;
