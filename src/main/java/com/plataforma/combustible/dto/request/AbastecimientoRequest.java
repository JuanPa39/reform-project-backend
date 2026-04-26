package com.plataforma.combustible.dto.request;

public class AbastecimientoRequest {
    private Long distribuidorId;
    private Long estacionId;
    private Long combustibleId;
    private Double cantidadGalones;

    // Getters y Setters
    public Long getDistribuidorId() { return distribuidorId; }
    public void setDistribuidorId(Long distribuidorId) { this.distribuidorId = distribuidorId; }
    
    public Long getEstacionId() { return estacionId; }
    public void setEstacionId(Long estacionId) { this.estacionId = estacionId; }
    
    public Long getCombustibleId() { return combustibleId; }
    public void setCombustibleId(Long combustibleId) { this.combustibleId = combustibleId; }
    
    public Double getCantidadGalones() { return cantidadGalones; }
    public void setCantidadGalones(Double cantidadGalones) { this.cantidadGalones = cantidadGalones; }
}