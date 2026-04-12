package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.Subsidio;
import com.plataforma.combustible.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubsidioRepository extends JpaRepository<Subsidio, Long> {
    List<Subsidio> findByUsuarioId(Long usuarioId);
    List<Subsidio> findByUsuarioAndActivoTrue(Usuario usuario);
    Optional<Subsidio> findByCodigo(String codigo);
    List<Subsidio> findByFechaExpiracionBeforeAndActivoTrue(LocalDate fecha);
}