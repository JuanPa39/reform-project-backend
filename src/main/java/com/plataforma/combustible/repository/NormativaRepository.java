package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Normativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NormativaRepository extends JpaRepository<Normativa, Long> {
    
    // ✅ AGREGAR ESTE MÉTODO
    List<Normativa> findByActivaTrue();
    
    List<Normativa> findByActivaTrueOrderByFechaInicioDesc();
    
    Optional<Normativa> findByNombre(String nombre);
}