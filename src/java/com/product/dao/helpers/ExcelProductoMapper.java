package com.product.dao.helpers; 

import com.product.model.Producto;

import org.apache.poi.ss.usermodel.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

public class ExcelProductoMapper {

    private static final int COL_CODIGO = 0;
    private static final int COL_NOMBRE = 1;
    private static final int COL_PRECIO = 2;
    private static final int COL_STOCK = 3;

    // Métodos Helper Privados para leer valores de celdas

    private Integer leerIntCelda(Cell celda) {
        if (celda == null) return null; 
        if (celda.getCellType() == CellType.NUMERIC) {
            return (int) celda.getNumericCellValue(); 
        }
        return null;
    }

    private String leerStrCelda(Cell celda) {
        if (celda == null) return "";
        if (celda.getCellType() == CellType.STRING) return celda.getStringCellValue();
        if (celda.getCellType() == CellType.NUMERIC) return String.valueOf(celda.getNumericCellValue());
        return "";
    }

    private BigDecimal leerDecimalCelda(Cell celda) {
        if (celda == null) return BigDecimal.ZERO; 
        if (celda.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(celda.getNumericCellValue());
        }
        if (celda.getCellType() == CellType.STRING) {
            try {
                return new BigDecimal(celda.getStringCellValue().replace(",", "."));
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO; 
            }
        }
        return BigDecimal.ZERO;
    }

    // Métodos de Mapeo

    private Producto filaAProducto(Row fila) {
        if (fila == null) return null;

        Integer codigo = leerIntCelda(fila.getCell(COL_CODIGO, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
        if (codigo == null) return null; 

        Producto producto = new Producto();
        producto.setCodigo(codigo); 
        producto.setNombre(leerStrCelda(fila.getCell(COL_NOMBRE, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
        producto.setPrecio(leerDecimalCelda(fila.getCell(COL_PRECIO, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
        
        Integer stock = leerIntCelda(fila.getCell(COL_STOCK, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
        producto.setStock(stock != null ? stock : 0); 

        return producto;
    }
    
    private void productoAFila(Producto producto, Row fila) {
        fila.createCell(COL_CODIGO).setCellValue(producto.getCodigo());
        fila.createCell(COL_NOMBRE).setCellValue(producto.getNombre());
        
        BigDecimal precio = producto.getPrecio();
        if (precio == null) { 
            fila.createCell(COL_PRECIO).setBlank();
        } else {
            fila.createCell(COL_PRECIO).setCellValue(precio.doubleValue());
        }
        fila.createCell(COL_STOCK).setCellValue(producto.getStock());
    }

    // Métodos Públicos utilizados por el DAO

    public List<Producto> leerProductos(Sheet hoja) {
        List<Producto> listaProds = new ArrayList<>();
        if (hoja == null) return listaProds;

        Iterator<Row> iteradorFilas = hoja.iterator();
        if (!iteradorFilas.hasNext()) return listaProds; 
        
        iteradorFilas.next(); 

        while (iteradorFilas.hasNext()) {
            Producto p = filaAProducto(iteradorFilas.next());
            if (p != null) listaProds.add(p);
        }
        return listaProds;
    }

    public void escribirProductos(Sheet hoja, List<Producto> listaProds, String[] encabezados) {
        if (hoja == null) return;
        if (listaProds == null) listaProds = new ArrayList<>();

        Row filaEncabezado = hoja.getRow(0); 
        if (filaEncabezado == null) filaEncabezado = hoja.createRow(0);
        for(int i=0; i < encabezados.length; i++) {
            Cell celda = filaEncabezado.getCell(i);
            if (celda == null) celda = filaEncabezado.createCell(i);
            celda.setCellValue(encabezados[i]);
        }
        
        int ultimaFila = hoja.getLastRowNum();
        for (int i = ultimaFila; i > 0; i--) { 
            Row fila = hoja.getRow(i);
            if (fila != null) hoja.removeRow(fila);
        }

        int numFila = 1; 
        for (Producto p : listaProds) {
            if (p != null) productoAFila(p, hoja.createRow(numFila++));
        }

        for (int i = 0; i < encabezados.length; i++) {
            hoja.autoSizeColumn(i);
        }
    }

    public int obtenerSgteCodigo(List<Producto> productos) {
        if (productos == null || productos.isEmpty()) return 1;
        
        OptionalInt maxCodigo = productos.stream()
                                         .mapToInt(Producto::getCodigo) 
                                         .max();
        return maxCodigo.orElse(0) + 1;
    }
}