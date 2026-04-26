package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "abastecimiento")
public class Abastecimiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "distribuidor_id", nullable = false)
    private Distribuidor distribuidor;
    
    @ManyToOne
    @JoinColumn(name = "estacion_id", nullable = false)
    private Estacion estacion;
    
    @ManyToOne
    @JoinColumn(name = "combustible_id", nullable = false)
    private Combustible combustible;
    
    @Column(name = "cantidad_galones", nullable = false)
    private Double cantidadGalones;
    
    @Column(name = "fecha")
    private LocalDateTime fecha;
    
    @Column(name = "estado")
    private String estado; // SOLICITADO, EN_PROCESO, COMPLETADO, RECHAZADO
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Distribuidor getDistribuidor() { return distribuidor; }
    public void setDistribuidor(Distribuidor distribuidor) { this.distribuidor = distribuidor; }
    
    public Estacion getEstacion() { return estacion; }
    public void setEstacion(Estacion estacion) { this.estacion = estacion; }
    
    public Combustible getCombustible() { return combustible; }
    public void setCombustible(Combustible combustible) { this.combustible = combustible; }
    
    public Double getCantidadGalones() { return cantidadGalones; }
    public void setCantidadGalones(Double cantidadGalones) { this.cantidadGalones = cantidadGalones; }
    
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}