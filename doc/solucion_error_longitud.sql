-- Script para solucionar el error "value too long for type character varying(255)"
-- Se cambian las columnas sospechosas de superar los 255 caracteres a TEXT
-- Ejecuta este script en tu base de datos PostgreSQL

ALTER TABLE presupuesto_detalles 
    ALTER COLUMN apropiacion_presupuestal TYPE TEXT,
    ALTER COLUMN cdp_numero TYPE TEXT,
    ALTER COLUMN rp_numero TYPE TEXT,
    ALTER COLUMN id_paa TYPE TEXT,
    ALTER COLUMN codigo_dane TYPE TEXT,
    ALTER COLUMN inversion TYPE TEXT,
    ALTER COLUMN funcionamiento TYPE TEXT,
    ALTER COLUMN certificado_insuficiencia TYPE TEXT;

-- Aseguramos que las fichas también sean TEXT (aunque ya deberían serlo)
ALTER TABLE presupuesto_detalles 
    ALTER COLUMN ficha_ebi_nombre TYPE TEXT,
    ALTER COLUMN ficha_ebi_objetivo TYPE TEXT,
    ALTER COLUMN ficha_ebi_actividades TYPE TEXT;
