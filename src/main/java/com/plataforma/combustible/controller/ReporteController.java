package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.response.ReporteZonaResponse;
import com.plataforma.combustible.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/consumo-por-zona")
    @PreAuthorize("hasRole('ADMIN') or hasRole('Entidad reguladora') or hasRole('Empleado de estación')")
    public ResponseEntity<List<ReporteZonaResponse>> getConsumoPorZona(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {
        
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        
        List<ReporteZonaResponse> reportes = reporteService.getReporteConsumoPorZona(inicio, fin);
        return ResponseEntity.ok(reportes);
    }
}