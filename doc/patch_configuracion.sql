-- Script para crear la tabla de configuración
CREATE TABLE IF NOT EXISTS configuracion (
    id SERIAL PRIMARY KEY, -- O INT AUTO_INCREMENT si es MySQL
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(500) NOT NULL,
    descripcion VARCHAR(255)
);

-- Insertar valores iniciales (las carpetas que ya existen en el código)
INSERT INTO configuracion (clave, valor, descripcion) VALUES
('DRIVE_CARPETA_SISTEMA', 'configuracion_SistemaContratacion', 'Carpeta raíz principal para archivos del sistema'),
('DRIVE_CARPETA_PRUEBAS', 'pruebas cuenta de cobro', 'Carpeta raíz para las pruebas de cuenta de cobro'),
('DRIVE_CARPETA_FIRMAS', 'FIRMAS_CONTRATISTAS', 'Carpeta donde se guardan las firmas de los contratistas'),
('DRIVE_CARPETA_IMAGENES_WEB', 'IMAGENES_EDITOR_WEB', 'Carpeta donde se suben imágenes del editor web'),
('DRIVE_CARPETA_EVIDENCIAS', 'EVIDENCIAS', 'Carpeta donde se suben las evidencias de los informes');
