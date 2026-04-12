package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.response.DisponibilidadResponse;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.service.SubsidioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {
    
    private final UsuarioRepository usuarioRepository;
    private final SubsidioService subsidioService;

    public DisponibilidadController(UsuarioRepository usuarioRepository, SubsidioService subsidioService) {
        this.usuarioRepository = usuarioRepository;
        this.subsidioService = subsidioService;
    }

    @GetMapping
    public ResponseEntity<DisponibilidadResponse> getDisponibilidad(
            @RequestParam String combustible,
            @RequestParam(required = false) String tipoVehiculo,
            @RequestParam(required = false) Double cantidad) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        // Obtener cantidad disponible (simulado - debería venir de inventario)
        double cantidadDisponible = 10000;
        
        boolean aplicaSubsidio = false;
        String mensajeSubsidio = "No aplica subsidio";
        
        // Solo calcular subsidio para clientes con todos los parámetros
        if (usuario != null && "Cliente".equals(usuario.getRol()) && cantidad != null && tipoVehiculo != null) {
            aplicaSubsidio = subsidioService.verificarSubsidio(tipoVehiculo, combustible, cantidad);
            mensajeSubsidio = subsidioService.getMensajeSubsidio(tipoVehiculo, combustible, cantidad);
            
            // Log para depuración
            System.out.println("=== CÁLCULO SUBSIDIO ===");
            System.out.println("Usuario: " + email);
            System.out.println("Combustible: " + combustible);
            System.out.println("Tipo Vehículo: " + tipoVehiculo);
            System.out.println("Cantidad: " + cantidad);
            System.out.println("Aplica subsidio: " + aplicaSubsidio);
            System.out.println("Mensaje: " + mensajeSubsidio);
        } else if (usuario != null && !"Cliente".equals(usuario.getRol())) {
            mensajeSubsidio = "El subsidio aplica solo para clientes particulares";
        } else if (cantidad == null || tipoVehiculo == null) {
            mensajeSubsidio = "Seleccione tipo de vehículo y cantidad para calcular subsidio";
        }
        
        DisponibilidadResponse response = new DisponibilidadResponse();
        response.setCantidadDisponible(cantidadDisponible);
        response.setAplicaSubsidio(aplicaSubsidio);
        response.setMensajeSubsidio(mensajeSubsidio);
        response.setCombustibleNombre(combustible);
        
        return ResponseEntity.ok(response);
    }
}