package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.PrecioRequest;
import com.plataforma.combustible.dto.response.PrecioResponse;
import com.plataforma.combustible.entity.Combustible;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.PrecioCombustible;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.CombustibleRepository;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.PrecioCombustibleRepository;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.service.AuditoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/precios")
public class PrecioController {
    
    private final PrecioCombustibleRepository precioRepository;
    private final EstacionRepository estacionRepository;
    private final CombustibleRepository combustibleRepository;
    private final UsuarioRepository usuarioRepository;  // ← AGREGAR
    private final AuditoriaService auditoriaService;    // ← AGREGAR

    public PrecioController(PrecioCombustibleRepository precioRepository, 
                            EstacionRepository estacionRepository,
                            CombustibleRepository combustibleRepository,
                            UsuarioRepository usuarioRepository,
                            AuditoriaService auditoriaService) {
        this.precioRepository = precioRepository;
        this.estacionRepository = estacionRepository;
        this.combustibleRepository = combustibleRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<PrecioResponse>> getAllPrecios() {
        List<PrecioResponse> precios = precioRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(precios);
    }

    @PostMapping
    @PreAuthorize("hasRole('Empleado de estación') or hasRole('ADMIN')")
    public ResponseEntity<Void> createPrecio(@RequestBody PrecioRequest request) {
        // Obtener usuario autenticado
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        Estacion estacion = estacionRepository.findById(request.getEstacionId()).orElse(null);
        if (estacion == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Buscar el combustible por nombre
        Combustible combustible = combustibleRepository.findByNombre(request.getTipoCombustible()).orElse(null);
        if (combustible == null) {
            return ResponseEntity.badRequest().build();
        }
        
        PrecioCombustible precio = new PrecioCombustible();
        precio.setEstacion(estacion);
        precio.setCombustible(combustible);
        precio.setPrecio(request.getPrecio());
        precio.setFecha(LocalDate.now());
        
        PrecioCombustible guardado = precioRepository.save(precio);
        
        // ✅ REGISTRAR EN AUDITORÍA
        if (usuario != null) {
            auditoriaService.registrar(
                usuario.getEmail(),
                "REGISTRO_PRECIO",
                String.format("Nuevo precio para %s en %s: $%.2f", 
                    combustible.getNombre(), estacion.getNombre(), request.getPrecio()),
                "PrecioCombustible",
                guardado.getId()
            );
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private PrecioResponse convertToResponse(PrecioCombustible precio) {
        PrecioResponse response = new PrecioResponse();
        response.setId(precio.getId());
        response.setEstacionNombre(precio.getEstacion().getNombre());
        response.setCombustibleNombre(precio.getCombustible().getNombre());
        response.setPrecio(precio.getPrecio());
        response.setFecha(precio.getFecha().toString());
        return response;
    }
}