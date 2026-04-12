package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Combustible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CombustibleRepository extends JpaRepository<Combustible, Long> {
    
    Optional<Combustible> findByNombre(String nombre);
    List<Combustible> findByActivoTrue();
    
}