package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.ParametroSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ParametroSistemaRepository extends JpaRepository<ParametroSistema, Long> {
    
    // ✅ AGREGAR ESTE MÉTODO
    Optional<String> findValorByClave(String clave);
    
    // Ya existente
    Optional<ParametroSistema> findByClave(String clave);
}