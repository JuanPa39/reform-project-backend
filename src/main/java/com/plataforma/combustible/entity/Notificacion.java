package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "estacion_nombre")
    private String estacionNombre;
    
    @Column(nullable = false)
    private String inconsistencia;
    
    private String estado;
    
    private LocalDateTime fecha;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnore 
    private Usuario usuario;
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEstacionNombre() { return estacionNombre; }
    public void setEstacionNombre(String estacionNombre) { this.estacionNombre = estacionNombre; }
    
    public String getInconsistencia() { return inconsistencia; }
    public void setInconsistencia(String inconsistencia) { this.inconsistencia = inconsistencia; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}