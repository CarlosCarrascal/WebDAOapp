package com.product.util; 

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExcelManager {

    private static final Logger LOGGER = Logger.getLogger(ExcelManager.class.getName());

    public static Workbook obtenerLibro(String rutaArchivo) throws IOException {
        File archivo = new File(rutaArchivo);
        if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
            archivo.getParentFile().mkdirs(); 
        }
        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                return new XSSFWorkbook(fis);
            }
        } else {
            return new XSSFWorkbook();
        }
    }

    public static void guardarCerrarLibro(Workbook libro, String rutaArchivo) throws IOException {
        if (libro == null) {
            return; 
        }
        File archivo = new File(rutaArchivo);
        File dirPadre = archivo.getParentFile();
        if (dirPadre != null && !dirPadre.exists()) {
            dirPadre.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(rutaArchivo)) {
            libro.write(fos); 
        } finally {
            try {
                libro.close(); 
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error al cerrar libro tras guardar: " + rutaArchivo, e);
            }
        }
    }

    public static Sheet obtenerHoja(Workbook libro, String nombreHoja, boolean crear) {
        if (libro == null) return null; 
        Sheet hoja = libro.getSheet(nombreHoja);
        if (hoja == null && crear) {
            hoja = libro.createSheet(nombreHoja); 
        }
        return hoja;
    }

    public static Sheet asegurarHojaEncabezado(Workbook libro, String nombreHoja, String[] encabezados) {
        Sheet hoja = obtenerHoja(libro, nombreHoja, true); 
        
        if (hoja.getLastRowNum() < 0 || hoja.getRow(0) == null) { 
            Row filaEncabezado = hoja.createRow(0); 
            for (int i = 0; i < encabezados.length; i++) {
                filaEncabezado.createCell(i).setCellValue(encabezados[i]); 
            }
        }
        return hoja;
    }
}
