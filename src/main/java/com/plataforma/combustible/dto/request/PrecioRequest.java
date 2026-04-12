package com.plataforma.combustible.dto.request;

public class PrecioRequest {
    private Long estacionId;
    private String tipoCombustible;
    private Double precio;
    
    // Getters y Setters
    public Long getEstacionId() { return estacionId; }
    public void setEstacionId(Long estacionId) { this.estacionId = estacionId; }
    
    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }
    
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
}