package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.ActualizarUsuarioRequest; 
import com.plataforma.combustible.dto.response.UsuarioResponse;
import com.plataforma.combustible.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Listar todos o filtrar por rol: GET /api/usuarios?rol=EMPLEADO
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULADOR')")
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(required = false) String rol) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(rol));
    }

    // Ver perfil propio: GET /api/usuarios/perfil
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> miPerfil(Authentication auth) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(auth));
    }

    // Ver cualquier usuario por ID (admin/regulador): GET /api/usuarios/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULADOR')")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // El usuario actualiza su propio perfil: PUT /api/usuarios/perfil
    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(
            @Valid @RequestBody ActualizarUsuarioRequest request,
            Authentication auth) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(request, auth));
    }

    // Admin/regulador actualiza cualquier usuario: PUT /api/usuarios/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGULADOR')")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }
}