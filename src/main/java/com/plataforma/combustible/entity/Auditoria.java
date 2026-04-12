package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String usuarioEmail;
    
    private String accion;
    
    private String entidad;
    
    private Long idEntidad;
    
    private String detalles;
    
    private LocalDateTime fecha;
    
    private String ipAddress;
}