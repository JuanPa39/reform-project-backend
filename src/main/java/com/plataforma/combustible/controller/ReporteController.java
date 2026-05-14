package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.response.ReporteZonaResponse;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final UsuarioRepository usuarioRepository;

    public ReporteController(ReporteService reporteService, UsuarioRepository usuarioRepository) {
        this.reporteService = reporteService;
        this.usuarioRepository = usuarioRepository;
    }

    // ============================================
    // NUEVO ENDPOINT: Consumo por zona
    // ============================================
    @GetMapping("/consumo-por-zona")
    @PreAuthorize("hasRole('ADMIN') or hasRole('Empleado de estación') or hasRole('Entidad reguladora')")
    public ResponseEntity<List<ReporteZonaResponse>> getConsumoPorZona(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        
        LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusDays(30);
        LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();
        
        List<ReporteZonaResponse> reportes = reporteService.generarReporteConsumoPorZona(inicio, fin);
        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/ventas/pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('Empleado de estación')")
    public ResponseEntity<byte[]> exportarVentasPDF(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        Long estacionId = usuario.getEstacion() != null ? usuario.getEstacion().getId() : null;
        
        LocalDateTime inicio = fechaInicio != null ? LocalDateTime.parse(fechaInicio + "T00:00:00") : LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime fin = fechaFin != null ? LocalDateTime.parse(fechaFin + "T23:59:59") : LocalDateTime.now();
        
        byte[] pdf = reporteService.generarReporteVentasPDF(estacionId, inicio, fin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte_ventas.pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/ventas/excel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('Empleado de estación')")
    public ResponseEntity<byte[]> exportarVentasExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        Long estacionId = usuario.getEstacion() != null ? usuario.getEstacion().getId() : null;
        
        LocalDateTime inicio = fechaInicio != null ? LocalDateTime.parse(fechaInicio + "T00:00:00") : LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime fin = fechaFin != null ? LocalDateTime.parse(fechaFin + "T23:59:59") : LocalDateTime.now();
        
        byte[] excel = reporteService.generarReporteVentasExcel(estacionId, inicio, fin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "reporte_ventas.xlsx");
        
        return ResponseEntity.ok().headers(headers).body(excel);
    }
}