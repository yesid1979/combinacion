package com.combinacion.util;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Gestión de la conexión a la base de datos mediante JNDI.
 */
public class DBConnection {

    private static DataSource dataSource;

    static {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/combinacion");
            System.out.println("[DBConnection] Recurso JNDI 'jdbc/combinacion' cargado exitosamente.");
        } catch (NamingException e) {
            System.err.println("[DBConnection] ERROR AL CARGAR JNDI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            // Re-intento manual si falló la carga estática inicial
            try {
                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:comp/env");
                dataSource = (DataSource) envContext.lookup("jdbc/combinacion");
            } catch (NamingException e) {
                System.err.println("[DBConnection] JNDI no disponible, intentando conexion directa JDBC...");
            }
        }
        
        if (dataSource == null) {
            // FALLBACK DIRECTO PARA EJECUCION FUERA DE TOMCAT (EJ. METODO MAIN)
            try {
                Class.forName("org.postgresql.Driver");
                return java.sql.DriverManager.getConnection(
                    "jdbc:postgresql://10.30.80.53:5432/combinacion?options=-c%20client_encoding=UTF8",
                    "adminjuridica",
                    "Produccion2023*"
                );
            } catch (Exception ex) {
                throw new SQLException("El DataSource JNDI no está disponible y falló la conexión directa.", ex);
            }
        }
        
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error al obtener conexión física: " + e.getMessage());
            throw e;
        }
    }
}
