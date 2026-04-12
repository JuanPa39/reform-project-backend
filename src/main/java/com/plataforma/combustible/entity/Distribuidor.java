package com.plataforma.combustible.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "distribuidor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Distribuidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "zona_operacion", length = 100)
    private String zonaOperacion;
}