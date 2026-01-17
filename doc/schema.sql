-- Tabla: Contratistas
CREATE TABLE IF NOT EXISTS contratistas (
    id SERIAL PRIMARY KEY,
    cedula VARCHAR(20) UNIQUE NOT NULL,
    dv VARCHAR(1),
    nombre VARCHAR(255) NOT NULL,
    telefono VARCHAR(50),
    correo VARCHAR(100),
    direccion VARCHAR(255),
    fecha_nacimiento DATE,
    edad INT,
    formacion_titulo TEXT,
    descripcion_formacion TEXT,
    tarjeta_profesional VARCHAR(50),
    descripcion_tarjeta TEXT,
    experiencia TEXT,
    descripcion_experiencia TEXT,
    restricciones TEXT
);

-- Tabla: Ordenadores de Gasto
CREATE TABLE IF NOT EXISTS ordenadores_gasto (
    id SERIAL PRIMARY KEY,
    organismo VARCHAR(255),
    direccion_organismo VARCHAR(255),
    nombre_ordenador VARCHAR(255),
    cedula_ordenador VARCHAR(20),
    cargo_ordenador VARCHAR(100),
    decreto_nombramiento VARCHAR(100),
    acta_posesion VARCHAR(100)
);

-- Tabla: Supervisores
CREATE TABLE IF NOT EXISTS supervisores (
    id SERIAL PRIMARY KEY,
    cedula VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    cargo VARCHAR(100)
);

-- Tabla: Detalles Presupuestales
CREATE TABLE IF NOT EXISTS presupuesto_detalles (
    id SERIAL PRIMARY KEY,
    cdp_numero VARCHAR(50),
    cdp_fecha DATE,
    cdp_valor NUMERIC(15, 2),
    cdp_vencimiento DATE,
    rp_numero VARCHAR(50),
    rp_fecha DATE,
    apropiacion_presupuestal VARCHAR(100),
    id_paa VARCHAR(50),
    codigo_dane VARCHAR(50),
    inversion VARCHAR(50),
    funcionamiento VARCHAR(50),
    ficha_ebi_nombre TEXT,
    ficha_ebi_objetivo TEXT,
    ficha_ebi_actividades TEXT,
    certificado_insuficiencia VARCHAR(50),
    fecha_insuficiencia DATE
);

-- Tabla: Estructuradores
CREATE TABLE IF NOT EXISTS estructuradores (
    id SERIAL PRIMARY KEY,
    juridico_nombre VARCHAR(255),
    juridico_cargo VARCHAR(100),
    tecnico_nombre VARCHAR(255),
    tecnico_cargo VARCHAR(100),
    financiero_nombre VARCHAR(255),
    financiero_cargo VARCHAR(100)
);

-- Tabla: Contratos (Tabla Principal)
CREATE TABLE IF NOT EXISTS contratos (
    id SERIAL PRIMARY KEY,
    trd_proceso VARCHAR(100),
    numero_contrato VARCHAR(50) UNIQUE NOT NULL,
    tipo_contrato VARCHAR(100),
    nivel VARCHAR(50),
    objeto TEXT,
    modalidad VARCHAR(100),
    estado VARCHAR(50),
    periodo VARCHAR(50),
    fecha_suscripcion DATE,
    fecha_inicio DATE,
    fecha_terminacion DATE,
    fecha_aprobacion DATE,
    fecha_ejecucion DATE,
    fecha_arl DATE,
    plazo_ejecucion VARCHAR(100),
    plazo_meses INT,
    plazo_dias INT,
    
    -- Valores Financieros
    valor_total_letras TEXT,
    valor_total_numeros NUMERIC(15, 2),
    valor_antes_iva NUMERIC(15, 2),
    valor_iva NUMERIC(15, 2),
    
    -- Cuotas
    valor_cuota_letras TEXT,
    valor_cuota_numero NUMERIC(15, 2),
    num_cuotas_letras VARCHAR(100),
    num_cuotas_numero INT,
    
    -- Media Cuota (Added based on CSV analysis)
    valor_media_cuota_letras TEXT,
    valor_media_cuota_numero NUMERIC(15, 2),
    
    actividades_entregables TEXT,
    
    -- Liquidacion
    liquidacion_acuerdo VARCHAR(100),
    liquidacion_articulo VARCHAR(100),
    liquidacion_decreto VARCHAR(100),
    circular_honorarios VARCHAR(100),
    
    -- Llaves Foraneas
    contratista_id INT REFERENCES contratistas(id),
    supervisor_id INT REFERENCES supervisores(id),
    ordenador_id INT REFERENCES ordenadores_gasto(id),
    presupuesto_id INT REFERENCES presupuesto_detalles(id),
    estructurador_id INT REFERENCES estructuradores(id)
);

-- Indices para optimizacion
CREATE INDEX IF NOT EXISTS idx_contratos_numero ON contratos(numero_contrato);
CREATE INDEX IF NOT EXISTS idx_contratos_contratista ON contratos(contratista_id);
CREATE INDEX IF NOT EXISTS idx_contratistas_cedula ON contratistas(cedula);
