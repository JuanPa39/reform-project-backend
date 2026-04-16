package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.EstacionRequest;
import com.plataforma.combustible.dto.response.EstacionResponse;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.service.AuditoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionController {
    
    private final EstacionRepository estacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public EstacionController(EstacionRepository estacionRepository,
                              UsuarioRepository usuarioRepository,
                              AuditoriaService auditoriaService) {
        this.estacionRepository = estacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
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
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        Estacion estacion = new Estacion();
        estacion.setNombre(request.getNombre());
        estacion.setNit(request.getNit());
        estacion.setUbicacion(request.getUbicacion());
        estacion.setActiva(true);
        estacion.setFechaRegistro(LocalDateTime.now());
        
        if (request.getLatitud() != null) {
            estacion.setLatitud(request.getLatitud());
        }
        if (request.getLongitud() != null) {
            estacion.setLongitud(request.getLongitud());
        }
        
        Estacion saved = estacionRepository.save(estacion);
        
        if (usuario != null) {
            auditoriaService.registrar(
                usuario.getEmail(),
                "CREACION_ESTACION",
                String.format("Nueva estación: %s (NIT: %s)", request.getNombre(), request.getNit()),
                "Estacion",
                saved.getId()
            );
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<EstacionResponse> updateEstacion(@PathVariable Long id, 
                                                            @Valid @RequestBody EstacionRequest request) {
        Estacion estacion = estacionRepository.findById(id).orElse(null);
        if (estacion == null) {
            return ResponseEntity.notFound().build();
        }
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        String nombreAntiguo = estacion.getNombre();
        
        estacion.setNombre(request.getNombre());
        estacion.setNit(request.getNit());
        estacion.setUbicacion(request.getUbicacion());
        
        if (request.getLatitud() != null) {
            estacion.setLatitud(request.getLatitud());
        }
        if (request.getLongitud() != null) {
            estacion.setLongitud(request.getLongitud());
        }
        
        Estacion updated = estacionRepository.save(estacion);
        
        if (usuario != null) {
            auditoriaService.registrar(
                usuario.getEmail(),
                "EDICION_ESTACION",
                String.format("Estación actualizada: %s → %s", nombreAntiguo, request.getNombre()),
                "Estacion",
                updated.getId()
            );
        }
        
        return ResponseEntity.ok(convertToResponse(updated));
    }

    private EstacionResponse convertToResponse(Estacion estacion) {
        EstacionResponse response = new EstacionResponse();
        response.setId(estacion.getId());
        response.setNombre(estacion.getNombre());
        response.setNit(estacion.getNit());
        response.setUbicacion(estacion.getUbicacion());
        response.setActiva(estacion.isActiva());
        response.setLatitud(estacion.getLatitud());
        response.setLongitud(estacion.getLongitud());
        return response;
    }
}