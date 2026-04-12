package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    
    Optional<Inventario> findByEstacionIdAndCombustibleId(Long estacionId, Long combustibleId);
    
    // ✅ NUEVO MÉTODO
    List<Inventario> findByEstacionId(Long estacionId);
    
    boolean existsByEstacionIdAndCombustibleId(Long estacionId, Long combustibleId);
}