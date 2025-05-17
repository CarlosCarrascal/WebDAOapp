package com.product.dao.impl;

import com.product.dao.ProductoDAO;
import com.product.model.Producto;
import com.product.util.MySqlConexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ProductoMySqlDAO implements ProductoDAO {

    private static final String SQL_INSERTAR_PRODUCTO = "INSERT INTO producto (nombre, precio, stock) VALUES (?, ?, ?)";
    private static final String SQL_LISTAR_PRODUCTOS = "SELECT codigo, nombre, precio, stock FROM producto ORDER BY codigo";
    private static final String SQL_BUSCAR_POR_NOMBRE = "SELECT codigo, nombre, precio, stock FROM producto WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
    private static final String SQL_BUSCAR_POR_CODIGO = "SELECT codigo, nombre, precio, stock FROM producto WHERE codigo = ?";
    private static final String SQL_ACTUALIZAR_PRODUCTO = "UPDATE producto SET nombre = ?, precio = ?, stock = ? WHERE codigo = ?";
    private static final String SQL_ELIMINAR_PRODUCTO = "DELETE FROM producto WHERE codigo = ?";

    @Override
    public void insertar(Producto producto) throws SQLException {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        validarDatosProducto(producto);

        try (Connection conn = MySqlConexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERTAR_PRODUCTO, Statement.RETURN_GENERATED_KEYS)) {
            
            establecerParametrosProducto(pstmt, producto);
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        producto.setCodigo(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        
        try (Connection conn = MySqlConexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_LISTAR_PRODUCTOS);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        }
        return productos;
    }

    @Override
    public List<Producto> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de búsqueda no puede estar vacío");
        }

        List<Producto> productos = new ArrayList<>();
        try (Connection conn = MySqlConexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {
            
            pstmt.setString(1, "%" + nombre.trim() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapResultSetToProducto(rs));
                }
            }
        }
        return productos;
    }

    @Override
    public Producto obtenerPorCodigo(int codigo) throws SQLException {
        if (codigo <= 0) {
            throw new IllegalArgumentException("El código debe ser mayor que cero");
        }

        try (Connection conn = MySqlConexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_BUSCAR_POR_CODIGO)) {
            
            pstmt.setInt(1, codigo);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToProducto(rs) : null;
            }
        }
    }

    @Override
    public boolean actualizar(Producto producto) throws SQLException {
        if (producto == null || producto.getCodigo() <= 0) {
            throw new IllegalArgumentException("Producto inválido para actualización");
        }
        validarDatosProducto(producto);

        try (Connection conn = MySqlConexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_ACTUALIZAR_PRODUCTO)) {
            
            establecerParametrosActualizacion(pstmt, producto);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int codigo) throws SQLException {
        if (codigo <= 0) {
            throw new IllegalArgumentException("El código debe ser mayor que cero");
        }

        try (Connection conn = MySqlConexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_ELIMINAR_PRODUCTO)) {
            
            pstmt.setInt(1, codigo);
            return pstmt.executeUpdate() > 0;
        }
    }

    private void validarDatosProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es requerido");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a cero");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    private void establecerParametrosProducto(PreparedStatement pstmt, Producto producto) throws SQLException {
        pstmt.setString(1, producto.getNombre().trim());
        pstmt.setBigDecimal(2, producto.getPrecio());
        pstmt.setInt(3, producto.getStock());
    }

    private void establecerParametrosActualizacion(PreparedStatement pstmt, Producto producto) throws SQLException {
        establecerParametrosProducto(pstmt, producto);
        pstmt.setInt(4, producto.getCodigo());
    }

    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        return new Producto(
            rs.getInt("codigo"),
            rs.getString("nombre"),
            rs.getBigDecimal("precio"),
            rs.getInt("stock")
        );
    }
}
