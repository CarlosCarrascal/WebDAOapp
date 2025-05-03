package com.ecommerce.ventas.dao;

import com.ecommerce.ventas.model.Producto;
import java.sql.SQLException;
import java.util.List;

public interface ProductoDAO {

    void insertar(Producto producto) throws SQLException;
    List<Producto> listarTodos() throws SQLException;
    List<Producto> buscarPorNombre(String nombre) throws SQLException;
    Producto obtenerPorCodigo(int codigo) throws SQLException;
    boolean actualizar(Producto producto) throws SQLException;
    boolean eliminar(int codigo) throws SQLException;
}
