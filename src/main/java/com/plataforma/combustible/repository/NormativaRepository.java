package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Normativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NormativaRepository extends JpaRepository<Normativa, Long> {
    
    List<Normativa> findByActivaTrueOrderByFechaInicioDesc();
    
    List<Normativa> findAllByOrderByFechaInicioDesc();
}