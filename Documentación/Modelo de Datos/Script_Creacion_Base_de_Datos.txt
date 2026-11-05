-- =============================================================================
-- SISTEMA DE COMBINACIÓN DE CORRESPONDENCIA Y SUPERVISIÓN CONTRACTUAL - DAGJP
-- ALCALDÍA DISTRITAL DE SANTIAGO DE CALI
-- MOTOR DE BASE DE DATOS: PostgreSQL (Base de datos: combinacion | Esquema: public)
-- SCRIPT OFICIAL EXTRAÍDO DE LA BASE DE DATOS EN PRODUCCIÓN / PRUEBAS
-- CONTIENE: 20 TABLAS FÍSICAS, RESTRICCIONES (PK, FK, UNIQUE), ÍNDICES Y 4 VISTAS
-- =============================================================================

SET client_encoding = 'UTF8';

-- -----------------------------------------------------------------------------
-- 1. TABLA: public.roles (5 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.roles (
    id SERIAL,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT roles_pkey PRIMARY KEY (id),
    CONSTRAINT roles_nombre_key UNIQUE (nombre)
);

-- -----------------------------------------------------------------------------
-- 2. TABLA: public.permisos (5 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.permisos (
    id SERIAL,
    codigo VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    modulo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT permisos_pkey PRIMARY KEY (id),
    CONSTRAINT permisos_codigo_key UNIQUE (codigo)
);

-- -----------------------------------------------------------------------------
-- 3. TABLA: public.rol_permisos (2 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.rol_permisos (
    rol_id INTEGER NOT NULL,
    permiso_id INTEGER NOT NULL,
    CONSTRAINT rol_permisos_pkey PRIMARY KEY (rol_id, permiso_id)
);

-- -----------------------------------------------------------------------------
-- 4. TABLA: public.usuarios (19 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.usuarios (
    id SERIAL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    correo VARCHAR(100),
    activo BOOLEAN DEFAULT true,
    ultimo_acceso TIMESTAMP,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rol_id INTEGER,
    cedula VARCHAR(20),
    celular VARCHAR(20),
    sexo VARCHAR(15),
    fecha_inicio_contrato DATE,
    fecha_fin_contrato DATE,
    vinculacion VARCHAR(50),
    foto_url VARCHAR(255),
    cargo VARCHAR(255),
    firma_url VARCHAR(500),
    CONSTRAINT usuarios_pkey PRIMARY KEY (id),
    CONSTRAINT usuarios_username_key UNIQUE (username)
);

-- -----------------------------------------------------------------------------
-- 5. TABLA: public.usuario_permisos (2 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.usuario_permisos (
    usuario_id INTEGER NOT NULL,
    permiso_id INTEGER NOT NULL,
    CONSTRAINT usuario_permisos_pkey PRIMARY KEY (usuario_id, permiso_id)
);

-- -----------------------------------------------------------------------------
-- 6. TABLA: public.contratistas (16 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.contratistas (
    id SERIAL,
    cedula VARCHAR(100) NOT NULL,
    dv VARCHAR(10),
    nombre TEXT NOT NULL,
    telefono VARCHAR(200),
    correo VARCHAR(500),
    direccion TEXT,
    fecha_nacimiento DATE,
    edad INTEGER,
    formacion_titulo TEXT,
    descripcion_formacion TEXT,
    tarjeta_profesional VARCHAR(200),
    descripcion_tarjeta TEXT,
    experiencia TEXT,
    descripcion_experiencia TEXT,
    restricciones TEXT,
    CONSTRAINT contratistas_pkey PRIMARY KEY (id),
    CONSTRAINT contratistas_cedula_key UNIQUE (cedula)
);

-- -----------------------------------------------------------------------------
-- 7. TABLA: public.supervisores (4 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.supervisores (
    id SERIAL,
    cedula VARCHAR(70) NOT NULL,
    nombre VARCHAR(300) NOT NULL,
    cargo VARCHAR(100),
    CONSTRAINT supervisores_pkey PRIMARY KEY (id),
    CONSTRAINT supervisores_cedula_key UNIQUE (cedula)
);

-- -----------------------------------------------------------------------------
-- 8. TABLA: public.ordenadores_gasto (8 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.ordenadores_gasto (
    id SERIAL,
    organismo TEXT,
    direccion_organismo TEXT,
    nombre_ordenador TEXT,
    cedula_ordenador VARCHAR(100),
    cargo_ordenador TEXT,
    decreto_nombramiento TEXT,
    acta_posesion TEXT,
    CONSTRAINT ordenadores_gasto_pkey PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 9. TABLA: public.estructuradores (7 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.estructuradores (
    id SERIAL,
    juridico_nombre VARCHAR(255),
    juridico_cargo VARCHAR(100),
    tecnico_nombre VARCHAR(255),
    tecnico_cargo VARCHAR(100),
    financiero_nombre VARCHAR(255),
    financiero_cargo VARCHAR(100),
    CONSTRAINT estructuradores_pkey PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 10. TABLA: public.presupuesto_detalles (22 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.presupuesto_detalles (
    id SERIAL,
    cdp_numero TEXT,
    cdp_fecha DATE,
    cdp_valor NUMERIC(15, 2),
    cdp_vencimiento DATE,
    rp_numero TEXT,
    rp_fecha DATE,
    apropiacion_presupuestal TEXT,
    id_paa TEXT,
    codigo_dane TEXT,
    inversion TEXT,
    funcionamiento TEXT,
    ficha_ebi_nombre TEXT,
    ficha_ebi_objetivo TEXT,
    ficha_ebi_actividades TEXT,
    certificado_insuficiencia TEXT,
    fecha_insuficiencia DATE,
    cdp_adicion TEXT,
    cdp_valor_adicion NUMERIC,
    rp_adicion TEXT,
    rp_fecha_adicion DATE,
    id_paa_si_no VARCHAR(10),
    CONSTRAINT presupuesto_detalles_pkey PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 11. TABLA: public.contratos (59 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.contratos (
    id SERIAL,
    trd_proceso TEXT,
    numero_contrato VARCHAR(50) NOT NULL,
    tipo_contrato TEXT,
    nivel TEXT,
    objeto TEXT,
    modalidad TEXT,
    estado TEXT,
    periodo TEXT,
    fecha_suscripcion DATE,
    fecha_inicio DATE,
    fecha_terminacion DATE,
    fecha_aprobacion DATE,
    fecha_ejecucion DATE,
    fecha_arl DATE,
    plazo_ejecucion TEXT,
    plazo_meses INTEGER,
    plazo_dias INTEGER,
    valor_total_letras TEXT,
    valor_total_numeros NUMERIC(15, 2),
    valor_antes_iva NUMERIC(15, 2),
    valor_iva NUMERIC(15, 2),
    valor_cuota_letras TEXT,
    valor_cuota_numero NUMERIC(15, 2),
    num_cuotas_letras TEXT,
    num_cuotas_numero INTEGER,
    valor_media_cuota_letras TEXT,
    valor_media_cuota_numero NUMERIC(15, 2),
    actividades_entregables TEXT,
    liquidacion_acuerdo TEXT,
    liquidacion_articulo TEXT,
    liquidacion_decreto TEXT,
    circular_honorarios TEXT,
    contratista_id INTEGER,
    supervisor_id INTEGER,
    ordenador_id INTEGER,
    presupuesto_id INTEGER,
    estructurador_id INTEGER,
    apoyo_supervision TEXT,
    fecha_idoneidad DATE,
    fecha_estructurador DATE,
    valor_antes_iva_letras TEXT,
    valor_iva_letras TEXT,
    valor_cuota_antes_iva_letras TEXT,
    valor_cuota_antes_iva NUMERIC,
    valor_cuota_iva_letras TEXT,
    valor_cuota_iva NUMERIC,
    adicion_si_no TEXT,
    numero_cuotas_adicion INTEGER,
    valor_total_adicion_letras TEXT,
    valor_total_adicion NUMERIC,
    valor_contrato_mas_adicion_letras TEXT,
    valor_contrato_mas_adicion NUMERIC,
    enlace_secop TEXT,
    iva_si_no VARCHAR(10),
    anio INTEGER,
    numero_modificacion VARCHAR(50),
    fecha_modificacion DATE,
    facturador_electronico VARCHAR(50),
    CONSTRAINT contratos_pkey PRIMARY KEY (id),
    CONSTRAINT contratos_numero_contrato_key UNIQUE (numero_contrato)
);

-- -----------------------------------------------------------------------------
-- 12. TABLA: public.informes_supervision (45 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.informes_supervision (
    id SERIAL,
    contrato_id INTEGER,
    periodo_informe VARCHAR(100),
    tipo_informe VARCHAR(20),
    numero_cuota VARCHAR(20),
    fecha_inicio_periodo DATE,
    fecha_fin_periodo DATE,
    modificaciones TEXT,
    suspensiones TEXT,
    reanudaciones TEXT,
    cesiones TEXT,
    terminacion_anticipada TEXT,
    valor_cuota_pagar NUMERIC(15, 2),
    valor_acumulado_pagado NUMERIC(15, 2),
    saldo_por_cancelar NUMERIC(15, 2),
    planilla_numero VARCHAR(50),
    planilla_pin VARCHAR(100),
    planilla_operador VARCHAR(100),
    planilla_fecha_pago DATE,
    planilla_periodo VARCHAR(50),
    observaciones_tecnicas TEXT,
    recomendaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_suscripcion DATE,
    adiciones TEXT,
    prorrogas TEXT,
    recibo_satisfaccion TEXT,
    constancia_paz_salvo TEXT,
    concepto_supervisor TEXT,
    url_drive_evidencias VARCHAR(500),
    consecutivo_cobro VARCHAR(50),
    soportes_json TEXT,
    estado_radicacion VARCHAR(50) DEFAULT 'BORRADOR',
    id_revisor_asignado INTEGER,
    observaciones_revision TEXT,
    observaciones_financieras TEXT,
    pago_seguridad_social VARCHAR(50),
    anio INTEGER,
    eps VARCHAR(150),
    afp_pension VARCHAR(150),
    arl VARCHAR(150),
    es_contratista_nuevo VARCHAR(10) DEFAULT 'NO',
    supervisor_id INTEGER,
    estado_previo_devolucion VARCHAR(50),
    observaciones_contratista TEXT,
    CONSTRAINT informes_supervision_pkey PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 13. TABLA: public.historial_radicacion (7 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.historial_radicacion (
    id SERIAL,
    id_informe INTEGER,
    id_usuario_cambio INTEGER,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50) NOT NULL,
    observaciones TEXT,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT historial_radicacion_pkey PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 14. TABLA: public.consecutivos_cobro (8 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.consecutivos_cobro (
    id SERIAL,
    cedula VARCHAR(50) NOT NULL,
    contrato VARCHAR(50) NOT NULL,
    numero_cuota VARCHAR(20) NOT NULL,
    consecutivo VARCHAR(50) NOT NULL,
    fecha_carga TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cargado_por INTEGER,
    anio INTEGER,
    CONSTRAINT consecutivos_cobro_pkey PRIMARY KEY (id),
    CONSTRAINT consecutivos_cobro_cedula_contrato_numero_cuota_key UNIQUE (cedula, contrato, numero_cuota)
);

-- -----------------------------------------------------------------------------
-- 15. TABLA: public.notificaciones_usuario (9 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.notificaciones_usuario (
    id SERIAL,
    id_usuario INTEGER NOT NULL,
    id_informe INTEGER,
    titulo VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(50) DEFAULT 'INFO',
    url VARCHAR(500),
    leida BOOLEAN DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT notificaciones_usuario_pkey PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 16. TABLA: public.auditoria_sistema (8 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.auditoria_sistema (
    id SERIAL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    username VARCHAR(100) NOT NULL,
    nombres_apellidos VARCHAR(255) NOT NULL,
    tipo_accion VARCHAR(100) NOT NULL,
    accion_realizada TEXT NOT NULL,
    tipo_usuario VARCHAR(100),
    ip_address VARCHAR(45),
    CONSTRAINT auditoria_sistema_pkey PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 17. TABLA: public.configuracion (4 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.configuracion (
    id SERIAL,
    clave VARCHAR(100) NOT NULL,
    valor VARCHAR(500) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT configuracion_pkey PRIMARY KEY (id),
    CONSTRAINT configuracion_clave_key UNIQUE (clave)
);

-- -----------------------------------------------------------------------------
-- 18. TABLA: public.revisores_documento (5 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.revisores_documento (
    id SERIAL,
    tipo_documento VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    cargo VARCHAR(255) NOT NULL,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT revisores_documento_pkey PRIMARY KEY (id),
    CONSTRAINT revisores_documento_tipo_documento_key UNIQUE (tipo_documento)
);

-- -----------------------------------------------------------------------------
-- 19. TABLA: public.verbos_conjugacion (4 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.verbos_conjugacion (
    id SERIAL,
    tercera_persona VARCHAR(100) NOT NULL,
    primera_persona VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT true,
    CONSTRAINT verbos_conjugacion_pkey PRIMARY KEY (id)
);

-- -----------------------------------------------------------------------------
-- 20. TABLA: public.google_oauth_token (2 columnas)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.google_oauth_token (
    id VARCHAR(50) NOT NULL,
    token_data TEXT,
    CONSTRAINT google_oauth_token_pkey PRIMARY KEY (id)
);

-- =============================================================================
-- CLAVES FORÁNEAS (INTEGRIDAD REFERENCIAL REAL DE LA BASE DE DATOS)
-- =============================================================================
ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_contratista_id_fkey FOREIGN KEY (contratista_id) REFERENCES contratistas(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_estructurador_id_fkey FOREIGN KEY (estructurador_id) REFERENCES estructuradores(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_ordenador_id_fkey FOREIGN KEY (ordenador_id) REFERENCES ordenadores_gasto(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_presupuesto_id_fkey FOREIGN KEY (presupuesto_id) REFERENCES presupuesto_detalles(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT contratos_supervisor_id_fkey FOREIGN KEY (supervisor_id) REFERENCES supervisores(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk_contrato_contratista FOREIGN KEY (contratista_id) REFERENCES contratistas(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk_contrato_estructurador FOREIGN KEY (estructurador_id) REFERENCES estructuradores(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk_contrato_ordenador FOREIGN KEY (ordenador_id) REFERENCES ordenadores_gasto(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk_contrato_presupuesto FOREIGN KEY (presupuesto_id) REFERENCES presupuesto_detalles(id);

ALTER TABLE ONLY public.contratos
    ADD CONSTRAINT fk_contrato_supervisor FOREIGN KEY (supervisor_id) REFERENCES supervisores(id);

ALTER TABLE ONLY public.historial_radicacion
    ADD CONSTRAINT historial_radicacion_id_informe_fkey FOREIGN KEY (id_informe) REFERENCES informes_supervision(id);

ALTER TABLE ONLY public.historial_radicacion
    ADD CONSTRAINT historial_radicacion_id_usuario_cambio_fkey FOREIGN KEY (id_usuario_cambio) REFERENCES usuarios(id);

ALTER TABLE ONLY public.informes_supervision
    ADD CONSTRAINT informes_supervision_contrato_id_fkey FOREIGN KEY (contrato_id) REFERENCES contratos(id);

ALTER TABLE ONLY public.informes_supervision
    ADD CONSTRAINT informes_supervision_id_revisor_asignado_fkey FOREIGN KEY (id_revisor_asignado) REFERENCES usuarios(id);

ALTER TABLE ONLY public.informes_supervision
    ADD CONSTRAINT informes_supervision_supervisor_id_fkey FOREIGN KEY (supervisor_id) REFERENCES supervisores(id);

ALTER TABLE ONLY public.notificaciones_usuario
    ADD CONSTRAINT notificaciones_usuario_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.rol_permisos
    ADD CONSTRAINT rol_permisos_permiso_id_fkey FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.rol_permisos
    ADD CONSTRAINT rol_permisos_rol_id_fkey FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.usuario_permisos
    ADD CONSTRAINT usuario_permisos_permiso_id_fkey FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.usuario_permisos
    ADD CONSTRAINT usuario_permisos_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_rol_id_fkey FOREIGN KEY (rol_id) REFERENCES roles(id);

-- =============================================================================
-- ÍNDICES ADICIONALES DE RENDIMIENTO (BTREE)
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_contratistas_cedula ON public.contratistas USING btree (cedula);
CREATE INDEX IF NOT EXISTS idx_contratos_contratista ON public.contratos USING btree (contratista_id);
CREATE INDEX IF NOT EXISTS idx_contratos_numero ON public.contratos USING btree (numero_contrato);
CREATE INDEX IF NOT EXISTS idx_informes_contrato ON public.informes_supervision USING btree (contrato_id);
CREATE INDEX IF NOT EXISTS idx_notif_user_fecha ON public.notificaciones_usuario USING btree (id_usuario, fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_notif_user_leida ON public.notificaciones_usuario USING btree (id_usuario, leida);
CREATE INDEX IF NOT EXISTS idx_permisos_modulo ON public.permisos USING btree (modulo);
CREATE INDEX IF NOT EXISTS idx_supervisores_cedula ON public.supervisores USING btree (cedula);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON public.usuarios USING btree (rol_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON public.usuarios USING btree (username);

-- =============================================================================
-- VISTAS DEL SISTEMA (4 VISTAS EN ESQUEMA public)
-- =============================================================================
-- -----------------------------------------------------------------------------
-- VISTA: public.vista_contratos_proximos_a_vencer
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.vista_contratos_proximos_a_vencer AS
SELECT c.id AS contrato_id,
    c.numero_contrato,
    c.anio,
    ct.cedula AS cedula_contratista,
    ct.nombre AS nombres_apellidos,
    ct.telefono,
    ct.correo,
    COALESCE(s.nombre, 'Sin supervisor'::character varying) AS supervisor,
    c.fecha_inicio,
    c.fecha_terminacion,
    (c.fecha_terminacion - CURRENT_DATE) AS dias_para_vencer,
        CASE
            WHEN ((c.fecha_terminacion - CURRENT_DATE) <= 15) THEN 'Crítico (<= 15 días)'::text
            WHEN ((c.fecha_terminacion - CURRENT_DATE) <= 30) THEN 'Próximo (16 a 30 días)'::text
            WHEN ((c.fecha_terminacion - CURRENT_DATE) <= 60) THEN 'Alerta (31 a 60 días)'::text
            ELSE 'Vigente (> 60 días)'::text
        END AS nivel_alerta,
    c.plazo_ejecucion AS plazo,
    c.valor_total_numeros AS valor_contrato,
    count(inf.id) AS total_cuentas_registradas,
    count(
        CASE
            WHEN ((inf.estado_radicacion IS NOT NULL) AND ((inf.estado_radicacion)::text <> 'BORRADOR'::text)) THEN 1
            ELSE NULL::integer
        END) AS total_cuentas_radicadas
   FROM (((contratos c
     LEFT JOIN contratistas ct ON ((c.contratista_id = ct.id)))
     LEFT JOIN supervisores s ON ((c.supervisor_id = s.id)))
     LEFT JOIN informes_supervision inf ON ((inf.contrato_id = c.id)))
  WHERE ((c.fecha_terminacion >= CURRENT_DATE) AND ((c.fecha_terminacion - CURRENT_DATE) <= 60))
  GROUP BY c.id, c.numero_contrato, c.anio, ct.cedula, ct.nombre, ct.telefono, ct.correo, s.nombre, c.fecha_inicio, c.fecha_terminacion, c.plazo_ejecucion, c.valor_total_numeros
  ORDER BY c.fecha_terminacion, c.numero_contrato;

-- -----------------------------------------------------------------------------
-- VISTA: public.vista_informes_cuotas_detalle
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.vista_informes_cuotas_detalle AS
SELECT inf.id AS id_informe,
    c.id AS contrato_id,
    c.numero_contrato,
    c.anio,
    ct.nombre AS nombres_apellidos,
    ct.cedula AS cedula_contratista,
    COALESCE(s_inf.nombre, s_con.nombre, 'Sin supervisor'::character varying) AS supervisor,
    COALESCE(u.nombre_completo, 'Sin revisor'::character varying) AS revisor,
    inf.numero_cuota,
    COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying) AS estado_radicacion,
    inf.periodo_informe,
    inf.fecha_creacion
   FROM (((((informes_supervision inf
     JOIN contratos c ON ((inf.contrato_id = c.id)))
     LEFT JOIN contratistas ct ON ((c.contratista_id = ct.id)))
     LEFT JOIN supervisores s_con ON ((c.supervisor_id = s_con.id)))
     LEFT JOIN supervisores s_inf ON ((inf.supervisor_id = s_inf.id)))
     LEFT JOIN usuarios u ON ((inf.id_revisor_asignado = u.id)))
  ORDER BY inf.id DESC;

-- -----------------------------------------------------------------------------
-- VISTA: public.vista_personas_sin_cuentas_radicadas
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.vista_personas_sin_cuentas_radicadas AS
SELECT c.id AS contrato_id,
    c.numero_contrato,
    c.anio,
    ct.cedula,
    ct.nombre AS nombres_apellidos,
    ct.telefono,
    ct.correo,
    COALESCE(s.nombre, 'Sin supervisor'::character varying) AS supervisor,
    c.fecha_inicio,
    c.fecha_terminacion,
    (c.fecha_terminacion - CURRENT_DATE) AS dias_para_vencer,
    c.plazo_ejecucion AS plazo,
    c.valor_total_numeros AS valor_contrato
   FROM ((contratos c
     LEFT JOIN contratistas ct ON ((c.contratista_id = ct.id)))
     LEFT JOIN supervisores s ON ((c.supervisor_id = s.id)))
  WHERE ((c.fecha_terminacion >= CURRENT_DATE) AND (NOT (EXISTS ( SELECT 1
           FROM informes_supervision inf
          WHERE (inf.contrato_id = c.id)))))
  ORDER BY c.fecha_terminacion, c.numero_contrato;

-- -----------------------------------------------------------------------------
-- VISTA: public.vista_resumen_contratos_cuotas
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW public.vista_resumen_contratos_cuotas AS
SELECT c.id AS contrato_id,
    c.numero_contrato,
    c.anio,
    ct.nombre AS nombres_apellidos,
    ct.cedula AS cedula_contratista,
    COALESCE(s_inf.nombre, s_con.nombre, 'Sin supervisor'::character varying) AS supervisor,
    string_agg(DISTINCT (COALESCE(u.nombre_completo, 'Sin revisor'::character varying))::text, ', '::text) AS revisor,
    string_agg((inf.id)::text, ', '::text ORDER BY
        CASE
            WHEN ((inf.numero_cuota)::text ~ '^[0-9]+$'::text) THEN (inf.numero_cuota)::integer
            ELSE 9999
        END, inf.id) AS ids_informes,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '1'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_1,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '2'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_2,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '3'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_3,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '4'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_4,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '5'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_5,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '6'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_6,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '7'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_7,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '8'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_8,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '9'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_9,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '10'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_10,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '11'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_11,
    max((
        CASE
            WHEN ((inf.numero_cuota)::text = '12'::text) THEN COALESCE(inf.estado_radicacion, 'BORRADOR'::character varying)
            ELSE NULL::character varying
        END)::text) AS cuota_12,
    count(inf.id) AS total_cuotas_registradas
   FROM (((((contratos c
     LEFT JOIN contratistas ct ON ((c.contratista_id = ct.id)))
     LEFT JOIN supervisores s_con ON ((c.supervisor_id = s_con.id)))
     JOIN informes_supervision inf ON ((inf.contrato_id = c.id)))
     LEFT JOIN supervisores s_inf ON ((inf.supervisor_id = s_inf.id)))
     LEFT JOIN usuarios u ON ((inf.id_revisor_asignado = u.id)))
  GROUP BY c.id, c.numero_contrato, c.anio, ct.nombre, ct.cedula, COALESCE(s_inf.nombre, s_con.nombre, 'Sin supervisor'::character varying)
  ORDER BY c.id DESC;
