package com.plataforma.combustible.dto.response;

import java.time.LocalDateTime;

public class InventarioResponse {
    private Long id;
    private String estacionNombre;
    private String combustibleNombre;
    private Double cantidadDisponible;
    private LocalDateTime fechaActualizacion;

    // Constructor por defecto
    public InventarioResponse() {}

    // Constructor con parámetros
    public InventarioResponse(Long id, String estacionNombre, String combustibleNombre, 
                              Double cantidadDisponible, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.estacionNombre = estacionNombre;
        this.combustibleNombre = combustibleNombre;
        this.cantidadDisponible = cantidadDisponible;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstacionNombre() { return estacionNombre; }
    public void setEstacionNombre(String estacionNombre) { this.estacionNombre = estacionNombre; }

    public String getCombustibleNombre() { return combustibleNombre; }
    public void setCombustibleNombre(String combustibleNombre) { this.combustibleNombre = combustibleNombre; }

    public Double getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(Double cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}