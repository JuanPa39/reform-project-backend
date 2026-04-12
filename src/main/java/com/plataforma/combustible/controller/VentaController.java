package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.VentaRequest;
import com.plataforma.combustible.dto.response.VentaResponse;
import com.plataforma.combustible.service.VentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private static final Logger log = LoggerFactory.getLogger(VentaController.class);
    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<?> registrarVenta(@RequestBody VentaRequest request) {
        log.info("Petición POST /api/ventas - Combustible: {}, Cantidad: {} galones", 
            request.getTipoCombustible(), request.getCantidad());
        
        try {
            VentaResponse response = ventaService.registrarVenta(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error al registrar venta: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // NUEVO ENDPOINT: Historial de ventas
    @GetMapping("/historial")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VentaResponse>> getHistorialVentas() {
        try {
            List<VentaResponse> historial = ventaService.getHistorialVentas();
            return ResponseEntity.ok(historial);
        } catch (RuntimeException e) {
            log.error("Error al obtener historial: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // NUEVO ENDPOINT: Reporte mensual
    @GetMapping("/reporte-mensual")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<Double> getReporteMensual() {
        try {
            double total = ventaService.getReporteMensual();
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            log.error("Error al obtener reporte: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}