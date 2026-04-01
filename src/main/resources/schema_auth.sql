-- ============================================================
-- MÃ³dulo de AutenticaciÃ³n, Roles y Permisos
-- Base de datos: PostgreSQL
-- ============================================================

-- Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Permisos
CREATE TABLE IF NOT EXISTS permisos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    modulo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255)
);

-- RelaciÃ³n N:M entre Roles y Permisos
CREATE TABLE IF NOT EXISTS rol_permisos (
    rol_id INT REFERENCES roles(id) ON DELETE CASCADE,
    permiso_id INT REFERENCES permisos(id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    correo VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    ultimo_acceso TIMESTAMP,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rol_id INT REFERENCES roles(id)
);

-- Indices
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios(rol_id);
CREATE INDEX IF NOT EXISTS idx_permisos_modulo ON permisos(modulo);

-- ============================================================
-- DATOS INICIALES (SEED)
-- ============================================================

-- Roles
INSERT INTO roles (nombre, descripcion) VALUES
    ('Administrador', 'Acceso total al sistema, gestiÃ³n de usuarios y roles'),
    ('Contratacion', 'Puede crear, editar y eliminar contratos y datos maestros'),
    ('Consulta', 'Solo puede consultar informaciÃ³n, sin modificar')
ON CONFLICT (nombre) DO NOTHING;

-- Permisos
INSERT INTO permisos (codigo, nombre, modulo, descripcion) VALUES
    -- MÃ³dulo CONTRATOS
    ('CONTRATOS_VER',       'Ver contratos',        'CONTRATOS',      'Permite ver la lista y detalle de contratos'),
    ('CONTRATOS_CREAR',     'Crear contratos',       'CONTRATOS',      'Permite crear nuevos contratos'),
    ('CONTRATOS_EDITAR',    'Editar contratos',      'CONTRATOS',      'Permite editar contratos existentes'),
    ('CONTRATOS_ELIMINAR',  'Eliminar contratos',    'CONTRATOS',      'Permite eliminar contratos'),
    -- MÃ³dulo CONTRATISTAS
    ('CONTRATISTAS_VER',       'Ver contratistas',        'CONTRATISTAS',   'Permite ver la lista de contratistas'),
    ('CONTRATISTAS_CREAR',     'Crear contratistas',       'CONTRATISTAS',   'Permite crear nuevos contratistas'),
    ('CONTRATISTAS_EDITAR',    'Editar contratistas',      'CONTRATISTAS',   'Permite editar contratistas'),
    ('CONTRATISTAS_ELIMINAR',  'Eliminar contratistas',    'CONTRATISTAS',   'Permite eliminar contratistas'),
    -- MÃ³dulo SUPERVISORES
    ('SUPERVISORES_VER',       'Ver supervisores',        'SUPERVISORES',   'Permite ver la lista de supervisores'),
    ('SUPERVISORES_CREAR',     'Crear supervisores',       'SUPERVISORES',   'Permite crear nuevos supervisores'),
    ('SUPERVISORES_EDITAR',    'Editar supervisores',      'SUPERVISORES',   'Permite editar supervisores'),
    ('SUPERVISORES_ELIMINAR',  'Eliminar supervisores',    'SUPERVISORES',   'Permite eliminar supervisores'),
    -- MÃ³dulo ORDENADORES
    ('ORDENADORES_VER',       'Ver ordenadores',        'ORDENADORES',    'Permite ver la lista de ordenadores del gasto'),
    ('ORDENADORES_CREAR',     'Crear ordenadores',       'ORDENADORES',    'Permite crear nuevos ordenadores'),
    ('ORDENADORES_EDITAR',    'Editar ordenadores',      'ORDENADORES',    'Permite editar ordenadores'),
    ('ORDENADORES_ELIMINAR',  'Eliminar ordenadores',    'ORDENADORES',    'Permite eliminar ordenadores'),
    -- MÃ³dulo PRESUPUESTO
    ('PRESUPUESTO_VER',    'Ver presupuesto',     'PRESUPUESTO',    'Permite ver detalles presupuestales'),
    ('PRESUPUESTO_CREAR',  'Crear presupuesto',   'PRESUPUESTO',    'Permite crear detalles presupuestales'),
    ('PRESUPUESTO_EDITAR', 'Editar presupuesto',  'PRESUPUESTO',    'Permite editar detalles presupuestales'),
    -- MÃ³dulo COMBINACION
    ('COMBINACION_VER',      'Ver combinaciÃ³n',      'COMBINACION',    'Permite acceder al mÃ³dulo de combinaciÃ³n'),
    ('COMBINACION_GENERAR',  'Generar documentos',   'COMBINACION',    'Permite generar documentos contractuales'),
    -- MÃ³dulo CARGA MASIVA
    ('CARGA_MASIVA_EJECUTAR', 'Carga masiva',        'CARGA_MASIVA',   'Permite ejecutar cargas masivas de datos'),
    -- Módulo ADMINISTRACION
    ('ADMIN_VER',       'Ver administración',  'ADMINISTRACION', 'Acceso básico al módulo admin'),
    ('ADMIN_USUARIOS',  'Gestionar usuarios',  'ADMINISTRACION', 'Permite gestionar usuarios del sistema'),
    ('ADMIN_ROLES',     'Gestionar roles',     'ADMINISTRACION', 'Permite gestionar roles y permisos'),
    ('ADMIN_EDITAR',    'Editar configuración', 'ADMINISTRACION', 'Permite editar configuraciones de administración'),
    ('ADMIN_ELIMINAR',  'Eliminar datos admin', 'ADMINISTRACION', 'Permite eliminar registros de administración'),
    -- Permisos VER TODO (Para la mallas dinámica)
    ('CONTRATOS_VER_TODO',     'Ver todo', 'CONTRATOS',      'Ver todos los contratos'),
    ('CONTRATISTAS_VER_TODO',  'Ver todo', 'CONTRATISTAS',   'Ver todos los contratistas'),
    ('SUPERVISORES_VER_TODO',   'Ver todo', 'SUPERVISORES',   'Ver todos los supervisores'),
    ('ORDENADORES_VER_TODO',    'Ver todo', 'ORDENADORES',    'Ver todos los ordenadores'),
    ('PRESUPUESTO_VER_TODO',    'Ver todo', 'PRESUPUESTO',    'Ver todo el presupuesto'),
    ('COMBINACION_VER_TODO',    'Ver todo', 'COMBINACION',    'Ver toda la combinación'),
    ('CARGA_MASIVA_VER_TODO',   'Ver todo', 'CARGA_MASIVA',   'Ver todas las cargas masivas'),
    ('ADMIN_VER_TODO',          'Ver todo', 'ADMINISTRACION', 'Ver toda la administración')
ON CONFLICT (codigo) DO NOTHING;

-- Asignar TODOS los permisos al rol Administrador
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'Administrador'
ON CONFLICT DO NOTHING;

-- Asignar permisos operativos al rol Contratacion (todo menos ADMIN)
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'Contratacion'
  AND p.modulo NOT IN ('ADMINISTRACION')
ON CONFLICT DO NOTHING;

-- Asignar solo permisos de lectura al rol Consulta
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r, permisos p
WHERE r.nombre = 'Consulta'
  AND p.codigo LIKE '%_VER'
ON CONFLICT DO NOTHING;

-- Usuario administrador por defecto
-- Password: admin123 (SHA-256 con salt fijo para seed - CAMBIAR tras primer login)
-- Salt: 'seed_salt_change_me'
-- Hash de 'seed_salt_change_meadmin123' en SHA-256
-- NOTA: El hash real se genera en la aplicaciÃ³n. Este es un placeholder
--       que serÃ¡ reemplazado por el DatabasePatcher al iniciar.

-- Tabla para Permisos Dinámicos por Usuario
CREATE TABLE IF NOT EXISTS usuario_permisos (
    usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE,
    permiso_id INT REFERENCES permisos(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, permiso_id)
);
