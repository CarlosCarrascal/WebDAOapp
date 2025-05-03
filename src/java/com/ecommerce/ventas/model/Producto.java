package com.ecommerce.ventas.model;

import java.math.BigDecimal;

public class Producto {
    private int codigo;
    private String nombre;
    private BigDecimal precio;
    private int stock;

    // Constructor vacío
    public Producto() {
    }

    public Producto(String nombre, BigDecimal precio, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }
    
    // Constructor con código
    public Producto(int codigo, String nombre, BigDecimal precio, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    // Getters y Setters
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

    @Override
    public String toString() {
        return "Producto{" +
               "codigo=" + codigo +
               ", nombre='" + nombre + '\'' +
               ", precio=" + precio +
               ", stock=" + stock +
               '}';
    }
}
