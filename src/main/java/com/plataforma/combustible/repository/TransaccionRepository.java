package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    List<Transaccion> findByEstacionIdOrderByFechaDesc(Long estacionId);
    @Query("SELECT SUM(t.precioTotal) FROM Transaccion t WHERE t.usuario.id = :usuarioId AND t.fecha BETWEEN :inicio AND :fin")
    Double sumTotalByUsuarioAndFechaBetween(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}