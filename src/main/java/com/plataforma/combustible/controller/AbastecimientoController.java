package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.AbastecimientoRequest;
import com.plataforma.combustible.dto.response.AbastecimientoResponse;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.service.AbastecimientoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/abastecimientos")
public class AbastecimientoController {

    private final AbastecimientoService abastecimientoService;
    private final UsuarioRepository usuarioRepository;
    private final EstacionRepository estacionRepository;

    public AbastecimientoController(AbastecimientoService abastecimientoService,
                                    UsuarioRepository usuarioRepository,
                                    EstacionRepository estacionRepository) {
        this.abastecimientoService = abastecimientoService;
        this.usuarioRepository = usuarioRepository;
        this.estacionRepository = estacionRepository;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<AbastecimientoResponse> solicitar(@RequestBody AbastecimientoRequest request) {
        System.out.println("=== LLEGÓ AL CONTROLLER ===");
        try {
            AbastecimientoResponse response = abastecimientoService.solicitarAbastecimiento(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            System.out.println("=== ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

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

    // ✅ CORREGIDO: AHORA DISTRIBUIDOR PUEDE VER
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('Entidad reguladora') or hasRole('Distribuidor')")
    public ResponseEntity<List<AbastecimientoResponse>> getAll() {
        return ResponseEntity.ok(abastecimientoService.getAll());
    }

    @GetMapping("/estacion/{estacionId}")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<List<AbastecimientoResponse>> getByEstacion(@PathVariable Long estacionId) {
        return ResponseEntity.ok(abastecimientoService.getByEstacion(estacionId));
    }

    @GetMapping("/distribuidor/{distribuidorId}")
    @PreAuthorize("hasRole('Distribuidor') or hasRole('ADMIN')")
    public ResponseEntity<List<AbastecimientoResponse>> getByDistribuidor(@PathVariable Long distribuidorId) {
        return ResponseEntity.ok(abastecimientoService.getByDistribuidor(distribuidorId));
    }

    // ✅ CORREGIDO: AHORA DISTRIBUIDOR PUEDE VER POR ESTADO
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('Entidad reguladora') or hasRole('Distribuidor')")
    public ResponseEntity<List<AbastecimientoResponse>> getByEstado(@PathVariable String estado) {
        return ResponseEntity.ok(abastecimientoService.getByEstado(estado));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<List<AbastecimientoResponse>> getHistorialRecargas(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Long combustibleId) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        
        Estacion estacion = usuario.getEstacion();
        if (estacion == null) {
            throw new RuntimeException("No tienes una estación asociada");
        }
        
        LocalDateTime inicio = fechaInicio != null ? LocalDateTime.parse(fechaInicio + "T00:00:00") : null;
        LocalDateTime fin = fechaFin != null ? LocalDateTime.parse(fechaFin + "T23:59:59") : null;
        
        List<AbastecimientoResponse> historial = abastecimientoService
            .getHistorialRecargas(estacion.getId(), inicio, fin, combustibleId);
        
        return ResponseEntity.ok(historial);
    }
}