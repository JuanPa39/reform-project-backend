package com.plataforma.combustible.dto.response;

public class DisponibilidadResponse {
    private double cantidadDisponible;
    private boolean aplicaSubsidio;
    private String mensajeSubsidio;
    private String combustibleNombre;
    private boolean nivelBajo;  // ← AGREGAR

    // Getters y Setters
    public double getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(double cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    
    public boolean isAplicaSubsidio() { return aplicaSubsidio; }
    public void setAplicaSubsidio(boolean aplicaSubsidio) { this.aplicaSubsidio = aplicaSubsidio; }
    
    public String getMensajeSubsidio() { return mensajeSubsidio; }
    public void setMensajeSubsidio(String mensajeSubsidio) { this.mensajeSubsidio = mensajeSubsidio; }
    
    public String getCombustibleNombre() { return combustibleNombre; }
    public void setCombustibleNombre(String combustibleNombre) { this.combustibleNombre = combustibleNombre; }
    
    public boolean isNivelBajo() { return nivelBajo; }  // ← AGREGAR
    public void setNivelBajo(boolean nivelBajo) { this.nivelBajo = nivelBajo; }  // ← AGREGAR
}