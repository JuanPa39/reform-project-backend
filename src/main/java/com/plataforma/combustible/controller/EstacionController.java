package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.EstacionRequest;
import com.plataforma.combustible.dto.response.EstacionResponse;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.repository.EstacionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionController {
    private final EstacionRepository estacionRepository;

    public EstacionController(EstacionRepository estacionRepository) {
        this.estacionRepository = estacionRepository;
    }

    @GetMapping
    public ResponseEntity<List<EstacionResponse>> getAllEstaciones() {
        List<EstacionResponse> estaciones = estacionRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(estaciones);
    }

    @PostMapping
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<EstacionResponse> createEstacion(@Valid @RequestBody EstacionRequest request) {
        if (estacionRepository.existsByNit(request.getNit())) {
            return ResponseEntity.badRequest().build();
        }
        
        Estacion estacion = new Estacion();
        estacion.setNombre(request.getNombre());
        estacion.setNit(request.getNit());
        estacion.setUbicacion(request.getUbicacion());
        estacion.setActiva(true);
        
        Estacion saved = estacionRepository.save(estacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<EstacionResponse> updateEstacion(@PathVariable Long id, @Valid @RequestBody EstacionRequest request) {
        Estacion estacion = estacionRepository.findById(id).orElse(null);
        if (estacion == null) {
            return ResponseEntity.notFound().build();
        }
        
        estacion.setNombre(request.getNombre());
        estacion.setNit(request.getNit());
        estacion.setUbicacion(request.getUbicacion());
        
        Estacion updated = estacionRepository.save(estacion);
        return ResponseEntity.ok(convertToResponse(updated));
    }

    private EstacionResponse convertToResponse(Estacion estacion) {
        EstacionResponse response = new EstacionResponse();
        response.setId(estacion.getId());
        response.setNombre(estacion.getNombre());
        response.setNit(estacion.getNit());
        response.setUbicacion(estacion.getUbicacion());
        response.setActiva(estacion.isActiva());
        return response;
    }
}