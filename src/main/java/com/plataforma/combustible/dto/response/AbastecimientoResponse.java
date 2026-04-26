package com.plataforma.combustible.dto.response;

import java.time.LocalDateTime;

public class AbastecimientoResponse {
    private Long id;
    private String distribuidorNombre;
    private String estacionNombre;
    private String combustibleNombre;
    private Double cantidadGalones;
    private LocalDateTime fecha;
    private String estado;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDistribuidorNombre() { return distribuidorNombre; }
    public void setDistribuidorNombre(String distribuidorNombre) { this.distribuidorNombre = distribuidorNombre; }
    
    public String getEstacionNombre() { return estacionNombre; }
    public void setEstacionNombre(String estacionNombre) { this.estacionNombre = estacionNombre; }
    
    public String getCombustibleNombre() { return combustibleNombre; }
    public void setCombustibleNombre(String combustibleNombre) { this.combustibleNombre = combustibleNombre; }
    
    public Double getCantidadGalones() { return cantidadGalones; }
    public void setCantidadGalones(Double cantidadGalones) { this.cantidadGalones = cantidadGalones; }
    
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}