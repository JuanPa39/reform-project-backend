package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "subsidios")
@Data
public class Subsidio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String codigo;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "tipo_vehiculo_id")
    private TipoVehiculo tipoVehiculo;
    
    private Double porcentajeDescuento;
    
    private Double montoMaximo;
    
    private LocalDate fechaInicio;
    
    private LocalDate fechaExpiracion;
    
    private boolean activo;
    
    private String descripcion;
}