package com.plataforma.combustible.service;

import com.plataforma.combustible.dto.request.InventarioRequest;
import com.plataforma.combustible.dto.response.DisponibilidadResponse;
import com.plataforma.combustible.dto.response.InventarioResponse;
import com.plataforma.combustible.entity.*;
import com.plataforma.combustible.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    private static final Logger log = LoggerFactory.getLogger(InventarioService.class);
    private static final double NIVEL_BAJO_GALONES = 500.0;
    private final InventarioRepository inventarioRepository;
    private final CombustibleRepository combustibleRepository;
    private final UsuarioRepository usuarioRepository;

    public InventarioService(InventarioRepository inventarioRepository, 
                             CombustibleRepository combustibleRepository, 
                             UsuarioRepository usuarioRepository) {
        this.inventarioRepository = inventarioRepository;
        this.combustibleRepository = combustibleRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public InventarioResponse registrarInventario(InventarioRequest request) {
        log.info("=== INICIANDO REGISTRO DE INVENTARIO ===");
        log.info("Request recibido: tipoCombustible={}, cantidad={}", 
            request.getTipoCombustible(), request.getCantidad());
        
        try {
            // 1. Obtener usuario autenticado
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Usuario autenticado: {}", email);
            
            Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", email);
                    return new RuntimeException("Usuario no encontrado: " + email);
                });
            log.info("Usuario encontrado: ID={}, Rol={}, EstacionID={}", 
                usuario.getId(), usuario.getRol(), usuario.getEstacion() != null ? usuario.getEstacion().getId() : "null");

            // 2. Obtener estación asociada al empleado (DIRECTAMENTE DEL USUARIO)
            Estacion estacion = usuario.getEstacion();
            if (estacion == null) {
                log.error("No se encontró estación para usuario ID: {}", usuario.getId());
                throw new RuntimeException("No tienes una estación asociada. Usuario ID: " + usuario.getId());
            }
            log.info("Estación encontrada: ID={}, Nombre={}", estacion.getId(), estacion.getNombre());

            // 3. Obtener o crear combustible
            log.info("Buscando combustible: {}", request.getTipoCombustible());
            Combustible combustible = combustibleRepository.findByNombre(request.getTipoCombustible())
                .orElseGet(() -> {
                    log.info("Combustible no encontrado, creando nuevo: {}", request.getTipoCombustible());
                    Combustible nuevo = new Combustible();
                    nuevo.setNombre(request.getTipoCombustible());
                    nuevo.setActivo(true);
                    return combustibleRepository.save(nuevo);
                });
            log.info("Combustible ID: {}", combustible.getId());

            // 4. Buscar inventario existente
            log.info("Buscando inventario para estación ID={}, combustible ID={}", 
                estacion.getId(), combustible.getId());
            Inventario inventario = inventarioRepository
                .findByEstacionIdAndCombustibleId(estacion.getId(), combustible.getId())
                .orElse(null);

            // 5. Actualizar cantidad
            BigDecimal nuevaCantidad = BigDecimal.valueOf(request.getCantidad());
            log.info("Nueva cantidad a agregar: {}", nuevaCantidad);
            
            if (inventario == null) {
                log.info("Creando nuevo registro de inventario");
                inventario = new Inventario();
                inventario.setEstacion(estacion);
                inventario.setCombustible(combustible);
                inventario.setCantidadDisponible(nuevaCantidad);
            } else {
                BigDecimal cantidadActual = inventario.getCantidadDisponible();
                log.info("Inventario existente. Cantidad actual: {}", cantidadActual);
                inventario.setCantidadDisponible(cantidadActual.add(nuevaCantidad));
                log.info("Nueva cantidad total: {}", inventario.getCantidadDisponible());
            }

            Inventario guardado = inventarioRepository.save(inventario);
            log.info("Inventario guardado con ID: {}", guardado.getId());

            // 6. Crear respuesta
            InventarioResponse response = new InventarioResponse();
            response.setId(guardado.getId());
            response.setEstacionNombre(estacion.getNombre());
            response.setCombustibleNombre(combustible.getNombre());
            response.setCantidadDisponible(guardado.getCantidadDisponible().doubleValue());
            response.setFechaActualizacion(guardado.getFechaActualizacion());
            
            log.info("=== INVENTARIO REGISTRADO EXITOSAMENTE ===");
            return response;
            
        } catch (Exception e) {
            log.error("Error en registro de inventario: ", e);
            throw e;
        }
    }

     public List<DisponibilidadResponse> getDisponibilidad() {
    log.info("=== OBTENIENDO DISPONIBILIDAD DE INVENTARIO ===");
    
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario usuario = usuarioRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    
    Estacion estacion = usuario.getEstacion();
    if (estacion == null) {
        throw new RuntimeException("No tienes una estación asociada");
    }
    
    List<Inventario> inventarios = inventarioRepository.findByEstacionId(estacion.getId());
    
    return inventarios.stream().map(inv -> {
        DisponibilidadResponse response = new DisponibilidadResponse();
        response.setCombustibleNombre(inv.getCombustible().getNombre());
        response.setCantidadDisponible(inv.getCantidadDisponible().doubleValue());
        response.setNivelBajo(inv.getCantidadDisponible().doubleValue() < NIVEL_BAJO_GALONES);
        
        // Calcular si aplica subsidio (ejemplo: solo para clientes con ciertos vehículos)
        boolean aplicaSubsidio = false;
        if (usuario.getRol().equals("Cliente")) {
            // Lógica para determinar subsidio según tipo de vehículo
            aplicaSubsidio = true; // Simplificado
        }
        response.setAplicaSubsidio(aplicaSubsidio);
        
        return response;
    }).collect(Collectors.toList());
}
}