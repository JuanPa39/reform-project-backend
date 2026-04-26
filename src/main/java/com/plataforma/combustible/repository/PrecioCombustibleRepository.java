package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Combustible;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.PrecioCombustible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrecioCombustibleRepository extends JpaRepository<PrecioCombustible, Long> {
    
    List<PrecioCombustible> findByEstacionIdOrderByFechaDesc(Long estacionId);
    
    Optional<PrecioCombustible> findTopByEstacionIdAndCombustibleIdOrderByFechaDesc(Long estacionId, Long combustibleId);
    
    @Query("SELECT p FROM PrecioCombustible p WHERE p.estacion.id = :estacionId AND p.fecha BETWEEN :inicio AND :fin")
    List<PrecioCombustible> findByEstacionAndFechaBetween(@Param("estacionId") Long estacionId, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
    
    // ✅ AGREGAR ESTE MÉTODO
    boolean existsByEstacionIdAndCombustibleIdAndFecha(Long estacionId, Long combustibleId, LocalDate fecha);

    Optional<PrecioCombustible> findTopByEstacionAndCombustibleOrderByFechaDesc(
            Estacion estacion, Combustible combustible);
}