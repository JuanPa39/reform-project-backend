package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "parametros_sistema")
@Data
public class ParametroSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String clave;
    
    private String valor;
    
    private String descripcion;
    
    private boolean editable;
}