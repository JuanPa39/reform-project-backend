package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    
    List<Notificacion> findAllByOrderByFechaDesc();
    
    List<Notificacion> findByEstacionNombreOrderByFechaDesc(String estacionNombre);
    
    @Query("SELECT n FROM Notificacion n WHERE n.estacionNombre = :estacionNombre OR n.usuario.id = :usuarioId ORDER BY n.fecha DESC")
    List<Notificacion> findByEstacionNombreOrUsuarioIdOrderByFechaDesc(
        @Param("estacionNombre") String estacionNombre, 
        @Param("usuarioId") Long usuarioId);
}