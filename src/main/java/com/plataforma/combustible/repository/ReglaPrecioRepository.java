package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.ReglaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReglaPrecioRepository extends JpaRepository<ReglaPrecio, Long> {
    
    // ============================================
    // MÉTODOS POR COMBUSTIBLE (a través de la relación)
    // ============================================
    
    /**
     * Busca reglas por normativa ID y combustible ID
     */
    @Query("SELECT r FROM ReglaPrecio r WHERE r.normativa.id = :normativaId AND r.combustible.id = :combustibleId")
    List<ReglaPrecio> findByNormativaIdAndCombustibleId(@Param("normativaId") Long normativaId, 
                                                         @Param("combustibleId") Long combustibleId);
    
    /**
     * Busca reglas activas por combustible ID
     */
    @Query("SELECT r FROM ReglaPrecio r WHERE r.combustible.id = :combustibleId AND r.activa = true")
    List<ReglaPrecio> findActivasByCombustibleId(@Param("combustibleId") Long combustibleId);
    
    // ============================================
    // MÉTODOS POR TIPO DE VEHÍCULO
    // ============================================
    
    /**
     * Busca reglas por normativa, combustible y tipo de vehículo
     */
    @Query("SELECT r FROM ReglaPrecio r WHERE r.normativa.id = :normativaId AND r.combustible.id = :combustibleId AND r.tipoVehiculo.id = :tipoVehiculoId")
    Optional<ReglaPrecio> findByNormativaIdAndCombustibleIdAndTipoVehiculoId(@Param("normativaId") Long normativaId,
                                                                              @Param("combustibleId") Long combustibleId,
                                                                              @Param("tipoVehiculoId") Long tipoVehiculoId);
    
    /**
     * Busca reglas activas por tipo de vehículo
     */
    List<ReglaPrecio> findByTipoVehiculoIdAndActivaTrue(Long tipoVehiculoId);
    
    // ============================================
    // MÉTODOS POR NORMATIVA
    // ============================================
    
    /**
     * Busca reglas por normativa ID
     */
    List<ReglaPrecio> findByNormativaId(Long normativaId);
    
    /**
     * Busca reglas activas por normativa ID
     */
    List<ReglaPrecio> findByNormativaIdAndActivaTrue(Long normativaId);
    
    // ============================================
    // MÉTODOS POR FECHA
    // ============================================
    
    /**
     * Busca reglas vigentes en una fecha específica
     */
    @Query("SELECT r FROM ReglaPrecio r WHERE r.fechaInicio <= :fecha AND (r.fechaFin IS NULL OR r.fechaFin >= :fecha) AND r.activa = true")
    List<ReglaPrecio> findVigentesEnFecha(@Param("fecha") LocalDate fecha);
    
    /**
     * Busca reglas vigentes para un combustible específico
     */
    @Query("SELECT r FROM ReglaPrecio r WHERE r.combustible.id = :combustibleId AND r.fechaInicio <= :fecha AND (r.fechaFin IS NULL OR r.fechaFin >= :fecha) AND r.activa = true")
    List<ReglaPrecio> findVigentesByCombustibleIdEnFecha(@Param("combustibleId") Long combustibleId, 
                                                          @Param("fecha") LocalDate fecha);
    
    // ============================================
    // MÉTODOS POR NOMBRE
    // ============================================
    
    /**
     * Busca regla por nombre
     */
    Optional<ReglaPrecio> findByNombre(String nombre);
    
    /**
     * Busca reglas por nombre que contengan texto
     */
    List<ReglaPrecio> findByNombreContainingIgnoreCase(String nombre);
    
    // ============================================
    // MÉTODOS DE AUDITORÍA
    // ============================================
    
    /**
     * Cuenta reglas activas por combustible
     */
    long countByCombustibleIdAndActivaTrue(Long combustibleId);
    
    /**
     * Busca reglas con ajuste mayor a cierto porcentaje
     */
    List<ReglaPrecio> findByFactorAjusteGreaterThan(Double factorAjuste);
}