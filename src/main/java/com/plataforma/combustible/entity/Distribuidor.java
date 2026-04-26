package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "distribuidor")
public class Distribuidor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "zona_operacion")
    private String zonaOperacion;
    
    private String direccion;
    
    private String telefono;
    
    private String email;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    private boolean activo = true;
    
    // Constructor por defecto
    public Distribuidor() {}
    
    // Constructor con parámetros
    public Distribuidor(String nombre, String zonaOperacion) {
        this.nombre = nombre;
        this.zonaOperacion = zonaOperacion;
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getZonaOperacion() { return zonaOperacion; }
    public void setZonaOperacion(String zonaOperacion) { this.zonaOperacion = zonaOperacion; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}