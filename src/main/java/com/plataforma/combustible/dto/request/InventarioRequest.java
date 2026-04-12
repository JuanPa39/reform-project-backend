package com.plataforma.combustible.dto.request;

public class InventarioRequest {
    private String tipoCombustible;
    private Integer cantidad;

    // Constructor por defecto
    public InventarioRequest() {}

    // Constructor con parámetros
    public InventarioRequest(String tipoCombustible, Integer cantidad) {
        this.tipoCombustible = tipoCombustible;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}