package com.product.dao.impl;

import com.product.model.Producto;
import com.product.util.ExcelManager;
import com.product.dao.helpers.ExcelProductoMapper;
import com.product.dao.ProductoDAO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList; 
import java.util.stream.Collectors;

public class ProductoExcelDAO implements ProductoDAO {

    private static final String RUTA_ARCHIVO = "C:/DAO/productos_ventas.xlsx"; 
    private static final String NOMBRE_HOJA = "Productos";
    private static final String[] ENCABEZADOS = {"Codigo", "Nombre", "Precio", "Stock"};

    private final ExcelProductoMapper mapper;

    // Constructor: Inicializa el mapper y asegura que el archivo Excel exista
    public ProductoExcelDAO() {
        this.mapper = new ExcelProductoMapper();
        iniciarArchivoSiFalta(); 
    }

    // Crea el archivo Excel con encabezados solo si no existe.
    private void iniciarArchivoSiFalta() {
        File archivo = new File(RUTA_ARCHIVO);
        if (archivo.exists()) {
            return; 
        }
        Workbook libro = null;
        try {
            libro = ExcelManager.obtenerLibro(RUTA_ARCHIVO); 
            ExcelManager.asegurarHojaEncabezado(libro, NOMBRE_HOJA, ENCABEZADOS);
            ExcelManager.guardarCerrarLibro(libro, RUTA_ARCHIVO); 
            libro = null; 
        } catch (IOException e) {
            System.err.println("No se pudo crear archivo Excel: " + RUTA_ARCHIVO + " - " + e.getMessage());
        } finally {
            if (libro != null) {
                try { libro.close(); } catch (IOException ex) { }
            }
        }
    }
    
    // Lee la lista completa de productos desde el archivo Excel
    private synchronized List<Producto> leerArchivo() throws IOException {
        Workbook libro = null;
        List<Producto> lista = new ArrayList<>();
        try {
            libro = ExcelManager.obtenerLibro(RUTA_ARCHIVO);
            Sheet hoja = ExcelManager.obtenerHoja(libro, NOMBRE_HOJA, false); 
            
            if (hoja != null) { 
                lista = mapper.leerProductos(hoja);
            }
        } finally {
            if (libro != null) {
                libro.close(); 
            }
        }
        return lista;
    }

    // Escribe la lista completa de productos al archivo Excel
    private synchronized void escribirArchivo(List<Producto> lista) throws IOException {
        Workbook libro = null;
        try {
            libro = ExcelManager.obtenerLibro(RUTA_ARCHIVO); 
            Sheet hoja = ExcelManager.asegurarHojaEncabezado(libro, NOMBRE_HOJA, ENCABEZADOS); 
            
            mapper.escribirProductos(hoja, lista, ENCABEZADOS);
            ExcelManager.guardarCerrarLibro(libro, RUTA_ARCHIVO); 
            libro = null; 
        } finally {
            if (libro != null) { 
                try { libro.close(); } catch (IOException ex) { }
            }
        }
    }

    @Override
    public List<Producto> listarTodos() throws SQLException {
        try {
            return leerArchivo();
        } catch (IOException e) {
            throw new SQLException("Excel DAO: Error al listar. " + e.getMessage(), e);
        }
    }

    @Override
    public void insertar(Producto producto) throws SQLException {
        if (producto == null) { 
            throw new IllegalArgumentException("Producto a insertar no puede ser nulo.");
        }
        try {
            List<Producto> listaActual = leerArchivo();
            int nuevoCodigo = mapper.obtenerSgteCodigo(listaActual);
            producto.setCodigo(nuevoCodigo); 
            
            listaActual.add(producto);
            escribirArchivo(listaActual);
        } catch (IOException e) {
            throw new SQLException("Excel DAO: Error al insertar. " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Producto productoModif) throws SQLException {
        if (productoModif == null) { 
            return false; 
        }
        try {
            List<Producto> listaActual = leerArchivo();
            boolean actualizado = false;
            for (int i = 0; i < listaActual.size(); i++) {
                Producto pExistente = listaActual.get(i);
                if (pExistente != null && pExistente.getCodigo() == productoModif.getCodigo()) {
                    listaActual.set(i, productoModif);
                    actualizado = true;
                    break; 
                }
            }
            if (actualizado) {
                escribirArchivo(listaActual);
            }
            return actualizado;
        } catch (IOException e) {
            throw new SQLException("Excel DAO: Error al actualizar. " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int codigo) throws SQLException {
        try {
            List<Producto> listaActual = leerArchivo();
            boolean eliminado = listaActual.removeIf(p -> p != null && p.getCodigo() == codigo);
            if (eliminado) {
                escribirArchivo(listaActual);
            }
            return eliminado;
        } catch (IOException e) {
            throw new SQLException("Excel DAO: Error al eliminar. " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Producto> buscarPorNombre(String nombre) throws SQLException {
        List<Producto> todos = listarTodos(); 
        
        if (nombre == null) return todos;
        if (nombre.trim().isEmpty()) return todos;

        String nombreBusq = nombre.trim().toLowerCase();
        return todos.stream()
                    .filter(p -> p != null && p.getNombre() != null && p.getNombre().toLowerCase().contains(nombreBusq))
                    .collect(Collectors.toList());
    }

    @Override
    public Producto obtenerPorCodigo(int codigo) throws SQLException {
        List<Producto> todos = listarTodos();
        for (Producto p : todos) { 
            if (p != null && p.getCodigo() == codigo) {
                return p; 
            }
        }
        return null; 
    }
}
