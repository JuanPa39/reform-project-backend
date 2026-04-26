package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.AsignarEstacionRequest;
import com.plataforma.combustible.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PutMapping("/usuarios/{usuarioId}/estacion")
    public ResponseEntity<Void> asignarEstacion(@PathVariable Long usuarioId,
                                                @RequestBody AsignarEstacionRequest request) {
        usuarioService.asignarEstacion(usuarioId, request.getEstacionId());
        return ResponseEntity.ok().build();
    }
}