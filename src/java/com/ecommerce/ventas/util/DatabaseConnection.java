package com.ecommerce.ventas.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DatabaseConnection {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Ventas?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "abcd123"; 

    private static Connection connection = null;


    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Registrar el driver JDBC
                Class.forName(JDBC_DRIVER);
                // Abrir la conexión
                System.out.println("Intentando conectar a la base de datos...");
                connection = DriverManager.getConnection(DB_URL, USER, PASS);
                System.out.println("¡Conexión establecida exitosamente!");
            } catch (ClassNotFoundException e) {
                System.err.println("Error: No se encontró el driver JDBC. Asegúrate de que el JAR esté en las librerías.");
                e.printStackTrace();
                throw new SQLException("Driver no encontrado", e);
            } catch (SQLException e) {
                System.err.println("Error al conectar a la base de datos.");
                e.printStackTrace();
                throw e; 
            }
        }
        return connection;
    }

    /**
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión.");
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }

    /**
     * @param rs El ResultSet a cerrar.
     */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error cerrando ResultSet: " + e.getMessage());
            }
        }
    }

    /**
     * @param stmt El Statement a cerrar.
     */
    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error cerrando Statement: " + e.getMessage());
            }
        }
    }

     /**
     * @param pstmt El PreparedStatement a cerrar.
     */
    public static void close(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error cerrando PreparedStatement: " + e.getMessage());
            }
        }
    }

     /**
     * @param conn La Connection a cerrar.
     */
     public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error cerrando Connection: " + e.getMessage());
            }
        }
    }
}
