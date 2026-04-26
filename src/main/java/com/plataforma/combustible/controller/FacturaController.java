package com.plataforma.combustible.controller;

import com.plataforma.combustible.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @GetMapping(value = "/venta/{ventaId}", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN') or hasRole('REGULADOR')")
    public ResponseEntity<byte[]> generarFactura(@PathVariable Long ventaId) {
        byte[] pdfBytes = facturaService.generarFacturaPdf(ventaId);
        String filename = "factura_" + String.format("%08d", ventaId) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    @GetMapping(value = "/venta/{ventaId}/preview", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN') or hasRole('REGULADOR')")
    public ResponseEntity<byte[]> previsualizarFactura(@PathVariable Long ventaId) {
        byte[] pdfBytes = facturaService.generarFacturaPdf(ventaId);
        String filename = "factura_" + String.format("%08d", ventaId) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}