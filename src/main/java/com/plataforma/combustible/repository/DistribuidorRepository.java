package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Distribuidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DistribuidorRepository extends JpaRepository<Distribuidor, Long> {
    Optional<Distribuidor> findByNombre(String nombre);
    List<Distribuidor> findByZonaOperacion(String zonaOperacion);
    List<Distribuidor> findByActivoTrue();
}