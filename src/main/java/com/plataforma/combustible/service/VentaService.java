package com.plataforma.combustible.service;

import com.plataforma.combustible.dto.request.VentaRequest;
import com.plataforma.combustible.dto.response.VentaResponse;
import com.plataforma.combustible.entity.*;
import com.plataforma.combustible.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);
    private static final double NIVEL_BAJO_GALONES = 500.0;

    private final UsuarioRepository usuarioRepository;
    private final EstacionRepository estacionRepository;
    private final CombustibleRepository combustibleRepository;
    private final PrecioCombustibleRepository precioCombustibleRepository;
    private final InventarioRepository inventarioRepository;
    private final VentaRepository ventaRepository;
    private final NotificacionRepository notificacionRepository;

    public VentaService(UsuarioRepository usuarioRepository,
                        EstacionRepository estacionRepository,
                        CombustibleRepository combustibleRepository,
                        PrecioCombustibleRepository precioCombustibleRepository,
                        InventarioRepository inventarioRepository,
                        VentaRepository ventaRepository,
                        NotificacionRepository notificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.estacionRepository = estacionRepository;
        this.combustibleRepository = combustibleRepository;
        this.precioCombustibleRepository = precioCombustibleRepository;
        this.inventarioRepository = inventarioRepository;
        this.ventaRepository = ventaRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    public VentaResponse registrarVenta(VentaRequest request) {
        log.info("=== INICIANDO REGISTRO DE VENTA ===");
        
        // 1. Obtener usuario autenticado
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        log.info("Usuario: {} (ID: {})", email, usuario.getId());

        // 2. Obtener estación asociada
        Estacion estacion = usuario.getEstacion();
        if (estacion == null) {
            throw new RuntimeException("No tienes una estación asociada");
        }
        log.info("Estación: {} (ID: {})", estacion.getNombre(), estacion.getId());

        // 3. Obtener combustible
        Combustible combustible = combustibleRepository.findByNombre(request.getTipoCombustible())
            .orElseThrow(() -> new RuntimeException("Combustible no encontrado: " + request.getTipoCombustible()));
        log.info("Combustible: {}", combustible.getNombre());

        // 4. Obtener precio actual (NO del request, de la BD)
        PrecioCombustible precioActual = precioCombustibleRepository
            .findTopByEstacionIdAndCombustibleIdOrderByFechaDesc(estacion.getId(), combustible.getId())
            .orElseThrow(() -> new RuntimeException("No hay precio registrado para " + combustible.getNombre()));
        
        double precioUnitario = precioActual.getPrecio();
        log.info("Precio unitario obtenido de BD: ${}", precioUnitario);

        // 5. Obtener inventario actual
        Inventario inventario = inventarioRepository
            .findByEstacionIdAndCombustibleId(estacion.getId(), combustible.getId())
            .orElseThrow(() -> new RuntimeException("No hay inventario registrado para " + combustible.getNombre()));
        
        double stockActual = inventario.getCantidadDisponible().doubleValue();
        double cantidadVendida = request.getCantidad(); // En galones
        log.info("Stock actual: {} galones, Venta: {} galones", stockActual, cantidadVendida);

        // 6. Validar stock suficiente
        if (stockActual < cantidadVendida) {
            throw new RuntimeException(String.format("Stock insuficiente. Disponible: %.2f galones, Solicitado: %.2f galones", 
                stockActual, cantidadVendida));
        }

        // 7. Calcular monto total
        double montoTotal = cantidadVendida * precioUnitario;
        log.info("Monto total calculado: ${}", montoTotal);

        // 8. Actualizar inventario (restar lo vendido)
        double nuevoStock = stockActual - cantidadVendida;
        inventario.setCantidadDisponible(BigDecimal.valueOf(nuevoStock));
        inventarioRepository.save(inventario);
        log.info("Inventario actualizado. Nuevo stock: {} galones", nuevoStock);

        // 9. Registrar venta en BD
        Venta venta = new Venta();
        venta.setEstacion(estacion);
        venta.setCombustible(combustible);
        venta.setUsuario(usuario);
        venta.setCantidad(BigDecimal.valueOf(cantidadVendida));
        venta.setPrecioUnitario(BigDecimal.valueOf(precioUnitario));
        venta.setMontoTotal(BigDecimal.valueOf(montoTotal));
        venta.setFechaVenta(LocalDateTime.now());
        ventaRepository.save(venta);
        log.info("Venta registrada con ID: {}", venta.getId());

        // 10. Verificar nivel bajo y crear alerta
        boolean nivelBajo = nuevoStock < NIVEL_BAJO_GALONES;
        String mensajeAlerta = null;
        
        if (nivelBajo) {
            mensajeAlerta = String.format("⚠️ ALERTA: El inventario de %s en %s está BAJO (%.2f galones). Umbral mínimo: %.0f galones.", 
                combustible.getNombre(), estacion.getNombre(), nuevoStock, NIVEL_BAJO_GALONES);
            
            // Crear notificación
            Notificacion notificacion = new Notificacion();
            notificacion.setEstacionNombre(estacion.getNombre());
            notificacion.setInconsistencia(mensajeAlerta);
            notificacion.setEstado("Pendiente");
            notificacion.setFecha(LocalDateTime.now());
            notificacion.setUsuario(usuario);
            notificacionRepository.save(notificacion);
            
            log.warn(mensajeAlerta);
        }

        // 11. Crear respuesta
        VentaResponse response = new VentaResponse();
        response.setId(venta.getId());
        response.setEstacionNombre(estacion.getNombre());
        response.setCombustibleNombre(combustible.getNombre());
        response.setCantidad(cantidadVendida);
        response.setPrecioUnitario(precioUnitario);
        response.setMontoTotal(montoTotal);
        response.setFecha(venta.getFechaVenta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        log.info("=== VENTA REGISTRADA EXITOSAMENTE ===");
        return response;
    }

    // ========== NUEVOS MÉTODOS ==========

    public List<VentaResponse> getHistorialVentas() {
        log.info("=== OBTENIENDO HISTORIAL DE VENTAS ===");
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Venta> ventas;
        
        // Si es ADMIN o Empleado, ve todas las ventas de su estación
        if (usuario.getRol().equals("ADMIN") || usuario.getRol().equals("Empleado de estación")) {
            Estacion estacion = usuario.getEstacion();
            if (estacion == null) {
                throw new RuntimeException("No tienes una estación asociada");
            }
            ventas = ventaRepository.findByEstacionIdOrderByFechaVentaDesc(estacion.getId());
        } else {
            // Cliente ve sus propias ventas
            ventas = ventaRepository.findByUsuarioIdOrderByFechaVentaDesc(usuario.getId());
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        return ventas.stream().map(venta -> {
            VentaResponse response = new VentaResponse();
            response.setId(venta.getId());
            response.setEstacionNombre(venta.getEstacion().getNombre());
            response.setCombustibleNombre(venta.getCombustible().getNombre());
            response.setCantidad(venta.getCantidad().doubleValue());
            response.setPrecioUnitario(venta.getPrecioUnitario().doubleValue());
            response.setMontoTotal(venta.getMontoTotal().doubleValue());
            response.setFecha(venta.getFechaVenta().format(formatter));
            return response;
        }).collect(Collectors.toList());
    }

    public double getReporteMensual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Estacion estacion = usuario.getEstacion();
        if (estacion == null) {
            throw new RuntimeException("No tienes una estación asociada");
        }
        
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        Double total = ventaRepository.sumMontoTotalDelMes(estacion.getId(), inicioMes);
        
        return total != null ? total : 0.0;
    }
}