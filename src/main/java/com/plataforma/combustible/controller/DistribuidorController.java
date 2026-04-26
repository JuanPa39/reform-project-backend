package com.plataforma.combustible.controller;

import com.plataforma.combustible.entity.Distribuidor;
import com.plataforma.combustible.repository.DistribuidorRepository;
import com.plataforma.combustible.service.AuditoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/distribuidores")
public class DistribuidorController {

    private final DistribuidorRepository distribuidorRepository;
    private final AuditoriaService auditoriaService;

    public DistribuidorController(DistribuidorRepository distribuidorRepository,
                                  AuditoriaService auditoriaService) {
        this.distribuidorRepository = distribuidorRepository;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<Distribuidor>> getAll() {
        return ResponseEntity.ok(distribuidorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Distribuidor> getById(@PathVariable Long id) {
        return distribuidorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Distribuidor> create(@RequestBody Distribuidor distribuidor) {
        distribuidor.setFechaRegistro(LocalDateTime.now());
        Distribuidor guardado = distribuidorRepository.save(distribuidor);
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrar(
            email,
            "CREACION_DISTRIBUIDOR",
            String.format("Nuevo distribuidor: %s", distribuidor.getNombre()),
            "Distribuidor",
            guardado.getId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Distribuidor> update(@PathVariable Long id, @RequestBody Distribuidor distribuidor) {
        if (!distribuidorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        distribuidor.setId(id);
        Distribuidor actualizado = distribuidorRepository.save(distribuidor);
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrar(
            email,
            "EDICION_DISTRIBUIDOR",
            String.format("Distribuidor actualizado: %s", distribuidor.getNombre()),
            "Distribuidor",
            actualizado.getId()
        );
        
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!distribuidorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        distribuidorRepository.deleteById(id);
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrar(
            email,
            "ELIMINACION_DISTRIBUIDOR",
            "Distribuidor eliminado ID: " + id,
            "Distribuidor",
            id
        );
        
        return ResponseEntity.noContent().build();
    }
}