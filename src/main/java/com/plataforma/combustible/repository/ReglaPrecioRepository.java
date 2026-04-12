package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.ReglaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReglaPrecioRepository extends JpaRepository<ReglaPrecio, Long> {
    List<ReglaPrecio> findByActivaTrue();
    @Query("SELECT r FROM ReglaPrecio r WHERE r.tipoVehiculo.id = :tipoVehiculoId AND r.fechaInicio <= :fecha AND (r.fechaFin IS NULL OR r.fechaFin >= :fecha) AND r.activa = true")
    Optional<ReglaPrecio> findReglaAplicable(@Param("tipoVehiculoId") Long tipoVehiculoId, @Param("fecha") LocalDate fecha);
}