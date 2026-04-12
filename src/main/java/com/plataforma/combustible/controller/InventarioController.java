package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.InventarioRequest;
import com.plataforma.combustible.dto.response.InventarioResponse;
import com.plataforma.combustible.dto.response.DisponibilidadResponse;
import com.plataforma.combustible.service.InventarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private static final Logger log = LoggerFactory.getLogger(InventarioController.class);
    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<InventarioResponse> registrarInventario(@RequestBody InventarioRequest request) {
        log.info("Petición POST /api/inventario - tipoCombustible={}, cantidad={}", 
            request.getTipoCombustible(), request.getCantidad());
        
        try {
            InventarioResponse response = inventarioService.registrarInventario(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error en registro de inventario: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ NUEVO ENDPOINT: Obtener disponibilidad de todos los combustibles
    @GetMapping("/disponibilidad")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<List<DisponibilidadResponse>> getDisponibilidad() {
        try {
            List<DisponibilidadResponse> disponibilidad = inventarioService.getDisponibilidad();
            return ResponseEntity.ok(disponibilidad);
        } catch (RuntimeException e) {
            log.error("Error al obtener disponibilidad: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}