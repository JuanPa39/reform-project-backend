package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.NotificacionRequest;
import com.plataforma.combustible.dto.response.NotificacionResponse;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.Notificacion;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.NotificacionRepository;
import com.plataforma.combustible.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstacionRepository estacionRepository;

    public NotificacionController(NotificacionRepository notificacionRepository, 
                                   UsuarioRepository usuarioRepository,
                                   EstacionRepository estacionRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.estacionRepository = estacionRepository;
    }

    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> getNotificaciones() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        List<Notificacion> notificaciones;
        
        if (usuario != null) {
            String rol = usuario.getRol();
            
            switch (rol) {
                case "ADMIN":
                case "Entidad reguladora":
                    // ADMIN y Entidad Reguladora ven TODAS las notificaciones
                    notificaciones = notificacionRepository.findAllByOrderByFechaDesc();
                    break;
                    
                case "Empleado de estación":
                    // Empleado ve notificaciones de su estación + las que le enviaron a él
                    String estacionNombre = usuario.getEstacion() != null ? usuario.getEstacion().getNombre() : "";
                    notificaciones = notificacionRepository.findByEstacionNombreOrderByFechaDesc(estacionNombre);
                    break;
                    
                default:
                    // Cliente y otros roles ven solo sus propias notificaciones
                    notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());
                    break;
            }
        } else {
            notificaciones = notificacionRepository.findAll();
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        List<NotificacionResponse> response = notificaciones.stream().map(n -> {
            NotificacionResponse resp = new NotificacionResponse();
            resp.setId(n.getId());
            resp.setEstacionNombre(n.getEstacionNombre());
            resp.setInconsistencia(n.getInconsistencia());
            resp.setEstado(n.getEstado());
            resp.setFecha(n.getFecha() != null ? n.getFecha().format(formatter) : "");
            resp.setMensaje(n.getInconsistencia());
            return resp;
        }).collect(Collectors.toList());
        
        System.out.println("=== NOTIFICACIONES ===");
        System.out.println("Usuario: " + email);
        System.out.println("Rol: " + (usuario != null ? usuario.getRol() : "null"));
        System.out.println("Total notificaciones devueltas: " + response.size());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<NotificacionResponse> crearNotificacion(@RequestBody NotificacionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        // Obtener el nombre de la estación seleccionada
        String nombreEstacion = "Desconocida";
        if (request.getEstacionId() != null) {
            Estacion estacion = estacionRepository.findById(request.getEstacionId()).orElse(null);
            if (estacion != null) {
                nombreEstacion = estacion.getNombre();
            }
        }
        
        System.out.println("=== CREANDO NOTIFICACIÓN ===");
        System.out.println("Usuario: " + email);
        System.out.println("Estación ID: " + request.getEstacionId());
        System.out.println("Nombre estación: " + nombreEstacion);
        System.out.println("Inconsistencia: " + request.getInconsistencia());
        
        // Crear la notificación
        Notificacion notificacion = new Notificacion();
        notificacion.setEstacionNombre(nombreEstacion);
        notificacion.setInconsistencia(request.getInconsistencia());
        notificacion.setEstado("Pendiente");
        notificacion.setFecha(LocalDateTime.now());
        notificacion.setUsuario(usuario);
        
        Notificacion guardada = notificacionRepository.save(notificacion);
        
        // Crear respuesta
        NotificacionResponse response = new NotificacionResponse();
        response.setId(guardada.getId());
        response.setEstacionNombre(guardada.getEstacionNombre());
        response.setInconsistencia(guardada.getInconsistencia());
        response.setEstado(guardada.getEstado());
        response.setFecha(guardada.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setMensaje(guardada.getInconsistencia());
        
        return ResponseEntity.ok(response);
    }
}