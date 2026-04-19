-- =====================================================
-- SCRIPT COMPLETO PARA AMPLIAR CAMPOS
-- ORDENADORES Y CONTRATISTAS
-- =====================================================
-- Ejecuta este script UNA SOLA VEZ antes de usar la carga masiva

-- ========================================
-- TABLA: ORDENADORES_GASTO
-- ========================================

-- Ampliar cédula del ordenador
ALTER TABLE ordenadores_gasto 
  ALTER COLUMN cedula_ordenador TYPE VARCHAR(50);

-- Ampliar decreto de nombramiento
ALTER TABLE ordenadores_gasto 
  ALTER COLUMN decreto_nombramiento TYPE VARCHAR(255);

-- Ampliar acta de posesión
ALTER TABLE ordenadores_gasto 
  ALTER COLUMN acta_posesion TYPE VARCHAR(255);

-- ========================================
-- TABLA: CONTRATISTAS
-- ========================================

-- Ampliar cédula
ALTER TABLE contratistas 
  ALTER COLUMN cedula TYPE VARCHAR(50);

-- Ampliar DV
ALTER TABLE contratistas 
  ALTER COLUMN dv TYPE VARCHAR(2);

-- Ampliar nombre
ALTER TABLE contratistas 
  ALTER COLUMN nombre TYPE VARCHAR(255);

-- Ampliar teléfono
ALTER TABLE contratistas 
  ALTER COLUMN telefono TYPE VARCHAR(100);

-- Ampliar correo
ALTER TABLE contratistas 
  ALTER COLUMN correo TYPE VARCHAR(255);

-- Ampliar dirección a TEXT
ALTER TABLE contratistas 
  ALTER COLUMN direccion TYPE TEXT;

-- Ampliar tarjeta profesional
ALTER TABLE contratistas 
  ALTER COLUMN tarjeta_profesional TYPE VARCHAR(100);

-- Asegurar que campos de texto largo sean TEXT
ALTER TABLE contratistas 
  ALTER COLUMN formacion_titulo TYPE TEXT;

ALTER TABLE contratistas 
  ALTER COLUMN descripcion_formacion TYPE TEXT;

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

SELECT 
    'Campos ampliados exitosamente' AS resultado,
    'Ordenadores: cedula, decreto, acta' AS ordenadores_actualizados,
    'Contratistas: cedula, dv, telefono, correo, direccion, tarjeta, todos los TEXT' AS contratistas_actualizados;
