package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "precios_combustible")
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
    
    private Double precio;
    
    @Column(name = "precio_original")
    private Double precioOriginal;
    
    @Column(name = "normativa_aplicada")
    private String normativaAplicada;
    
    private LocalDate fecha;
    
    @Column(name = "precio_regulado")
    private Boolean precioRegulado = true;
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Estacion getEstacion() { return estacion; }
    public void setEstacion(Estacion estacion) { this.estacion = estacion; }
    
    public Combustible getCombustible() { return combustible; }
    public void setCombustible(Combustible combustible) { this.combustible = combustible; }
    
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    
    public Double getPrecioOriginal() { return precioOriginal; }
    public void setPrecioOriginal(Double precioOriginal) { this.precioOriginal = precioOriginal; }
    
    public String getNormativaAplicada() { return normativaAplicada; }
    public void setNormativaAplicada(String normativaAplicada) { this.normativaAplicada = normativaAplicada; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public Boolean getPrecioRegulado() { return precioRegulado; }
    public void setPrecioRegulado(Boolean precioRegulado) { this.precioRegulado = precioRegulado; }
}