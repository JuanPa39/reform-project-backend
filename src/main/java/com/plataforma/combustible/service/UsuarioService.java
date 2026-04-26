package com.plataforma.combustible.service;

import com.plataforma.combustible.dto.request.ActualizarUsuarioRequest;  
import com.plataforma.combustible.dto.response.UsuarioResponse;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EstacionRepository estacionRepository;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EstacionRepository estacionRepository,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.estacionRepository = estacionRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void asignarEstacion(Long usuarioId, Long estacionId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Estacion estacion = estacionRepository.findById(estacionId)
                .orElseThrow(() -> new RuntimeException("Estación no encontrada"));

        usuario.setEstacion(estacion);
        usuarioRepository.save(usuario);

        auditoriaService.registrar(
            usuario.getEmail(),
            "ASIGNACION_ESTACION",
            String.format("Asignado a estación: %s", estacion.getNombre()),
            "Usuario",
            usuario.getId()
        );
    }

    public List<UsuarioResponse> listarUsuarios(String rol) {
        List<Usuario> usuarios = (rol != null && !rol.isBlank())
                ? usuarioRepository.findByRol(rol.toUpperCase())
                : usuarioRepository.findAll();
        return usuarios.stream().map(UsuarioResponse::new).toList();
    }

    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        return new UsuarioResponse(usuario);
    }

    public UsuarioResponse obtenerPerfil(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        return new UsuarioResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        aplicarCambios(usuario, request);
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(ActualizarUsuarioRequest request, Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if (!usuario.getEmail().equals(request.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso por otro usuario");
        }

        aplicarCambios(usuario, request);
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    private void aplicarCambios(Usuario usuario, ActualizarUsuarioRequest request) {
        if (request.getNombre() != null) usuario.setNombre(request.getNombre());
        if (request.getEmail() != null) usuario.setEmail(request.getEmail());
        if (request.getTelefono() != null) usuario.setTelefono(request.getTelefono());
    }
}