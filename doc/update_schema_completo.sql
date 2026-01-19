-- =====================================================
-- SCRIPT DE ACTUALIZACIÓN COMPLETO PARA CARGA MASIVA
-- =====================================================
-- Ejecuta este script en PostgreSQL para asegurar que todas las 
-- columnas necesarias existan en tu base de datos.

-- =====================================================
-- 1. TABLA: CONTRATISTAS
-- =====================================================
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS cedula VARCHAR(20);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS dv VARCHAR(1);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS nombre VARCHAR(255);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS telefono VARCHAR(50);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS correo VARCHAR(100);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS direccion VARCHAR(255);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS fecha_nacimiento DATE;
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS edad INT;
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS formacion_titulo TEXT;
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS descripcion_formacion TEXT;
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS tarjeta_profesional VARCHAR(50);
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS descripcion_tarjeta TEXT;
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS experiencia TEXT;
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS descripcion_experiencia TEXT;
ALTER TABLE contratistas ADD COLUMN IF NOT EXISTS restricciones TEXT;

-- =====================================================
-- 2. TABLA: SUPERVISORES
-- =====================================================
ALTER TABLE supervisores ADD COLUMN IF NOT EXISTS cedula VARCHAR(20);
ALTER TABLE supervisores ADD COLUMN IF NOT EXISTS nombre VARCHAR(255);
ALTER TABLE supervisores ADD COLUMN IF NOT EXISTS cargo VARCHAR(100);

-- =====================================================
-- 3. TABLA: ORDENADORES_GASTO
-- =====================================================
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS organismo VARCHAR(255);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS direccion_organismo VARCHAR(255);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS nombre_ordenador VARCHAR(255);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS cedula_ordenador VARCHAR(20);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS cargo_ordenador VARCHAR(100);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS decreto_nombramiento VARCHAR(100);
ALTER TABLE ordenadores_gasto ADD COLUMN IF NOT EXISTS acta_posesion VARCHAR(100);

-- =====================================================
-- 4. TABLA: PRESUPUESTO_DETALLES
-- =====================================================
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS cdp_numero VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS cdp_fecha DATE;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS cdp_valor NUMERIC(15, 2);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS cdp_vencimiento DATE;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS rp_numero VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS rp_fecha DATE;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS apropiacion_presupuestal VARCHAR(100);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS id_paa VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS codigo_dane VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS inversion VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS funcionamiento VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS ficha_ebi_nombre TEXT;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS ficha_ebi_objetivo TEXT;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS ficha_ebi_actividades TEXT;
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS certificado_insuficiencia VARCHAR(50);
ALTER TABLE presupuesto_detalles ADD COLUMN IF NOT EXISTS fecha_insuficiencia DATE;

-- =====================================================
-- 5. TABLA: ESTRUCTURADORES
-- =====================================================
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS juridico_nombre VARCHAR(255);
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS juridico_cargo VARCHAR(100);
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS tecnico_nombre VARCHAR(255);
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS tecnico_cargo VARCHAR(100);
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS financiero_nombre VARCHAR(255);
ALTER TABLE estructuradores ADD COLUMN IF NOT EXISTS financiero_cargo VARCHAR(100);

-- =====================================================
-- 6. TABLA: CONTRATOS (Principal)
-- =====================================================
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS trd_proceso VARCHAR(100);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS numero_contrato VARCHAR(50);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS tipo_contrato VARCHAR(100);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS nivel VARCHAR(50);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS objeto TEXT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS modalidad VARCHAR(100);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS estado VARCHAR(50);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS periodo VARCHAR(50);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS fecha_suscripcion DATE;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS fecha_inicio DATE;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS fecha_terminacion DATE;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS fecha_aprobacion DATE;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS fecha_ejecucion DATE;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS fecha_arl DATE;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS plazo_ejecucion VARCHAR(100);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS plazo_meses INT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS plazo_dias INT;

-- Valores Financieros
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS valor_total_letras TEXT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS valor_total_numeros NUMERIC(15, 2);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS valor_antes_iva NUMERIC(15, 2);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS valor_iva NUMERIC(15, 2);

-- Cuotas
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS valor_cuota_letras TEXT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS valor_cuota_numero NUMERIC(15, 2);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS num_cuotas_letras VARCHAR(100);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS num_cuotas_numero INT;

-- Media Cuota
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS valor_media_cuota_letras TEXT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS valor_media_cuota_numero NUMERIC(15, 2);

-- Actividades
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS actividades_entregables TEXT;

-- Liquidación
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS liquidacion_acuerdo VARCHAR(100);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS liquidacion_articulo VARCHAR(100);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS liquidacion_decreto VARCHAR(100);
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS circular_honorarios VARCHAR(100);

-- Llaves Foráneas (si no existen)
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS contratista_id INT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS supervisor_id INT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS ordenador_id INT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS presupuesto_id INT;
ALTER TABLE contratos ADD COLUMN IF NOT EXISTS estructurador_id INT;

-- =====================================================
-- 7. RESTRICCIONES Y FOREIGN KEYS
-- =====================================================
-- Intenta agregar Foreign Keys (puede fallar si ya existen, es normal)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_contrato_contratista') THEN
        ALTER TABLE contratos ADD CONSTRAINT fk_contrato_contratista 
            FOREIGN KEY (contratista_id) REFERENCES contratistas(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_contrato_supervisor') THEN
        ALTER TABLE contratos ADD CONSTRAINT fk_contrato_supervisor 
            FOREIGN KEY (supervisor_id) REFERENCES supervisores(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_contrato_ordenador') THEN
        ALTER TABLE contratos ADD CONSTRAINT fk_contrato_ordenador 
            FOREIGN KEY (ordenador_id) REFERENCES ordenadores_gasto(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_contrato_presupuesto') THEN
        ALTER TABLE contratos ADD CONSTRAINT fk_contrato_presupuesto 
            FOREIGN KEY (presupuesto_id) REFERENCES presupuesto_detalles(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_contrato_estructurador') THEN
        ALTER TABLE contratos ADD CONSTRAINT fk_contrato_estructurador 
            FOREIGN KEY (estructurador_id) REFERENCES estructuradores(id);
    END IF;
END $$;

-- =====================================================
-- 8. ÍNDICES PARA OPTIMIZACIÓN
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_contratos_numero ON contratos(numero_contrato);
CREATE INDEX IF NOT EXISTS idx_contratos_contratista ON contratos(contratista_id);
CREATE INDEX IF NOT EXISTS idx_contratistas_cedula ON contratistas(cedula);
CREATE INDEX IF NOT EXISTS idx_supervisores_cedula ON supervisores(cedula);

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================
COMMIT;
