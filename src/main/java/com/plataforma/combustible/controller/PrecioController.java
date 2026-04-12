package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.PrecioRequest;
import com.plataforma.combustible.dto.response.PrecioResponse;
import com.plataforma.combustible.entity.Combustible;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.PrecioCombustible;
import com.plataforma.combustible.repository.CombustibleRepository;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.PrecioCombustibleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/precios")
public class PrecioController {
    private final PrecioCombustibleRepository precioRepository;
    private final EstacionRepository estacionRepository;
    private final CombustibleRepository combustibleRepository;

    public PrecioController(PrecioCombustibleRepository precioRepository, 
                            EstacionRepository estacionRepository,
                            CombustibleRepository combustibleRepository) {
        this.precioRepository = precioRepository;
        this.estacionRepository = estacionRepository;
        this.combustibleRepository = combustibleRepository;
    }

    @GetMapping
    public ResponseEntity<List<PrecioResponse>> getAllPrecios() {
        List<PrecioResponse> precios = precioRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(precios);
    }

    @PostMapping
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<Void> createPrecio(@RequestBody PrecioRequest request) {
        Estacion estacion = estacionRepository.findById(request.getEstacionId()).orElse(null);
        if (estacion == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Buscar el combustible por nombre
        Combustible combustible = combustibleRepository.findByNombre(request.getTipoCombustible()).orElse(null);
        if (combustible == null) {
            return ResponseEntity.badRequest().build();
        }
        
        PrecioCombustible precio = new PrecioCombustible();
        precio.setEstacion(estacion);
        precio.setCombustible(combustible);
        precio.setPrecio(request.getPrecio());
        precio.setFecha(LocalDate.now());
        
        precioRepository.save(precio);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private PrecioResponse convertToResponse(PrecioCombustible precio) {
        PrecioResponse response = new PrecioResponse();
        response.setId(precio.getId());
        response.setEstacionNombre(precio.getEstacion().getNombre());
        response.setCombustibleNombre(precio.getCombustible().getNombre());
        response.setPrecio(precio.getPrecio());
        response.setFecha(precio.getFecha().toString());
        return response;
    }
}