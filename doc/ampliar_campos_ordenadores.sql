-- =====================================================
-- SCRIPT PARA AMPLIAR CAMPOS EN ORDENADORES_GASTO
-- =====================================================
-- Ejecuta este script para ampliar los campos que podrían
-- causar error "value too long" durante la carga masiva

-- Ampliar campo de cédula (de 20 a 50 caracteres)
ALTER TABLE ordenadores_gasto 
  ALTER COLUMN cedula_ordenador TYPE VARCHAR(50);

-- Ampliar campos que podrían tener textos largos
ALTER TABLE ordenadores_gasto 
  ALTER COLUMN decreto_nombramiento TYPE VARCHAR(255);

ALTER TABLE ordenadores_gasto 
  ALTER COLUMN acta_posesion TYPE VARCHAR(255);

-- Mensaje de confirmación
SELECT 'Campos ampliados exitosamente' AS resultado;
