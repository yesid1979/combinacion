package com.combinacion.util;

import com.combinacion.dao.RolDAO;
import com.combinacion.dao.UsuarioDAO;
import com.combinacion.models.Rol;
import com.combinacion.models.Usuario;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Inicializador que ejecuta el schema de autenticación al arrancar la aplicación
 * y crea el usuario administrador por defecto si no existe.
 */
@WebListener
public class AuthInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[AuthInitializer] Inicializando módulo de autenticación...");

        // 1. Ejecutar el schema SQL
        ejecutarSchema();
        
        // 1.1 Asegurar columnas nuevas
        asegurarColumnasPerfil();

        // 2. Crear usuario admin por defecto si no existe
        crearUsuarioAdminPorDefecto();

        System.out.println("[AuthInitializer] Módulo de autenticación listo.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nada que limpiar
    }

    private void ejecutarSchema() {
        try (Connection conn = DBConnection.getConnection()) {
            InputStream is = getClass().getClassLoader().getResourceAsStream("schema_auth.sql");
            if (is == null) {
                System.out.println("[AuthInitializer] ADVERTENCIA: schema_auth.sql no encontrado en classpath.");
                return;
            }

            StringBuilder sql = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignorar comentarios de línea
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    continue;
                }
                sql.append(line).append("\n");
            }
            reader.close();

            // Ejecutar cada sentencia separada por ;
            String[] statements = sql.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String s : statements) {
                    String trimmedStmt = s.trim();
                    if (!trimmedStmt.isEmpty()) {
                        try {
                            stmt.execute(trimmedStmt);
                        } catch (Exception e) {
                            // Ignorar errores de "ya existe" (ON CONFLICT, IF NOT EXISTS)
                            if (!e.getMessage().contains("already exists")
                                && !e.getMessage().contains("ya existe")
                                && !e.getMessage().contains("duplicate")) {
                                System.out.println("[AuthInitializer] Advertencia SQL: " + e.getMessage());
                            }
                        }
                    }
                }
            }
            System.out.println("[AuthInitializer] Schema de autenticación ejecutado correctamente.");
        } catch (Exception e) {
            System.out.println("[AuthInitializer] ERROR ejecutando schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void crearUsuarioAdminPorDefecto() {
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario existente = usuarioDAO.obtenerPorUsername("admin");

            // Generar nuevo par hash/salt
            String salt = PasswordUtils.generateSalt();
            String hash = PasswordUtils.hashPassword("admin123", salt);

            if (existente == null) {
                // Obtener el rol Administrador
                RolDAO rolDAO = new RolDAO();
                Rol rolAdmin = rolDAO.obtenerPorNombre("Administrador");

                if (rolAdmin == null) {
                    System.out.println("[AuthInitializer] ADVERTENCIA: Rol 'Administrador' no encontrado. No se puede crear usuario admin.");
                    return;
                }

                // Crear usuario admin con contraseña por defecto
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPasswordHash(hash);
                admin.setSalt(salt);
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setCorreo("admin@alcaldiacali.gov.co");
                admin.setActivo(true);
                admin.setRolId(rolAdmin.getId());

                int id = usuarioDAO.insertar(admin);
                if (id > 0) {
                    System.out.println("[AuthInitializer] Usuario 'admin' creado exitosamente.");
                }
            } else {
                // Solo actualizamos el acceso, no el nombre ni nada más para no borrar cambios del usuario
                usuarioDAO.actualizarUltimoAcceso(existente.getId());
                System.out.println("[AuthInitializer] Usuario 'admin' verificado.");
            }
        } catch (Exception e) {
            System.out.println("[AuthInitializer] ERROR en crearUsuarioAdminPorDefecto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void asegurarColumnasPerfil() {
        String sql = "ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS foto_url VARCHAR(255);";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[AuthInitializer] Columna 'foto_url' verificada/creada.");
        } catch (Exception e) {
            System.out.println("[AuthInitializer] Nota: No se pudo verificar la columna 'foto_url' (posiblemente ya existe).");
        }
    }
}
