package com.plataforma.combustible.controller;

import com.plataforma.combustible.entity.Auditoria;
import com.plataforma.combustible.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('Entidad reguladora')")
    public ResponseEntity<List<Auditoria>> obtenerTodas() {
        return ResponseEntity.ok(auditoriaService.obtenerTodas());
    }
}