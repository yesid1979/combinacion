-- Añadir nuevos campos de fechas a la tabla contratos
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS fecha_idoneidad DATE;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS fecha_estructurador DATE;

COMMENT ON COLUMN contratos.fecha_idoneidad IS 'Fecha del estudio previo de idoneidad';
COMMENT ON COLUMN contratos.fecha_estructurador IS 'Fecha en la que el estructurador finalizó el documento';
