-- =====================================================
-- TABLA PARA INFORMES DE SUPERVISIÓN
-- =====================================================
CREATE TABLE IF NOT EXISTS informes_supervision (
    id SERIAL PRIMARY KEY,
    contrato_id INT REFERENCES contratos(id),
    periodo_informe VARCHAR(100), -- Ejemplo: "Enero 2026"
    tipo_informe VARCHAR(20), -- "PARCIAL" o "FINAL"
    numero_cuota VARCHAR(20),
    
    -- Informe Jurídico
    fecha_inicio_periodo DATE,
    fecha_fin_periodo DATE,
    modificaciones TEXT,
    suspensiones TEXT,
    reanudaciones TEXT,
    cesiones TEXT,
    terminacion_anticipada TEXT,
    
    -- Informe Contable y Financiero
    valor_cuota_pagar NUMERIC(15, 2),
    valor_acumulado_pagado NUMERIC(15, 2),
    saldo_por_cancelar NUMERIC(15, 2),
    
    -- Seguridad Social
    planilla_numero VARCHAR(50),
    planilla_pin VARCHAR(100),
    planilla_operador VARCHAR(100),
    planilla_fecha_pago DATE,
    planilla_periodo VARCHAR(50),
    
    -- Informe Técnico
    observaciones_tecnicas TEXT,
    recomendaciones TEXT,
    
    -- Metadatos
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_suscripcion DATE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_informes_contrato ON informes_supervision(contrato_id);
