package com.ecommerce.ventas.dao;

import com.ecommerce.ventas.model.Producto;
import com.ecommerce.ventas.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ProductoDAOImpl implements ProductoDAO {

    private static final String INSERT_SQL = "INSERT INTO producto (nombre, precio, stock) VALUES (?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT codigo, nombre, precio, stock FROM producto ORDER BY codigo";
    private static final String SELECT_BY_NAME_SQL = "SELECT codigo, nombre, precio, stock FROM producto WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
    private static final String SELECT_BY_ID_SQL = "SELECT codigo, nombre, precio, stock FROM producto WHERE codigo = ?";
    private static final String UPDATE_SQL = "UPDATE producto SET nombre = ?, precio = ?, stock = ? WHERE codigo = ?";
    private static final String DELETE_SQL = "DELETE FROM producto WHERE codigo = ?";

    @Override
    public void insertar(Producto producto) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, producto.getNombre());
            pstmt.setBigDecimal(2, producto.getPrecio());
            pstmt.setInt(3, producto.getStock());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    producto.setCodigo(generatedKeys.getInt(1));
                    System.out.println("Producto insertado con código: " + producto.getCodigo());
                }
            } else {
                 System.err.println("No se pudo insertar el producto.");
            }

        } finally {
            DatabaseConnection.close(generatedKeys);
            DatabaseConnection.close(pstmt);
        }
    }

    @Override
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_ALL_SQL);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        } finally {
            DatabaseConnection.close(rs);
            DatabaseConnection.close(pstmt);
        }
        return productos;
    }

    @Override
    public List<Producto> buscarPorNombre(String nombre) throws SQLException {
         List<Producto> productos = new ArrayList<>();
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_BY_NAME_SQL);
            pstmt.setString(1, "%" + nombre + "%");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        } finally {
             DatabaseConnection.close(rs);
             DatabaseConnection.close(pstmt);
        }
        return productos;
    }

     @Override
    public Producto obtenerPorCodigo(int codigo) throws SQLException {
        Producto producto = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(SELECT_BY_ID_SQL);
            pstmt.setInt(1, codigo);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                producto = mapResultSetToProducto(rs);
            }
        } finally {
             DatabaseConnection.close(rs);
             DatabaseConnection.close(pstmt);
        }
        return producto;
    }

    @Override
    public boolean actualizar(Producto producto) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean actualizado = false;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(UPDATE_SQL);
            pstmt.setString(1, producto.getNombre());
            pstmt.setBigDecimal(2, producto.getPrecio());
            pstmt.setInt(3, producto.getStock());
            pstmt.setInt(4, producto.getCodigo());

            int filasAfectadas = pstmt.executeUpdate();
            actualizado = (filasAfectadas > 0);
        } finally {
             DatabaseConnection.close(pstmt);
        }
        return actualizado;
    }

    @Override
    public boolean eliminar(int codigo) throws SQLException {
         Connection conn = null;
         PreparedStatement pstmt = null;
         boolean eliminado = false;
        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(DELETE_SQL);
            pstmt.setInt(1, codigo);

            int filasAfectadas = pstmt.executeUpdate();
            eliminado = (filasAfectadas > 0);
        } finally {
             DatabaseConnection.close(pstmt);
        }
        return eliminado;
    }

    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        int codigo = rs.getInt("codigo");
        String nombre = rs.getString("nombre");
        BigDecimal precio = rs.getBigDecimal("precio");
        int stock = rs.getInt("stock");
        return new Producto(codigo, nombre, precio, stock);
    }
}
