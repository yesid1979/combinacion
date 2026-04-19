-- =====================================================
-- SCRIPT COMPLETO PARA AMPLIAR CAMPOS EN CONTRATISTAS
-- =====================================================
-- Ejecuta este script para ampliar TODOS los campos que podrían
-- causar error "value too long" durante la carga masiva

-- Ampliar cédula (de 20 a 50)
ALTER TABLE contratistas 
  ALTER COLUMN cedula TYPE VARCHAR(50);

-- Ampliar DV (mantener en 2 por si hay errores)
ALTER TABLE contratistas 
  ALTER COLUMN dv TYPE VARCHAR(2);

-- Ampliar nombre (mantener en 255)
ALTER TABLE contratistas 
  ALTER COLUMN nombre TYPE VARCHAR(255);

-- Ampliar teléfono (de 50 a 100)
ALTER TABLE contratistas 
  ALTER COLUMN telefono TYPE VARCHAR(100);

-- Ampliar correo (de 70/100 a 255)
ALTER TABLE contratistas 
  ALTER COLUMN correo TYPE VARCHAR(255);

-- Ampliar dirección (mantener en 255 o ampliar a TEXT)
ALTER TABLE contratistas 
  ALTER COLUMN direccion TYPE TEXT;

-- Ampliar tarjeta profesional (de 50 a 100)
ALTER TABLE contratistas 
  ALTER COLUMN tarjeta_profesional TYPE VARCHAR(100);

-- Asegurar que los campos de texto largo sean TEXT (no VARCHAR)
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

-- Mensaje de confirmación
SELECT 'Campos de contratistas ampliados exitosamente' AS resultado;
