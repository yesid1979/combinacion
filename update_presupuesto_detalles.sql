-- Script para ampliar la capacidad de las columnas en la tabla presupuesto_detalles
-- Se cambian los campos de texto a TEXT o VARCHAR de mayor longitud para evitar errores de truncamiento.

ALTER TABLE presupuesto_detalles 
    ALTER COLUMN cdp_numero TYPE VARCHAR(255),
    ALTER COLUMN rp_numero TYPE VARCHAR(255),
    ALTER COLUMN apropiacion_presupuestal TYPE VARCHAR(255),
    ALTER COLUMN id_paa TYPE VARCHAR(255),
    ALTER COLUMN codigo_dane TYPE VARCHAR(255),
    ALTER COLUMN inversion TYPE VARCHAR(255),
    ALTER COLUMN funcionamiento TYPE VARCHAR(255),
    ALTER COLUMN ficha_ebi_nombre TYPE TEXT,
    ALTER COLUMN ficha_ebi_objetivo TYPE TEXT,
    ALTER COLUMN ficha_ebi_actividades TYPE TEXT,
    ALTER COLUMN certificado_insuficiencia TYPE VARCHAR(255);
