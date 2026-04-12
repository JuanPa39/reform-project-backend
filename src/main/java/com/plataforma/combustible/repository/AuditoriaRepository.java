package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByUsuarioEmailOrderByFechaDesc(String usuarioEmail);
    @Query("SELECT a FROM Auditoria a WHERE a.fecha BETWEEN :inicio AND :fin")
    List<Auditoria> findByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}