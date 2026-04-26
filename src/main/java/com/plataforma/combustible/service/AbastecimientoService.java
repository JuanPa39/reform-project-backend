package com.plataforma.combustible.service;

import com.plataforma.combustible.dto.request.AbastecimientoRequest;
import com.plataforma.combustible.dto.response.AbastecimientoResponse;
import com.plataforma.combustible.entity.*;
import com.plataforma.combustible.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AbastecimientoService {

    private final AbastecimientoRepository abastecimientoRepository;
    private final DistribuidorRepository distribuidorRepository;
    private final EstacionRepository estacionRepository;
    private final CombustibleRepository combustibleRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioRepository inventarioRepository;
    private final AuditoriaService auditoriaService;

    public AbastecimientoService(AbastecimientoRepository abastecimientoRepository,
                                 DistribuidorRepository distribuidorRepository,
                                 EstacionRepository estacionRepository,
                                 CombustibleRepository combustibleRepository,
                                 UsuarioRepository usuarioRepository,
                                 InventarioRepository inventarioRepository,
                                 AuditoriaService auditoriaService) {
        this.abastecimientoRepository = abastecimientoRepository;
        this.distribuidorRepository = distribuidorRepository;
        this.estacionRepository = estacionRepository;
        this.combustibleRepository = combustibleRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioRepository = inventarioRepository;
        this.auditoriaService = auditoriaService;
    }

    // Solicitar abastecimiento (desde estación)
    @Transactional
    public AbastecimientoResponse solicitarAbastecimiento(AbastecimientoRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Distribuidor distribuidor = distribuidorRepository.findById(request.getDistribuidorId())
            .orElseThrow(() -> new RuntimeException("Distribuidor no encontrado"));
        
        Estacion estacion = estacionRepository.findById(request.getEstacionId())
            .orElseThrow(() -> new RuntimeException("Estación no encontrada"));
        
        Combustible combustible = combustibleRepository.findById(request.getCombustibleId())
            .orElseThrow(() -> new RuntimeException("Combustible no encontrado"));
        
        Abastecimiento abastecimiento = new Abastecimiento();
        abastecimiento.setDistribuidor(distribuidor);
        abastecimiento.setEstacion(estacion);
        abastecimiento.setCombustible(combustible);
        abastecimiento.setCantidadGalones(request.getCantidadGalones());
        abastecimiento.setFecha(LocalDateTime.now());
        abastecimiento.setEstado("SOLICITADO");
        
        Abastecimiento guardado = abastecimientoRepository.save(abastecimiento);
        
        auditoriaService.registrar(
            usuario.getEmail(),
            "SOLICITUD_ABASTECIMIENTO",
            String.format("Solicitud de %s galones de %s para %s", 
                request.getCantidadGalones(), combustible.getNombre(), estacion.getNombre()),
            "Abastecimiento",
            guardado.getId()
        );
        
        return convertToResponse(guardado);
    }

    // Aprobar abastecimiento (desde distribuidor)
    @Transactional
    public AbastecimientoResponse aprobarAbastecimiento(Long id) {
        Abastecimiento abastecimiento = abastecimientoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        abastecimiento.setEstado("EN_PROCESO");
        Abastecimiento actualizado = abastecimientoRepository.save(abastecimiento);
        
        return convertToResponse(actualizado);
    }

    // Completar abastecimiento (cuando llega a la estación)
    @Transactional
    public AbastecimientoResponse completarAbastecimiento(Long id) {
        Abastecimiento abastecimiento = abastecimientoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        abastecimiento.setEstado("COMPLETADO");
        Abastecimiento actualizado = abastecimientoRepository.save(abastecimiento);
        
        // Actualizar inventario de la estación
        Inventario inventario = inventarioRepository
            .findByEstacionIdAndCombustibleId(
                abastecimiento.getEstacion().getId(), 
                abastecimiento.getCombustible().getId())
            .orElse(new Inventario());
        
        if (inventario.getId() == null) {
            inventario.setEstacion(abastecimiento.getEstacion());
            inventario.setCombustible(abastecimiento.getCombustible());
            inventario.setCantidadDisponible(java.math.BigDecimal.valueOf(abastecimiento.getCantidadGalones()));
        } else {
            inventario.setCantidadDisponible(
                inventario.getCantidadDisponible().add(
                    java.math.BigDecimal.valueOf(abastecimiento.getCantidadGalones()))
            );
        }
        inventarioRepository.save(inventario);
        
        return convertToResponse(actualizado);
    }

    // Rechazar abastecimiento
    @Transactional
    public AbastecimientoResponse rechazarAbastecimiento(Long id) {
        Abastecimiento abastecimiento = abastecimientoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        abastecimiento.setEstado("RECHAZADO");
        Abastecimiento actualizado = abastecimientoRepository.save(abastecimiento);
        
        return convertToResponse(actualizado);
    }

    // Obtener todas las solicitudes
    public List<AbastecimientoResponse> getAll() {
        return abastecimientoRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // Obtener solicitudes por estación
    public List<AbastecimientoResponse> getByEstacion(Long estacionId) {
        return abastecimientoRepository.findByEstacionId(estacionId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // Obtener solicitudes por distribuidor
    public List<AbastecimientoResponse> getByDistribuidor(Long distribuidorId) {
        return abastecimientoRepository.findByDistribuidorId(distribuidorId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // Obtener solicitudes por estado
    public List<AbastecimientoResponse> getByEstado(String estado) {
        return abastecimientoRepository.findByEstado(estado).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    private AbastecimientoResponse convertToResponse(Abastecimiento a) {
        AbastecimientoResponse response = new AbastecimientoResponse();
        response.setId(a.getId());
        response.setDistribuidorNombre(a.getDistribuidor().getNombre());
        response.setEstacionNombre(a.getEstacion().getNombre());
        response.setCombustibleNombre(a.getCombustible().getNombre());
        response.setCantidadGalones(a.getCantidadGalones());
        response.setFecha(a.getFecha());
        response.setEstado(a.getEstado());
        return response;
    }
}