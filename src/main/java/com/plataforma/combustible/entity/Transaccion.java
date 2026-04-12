package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@Data
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "estacion_id")
    private Estacion estacion;
    
    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;
    
    @ManyToOne
    @JoinColumn(name = "combustible_id")
    private Combustible combustible;
    
    private Double cantidad;
    
    private Double precioUnitario;
    
    private Double precioTotal;
    
    private LocalDateTime fecha;
    
    private String facturaUrl;
}