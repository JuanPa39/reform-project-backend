package com.plataforma.combustible.repository;

import com.plataforma.combustible.entity.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, Short> {
    Optional<TipoVehiculo> findByNombre(String nombre);
    List<TipoVehiculo> findByActivoTrueOrderByOrdenDisplayAsc();
}