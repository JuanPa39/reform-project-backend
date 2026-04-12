package com.plataforma.combustible.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_vehiculo") 
public class TipoVehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;
    
    @Column(nullable = false, unique = true)
    private String nombre;
    
    private String descripcion;
    
    private boolean activo;
    
    private Integer ordenDisplay;

    // Getters y Setters
    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public Integer getOrdenDisplay() { return ordenDisplay; }
    public void setOrdenDisplay(Integer ordenDisplay) { this.ordenDisplay = ordenDisplay; }
    
    public boolean esTodos() {
        return "todos".equalsIgnoreCase(nombre);
    }
}