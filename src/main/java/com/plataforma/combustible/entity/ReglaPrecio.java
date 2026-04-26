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
    @JoinColumn(name = "normativa_id")
    private Normativa normativa;
    
    @ManyToOne
    @JoinColumn(name = "combustible_id")
    private Combustible combustible;
    
    @ManyToOne
    @JoinColumn(name = "tipo_vehiculo_id")
    private TipoVehiculo tipoVehiculo;
    
    private Double factorAjuste;
    
    // ✅ AGREGAR ESTE MÉTODO (getter para porcentajeAjuste)
    public Double getPorcentajeAjuste() {
        return factorAjuste;  // factorAjuste es el porcentaje de ajuste
    }
    
    private Double precioBase;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activa;
}