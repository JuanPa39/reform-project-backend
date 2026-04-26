package com.plataforma.combustible.dto.request;

public class VentaRequest {
    private String tipoCombustible;
    private double cantidad;
    private String tipoVehiculo;  // ← AGREGAR: Particular, Taxi, Oficial, etc.
    
    // Getters y Setters
    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }
    
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    
    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }
}