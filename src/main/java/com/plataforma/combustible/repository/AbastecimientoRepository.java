package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Abastecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AbastecimientoRepository extends JpaRepository<Abastecimiento, Long> {
    List<Abastecimiento> findByEstacionId(Long estacionId);
    List<Abastecimiento> findByDistribuidorId(Long distribuidorId);
    List<Abastecimiento> findByEstado(String estado);
}