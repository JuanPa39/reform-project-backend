package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "reglas_precio")
@Data
public class ReglaPrecio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    
    private String descripcion;
    
    @ManyToOne
    @JoinColumn(name = "tipo_vehiculo_id")
    private TipoVehiculo tipoVehiculo;
    
    private Double factorAjuste;
    
    private Double precioBase;
    
    private LocalDate fechaInicio;
    
    private LocalDate fechaFin;
    
    private boolean activa;
}