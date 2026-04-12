package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
    List<Venta> findByEstacionIdOrderByFechaVentaDesc(Long estacionId);
    
    List<Venta> findByUsuarioIdOrderByFechaVentaDesc(Long usuarioId);
    
    @Query("SELECT v FROM Venta v WHERE v.estacion.id = :estacionId AND v.fechaVenta >= :fechaInicio")
    List<Venta> findVentasDelMes(@Param("estacionId") Long estacionId, @Param("fechaInicio") LocalDateTime fechaInicio);
    
    @Query("SELECT COALESCE(SUM(v.montoTotal), 0) FROM Venta v WHERE v.estacion.id = :estacionId AND v.fechaVenta >= :fechaInicio")
    Double sumMontoTotalDelMes(@Param("estacionId") Long estacionId, @Param("fechaInicio") LocalDateTime fechaInicio);
}