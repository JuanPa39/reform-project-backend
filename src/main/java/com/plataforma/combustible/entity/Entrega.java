package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrega")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribuidor_id", nullable = false)
    private Distribuidor distribuidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacion_id", nullable = false)
    private Estacion estacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combustible_id", nullable = false)
    private Combustible combustible;

    @Column(name = "cantidad_entregada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadEntregada;

    @CreationTimestamp
    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
}