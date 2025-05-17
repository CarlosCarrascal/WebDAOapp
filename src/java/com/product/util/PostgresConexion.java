package com.product.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class PostgresConexion {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/Ventas";
    private static final String USER = "postgres";
    private static final String PASS = "abcd123@/"; // Cambia esto a tu contraseña real

    private static Connection connection = null;

    // Crea la conexión singleton a la base de datos PostgreSQL.
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Cargar explícitamente el driver
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(DB_URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("No se pudo cargar el driver de PostgreSQL: " + e.getMessage(), e);
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return connection;
    }

    // Cierra la conexión singleton principal
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection = null; 
        }
    }

    // Método para cerrar cualquier recurso AutoCloseable
    private static void closeResource(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        }
    }

    public static void close(ResultSet rs) {
        closeResource(rs);
    }

    public static void close(Statement stmt) {
        closeResource(stmt);
    }

    public static void close(PreparedStatement pstmt) {
        closeResource(pstmt);
    }
}
