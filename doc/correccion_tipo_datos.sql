-- Script para corregir el tipo de dato de la columna actividades_entregables
-- El problema reportado sugiere que el texto se está cortando, lo cual indica un límite en la base de datos (posiblemente VARCHAR(255)).
-- Este script cambia el tipo a TEXT, que en PostgreSQL permite longitud ilimitada.

ALTER TABLE contratos ALTER COLUMN actividades_entregables TYPE TEXT;

-- Opcionalmente, verificar y corregir también 'objeto' si presenta el mismo problema
ALTER TABLE contratos ALTER COLUMN objeto TYPE TEXT;
