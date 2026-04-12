package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "precios_combustible")  // ← Este ya está bien
public class PrecioCombustible {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "estacion_id", nullable = false)
    private Estacion estacion;
    
    @ManyToOne
    @JoinColumn(name = "combustible_id", nullable = false)
    private Combustible combustible;
    
    @Column(nullable = false)
    private Double precio;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    private boolean precioRegulado;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Estacion getEstacion() { return estacion; }
    public void setEstacion(Estacion estacion) { this.estacion = estacion; }
    
    public Combustible getCombustible() { return combustible; }
    public void setCombustible(Combustible combustible) { this.combustible = combustible; }
    
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public boolean isPrecioRegulado() { return precioRegulado; }
    public void setPrecioRegulado(boolean precioRegulado) { this.precioRegulado = precioRegulado; }
}