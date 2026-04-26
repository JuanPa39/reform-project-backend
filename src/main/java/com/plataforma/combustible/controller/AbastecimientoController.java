package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.AbastecimientoRequest;
import com.plataforma.combustible.dto.response.AbastecimientoResponse;
import com.plataforma.combustible.service.AbastecimientoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/abastecimientos")
public class AbastecimientoController {

    private final AbastecimientoService abastecimientoService;

    public AbastecimientoController(AbastecimientoService abastecimientoService) {
        this.abastecimientoService = abastecimientoService;
    }

    // Solicitar abastecimiento (Empleado de estación)
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<AbastecimientoResponse> solicitar(@RequestBody AbastecimientoRequest request) {
        try {
            AbastecimientoResponse response = abastecimientoService.solicitarAbastecimiento(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Aprobar abastecimiento (Distribuidor)
    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('Distribuidor') or hasRole('ADMIN')")
    public ResponseEntity<AbastecimientoResponse> aprobar(@PathVariable Long id) {
        try {
            AbastecimientoResponse response = abastecimientoService.aprobarAbastecimiento(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Completar abastecimiento (Empleado de estación)
    @PutMapping("/{id}/completar")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<AbastecimientoResponse> completar(@PathVariable Long id) {
        try {
            AbastecimientoResponse response = abastecimientoService.completarAbastecimiento(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Rechazar abastecimiento
    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('Distribuidor') or hasRole('ADMIN')")
    public ResponseEntity<AbastecimientoResponse> rechazar(@PathVariable Long id) {
        try {
            AbastecimientoResponse response = abastecimientoService.rechazarAbastecimiento(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener todas las solicitudes
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('Entidad reguladora')")
    public ResponseEntity<List<AbastecimientoResponse>> getAll() {
        return ResponseEntity.ok(abastecimientoService.getAll());
    }

    // Obtener por estación
    @GetMapping("/estacion/{estacionId}")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<List<AbastecimientoResponse>> getByEstacion(@PathVariable Long estacionId) {
        return ResponseEntity.ok(abastecimientoService.getByEstacion(estacionId));
    }

    // Obtener por distribuidor
    @GetMapping("/distribuidor/{distribuidorId}")
    @PreAuthorize("hasRole('Distribuidor') or hasRole('ADMIN')")
    public ResponseEntity<List<AbastecimientoResponse>> getByDistribuidor(@PathVariable Long distribuidorId) {
        return ResponseEntity.ok(abastecimientoService.getByDistribuidor(distribuidorId));
    }

    // Obtener por estado
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('Entidad reguladora')")
    public ResponseEntity<List<AbastecimientoResponse>> getByEstado(@PathVariable String estado) {
        return ResponseEntity.ok(abastecimientoService.getByEstado(estado));
    }
}