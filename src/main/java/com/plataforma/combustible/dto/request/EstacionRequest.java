package com.plataforma.combustible.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EstacionRequest {
    
    @NotBlank
    private String nombre;
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$", message = "NIT inválido")
    private String nit;
    
    private String ubicacion;
    
    private String zona;  // ← AGREGAR
    
    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
}