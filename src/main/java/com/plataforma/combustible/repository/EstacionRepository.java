package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Estacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstacionRepository extends JpaRepository<Estacion, Long> {
    Optional<Estacion> findByUsuarioId(Long usuarioId);
    Optional<Estacion> findByNit(String nit);
    List<Estacion> findByNombreContainingIgnoreCase(String nombre);
    boolean existsByNit(String nit);
    
}