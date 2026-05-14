package com.plataforma.combustible.repository;

import java.time.LocalDateTime;
import com.plataforma.combustible.entity.Abastecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AbastecimientoRepository extends JpaRepository<Abastecimiento, Long> {
    
    List<Abastecimiento> findByEstacionId(Long estacionId);
    List<Abastecimiento> findByDistribuidorId(Long distribuidorId);
    List<Abastecimiento> findByEstado(String estado);
    List<Abastecimiento> findByEstacionIdOrderByFechaDesc(Long estacionId);
    
    // ✅ VERSIÓN CON NATIVE QUERY - MÁS CONFIABLE
    @Query(value = "SELECT * FROM abastecimiento a WHERE a.estacion_id = :estacionId " +
           "AND a.estado = 'COMPLETADO' " +
           "AND (CAST(:fechaInicio AS TIMESTAMP) IS NULL OR a.fecha >= CAST(:fechaInicio AS TIMESTAMP)) " +
           "AND (CAST(:fechaFin AS TIMESTAMP) IS NULL OR a.fecha <= CAST(:fechaFin AS TIMESTAMP)) " +
           "AND (CAST(:combustibleId AS BIGINT) IS NULL OR a.combustible_id = CAST(:combustibleId AS BIGINT)) " +
           "ORDER BY a.fecha DESC", 
           nativeQuery = true)
    List<Abastecimiento> findHistorialRecargas(
        @Param("estacionId") Long estacionId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        @Param("combustibleId") Long combustibleId
    );
}