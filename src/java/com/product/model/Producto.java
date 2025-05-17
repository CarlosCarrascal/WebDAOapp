package com.product.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List; 

public class Producto {
    private int codigo;
    private String nombre; 
    private BigDecimal precio;  
    private int stock;
    private List<String> storedIn; 

    public Producto() {
        this.storedIn = new ArrayList<>();
    }

    // Constructor para crear un nuevo producto sin un código predefinido.
    public Producto(String nombre, BigDecimal precio, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.storedIn = new ArrayList<>();
    }
    
    // Constructor para crear un producto con todos sus atributos
    public Producto(int codigo, String nombre, BigDecimal precio, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.storedIn = new ArrayList<>();
    }

    // --- Getters y Setters ---
    public int getCodigo(){
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public List<String> getStoredIn() {
        return storedIn;
    }

    public void setStoredIn(List<String> storedIn) {
        this.storedIn = storedIn;
    }

    // Ubicación de almacenamiento a la lista
    public void addStorageLocation(String location) {
        if (this.storedIn == null) {
            this.storedIn = new ArrayList<>();
        }
        if (location != null && !location.trim().isEmpty() && !this.storedIn.contains(location)) {
            this.storedIn.add(location);
        }
    }

    @Override
    public String toString() {
        return "Producto{" +
               "codigo=" + codigo +
               ", nombre='" + nombre + '\'' +
               ", precio=" + precio +
               ", stock=" + stock +
               ", storedIn=" + (storedIn != null ? String.join(",", storedIn) : "N/A") +
               '}';
    }
}