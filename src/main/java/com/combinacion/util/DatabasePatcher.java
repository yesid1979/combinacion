package com.combinacion.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabasePatcher {

    public static void ensureSchema() {
        System.out.println("Verificando esquema de base de datos...");
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Verificamos si la columna existe intentando seleccionarla (manera 'lazy' pero
            // efectiva para compatibilidad)
            // O mejor, consultamos information_schema

            ResultSet rs = stmt.executeQuery(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE table_name='contratos' AND column_name='apoyo_supervision'");

            if (!rs.next()) {
                System.out.println("⚠️ Columna 'apoyo_supervision' no encontrada. Agregándola automáticamente...");
                stmt.executeUpdate("ALTER TABLE contratos ADD COLUMN apoyo_supervision TEXT");
                System.out.println("✅ Columna 'apoyo_supervision' agregada exitosamente.");
            } else {
                System.out.println("✅ La columna 'apoyo_supervision' ya existe.");
            }

            // --- NUEVOS CAMPOS PARA ADICION EN PRESUPUESTO_DETALLES ---
            String[] columnasPresupuesto = {
                "cdp_adicion TEXT", 
                "cdp_valor_adicion NUMERIC", 
                "rp_adicion TEXT", 
                "rp_fecha_adicion DATE"
            };

            for (String colInfo : columnasPresupuesto) {
                String colName = colInfo.split(" ")[0];
                ResultSet rsP = stmt.executeQuery(
                        "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_name='presupuesto_detalles' AND column_name='" + colName + "'");
                
                if (!rsP.next()) {
                    System.out.println("⚠️ Columna '" + colName + "' no encontrada en presupuesto_detalles. Agregándola...");
                    stmt.executeUpdate("ALTER TABLE presupuesto_detalles ADD COLUMN " + colInfo);
                    System.out.println("✅ Columna '" + colName + "' agregada.");
                }
            }

            // Verificar tabla usuario_permisos
            ResultSet rs2 = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.tables WHERE table_name='usuario_permisos'");
            if (!rs2.next()) {
                System.out.println("⚠️ Tabla 'usuario_permisos' no encontrada. Creándola...");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS usuario_permisos (" +
                                 "usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE, " +
                                 "permiso_id INT REFERENCES permisos(id) ON DELETE CASCADE, " +
                                 "PRIMARY KEY (usuario_id, permiso_id))");
                System.out.println("✅ Tabla 'usuario_permisos' creada exitosamente.");
            }

            // Asegurar permisos VER TODO para la mallas dinámica
            String[] modulosPerms = {"ADMINISTRACION", "CARGA_MASIVA", "COMBINACION", "CONTRATISTAS", "CONTRATOS", "ORDENADORES", "PRESUPUESTO", "SUPERVISORES"};
            for (String mod : modulosPerms) {
                String cod = mod + "_VER_TODO";
                if (mod.equals("ADMINISTRACION")) cod = "ADMIN_VER_TODO";
                stmt.executeUpdate("INSERT INTO permisos (codigo, nombre, modulo, descripcion) " +
                                 "VALUES ('" + cod + "', 'Ver todo', '" + mod + "', 'Ver todo del módulo " + mod + "') " +
                                 "ON CONFLICT (codigo) DO NOTHING");
            }

            // Asegurar permisos para el módulo ADMINISTRACION (CRUD completo)
            String[] adminPerms = {"ADMIN_EDITAR", "ADMIN_ELIMINAR", "ADMIN_VER"};
            for (String ap : adminPerms) {
                stmt.executeUpdate("INSERT INTO permisos (codigo, nombre, modulo, descripcion) " +
                                 "VALUES ('" + ap + "', 'Gestionar admin', 'ADMINISTRACION', 'Permisos de administración') " +
                                 "ON CONFLICT (codigo) DO NOTHING");
            }

            // Asegurar que el rol Administrador tenga TODOS los permisos incluyendo los nuevos
            stmt.executeUpdate("INSERT INTO rol_permisos (rol_id, permiso_id) " +
                             "SELECT r.id, p.id FROM roles r, permisos p " +
                             "WHERE r.nombre = 'Administrador' " +
                             "ON CONFLICT DO NOTHING");

            System.out.println("✅ Todos los permisos de ADMINISTRACION asegurados.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
