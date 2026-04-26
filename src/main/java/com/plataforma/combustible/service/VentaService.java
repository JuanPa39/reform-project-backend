package com.plataforma.combustible.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.combustible.dto.request.VentaRequest;
import com.plataforma.combustible.dto.response.VentaResponse;
import com.plataforma.combustible.entity.Combustible;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.Inventario;
import com.plataforma.combustible.entity.Notificacion;
import com.plataforma.combustible.entity.PrecioCombustible;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.entity.Venta;
import com.plataforma.combustible.repository.CombustibleRepository;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.InventarioRepository;
import com.plataforma.combustible.repository.NotificacionRepository;
import com.plataforma.combustible.repository.PrecioCombustibleRepository;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.repository.VentaRepository;

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
    private final AuditoriaService auditoriaService;

    public VentaService(UsuarioRepository usuarioRepository,
                        EstacionRepository estacionRepository,
                        CombustibleRepository combustibleRepository,
                        PrecioCombustibleRepository precioCombustibleRepository,
                        InventarioRepository inventarioRepository,
                        VentaRepository ventaRepository,
                        NotificacionRepository notificacionRepository,
                        AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.estacionRepository = estacionRepository;
        this.combustibleRepository = combustibleRepository;
        this.precioCombustibleRepository = precioCombustibleRepository;
        this.inventarioRepository = inventarioRepository;
        this.ventaRepository = ventaRepository;
        this.notificacionRepository = notificacionRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public VentaResponse registrarVenta(VentaRequest request) {
        log.info("=== INICIANDO REGISTRO DE VENTA ===");
        
        try {
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

            // 4. Obtener precio actual
            PrecioCombustible precioActual = precioCombustibleRepository
                .findTopByEstacionIdAndCombustibleIdOrderByFechaDesc(estacion.getId(), combustible.getId())
                .orElseThrow(() -> new RuntimeException("No hay precio registrado para " + combustible.getNombre()));
            
            double precioOriginal = precioActual.getPrecio();
            double cantidadVendida = request.getCantidad();
            String tipoVehiculo = request.getTipoVehiculo();
            
            // 5. Calcular precio con subsidio (si aplica)
            double precioUnitario = precioOriginal;
            boolean subsidioAplicado = false;
            
            if (tipoVehiculo != null && !tipoVehiculo.isEmpty()) {
                precioUnitario = calcularPrecioConSubsidio(tipoVehiculo, combustible.getNombre(), precioOriginal, cantidadVendida);
                subsidioAplicado = precioUnitario != precioOriginal;
                if (subsidioAplicado) {
                    log.info("💰 Subsidio aplicado para {}: precio original ${} → precio con subsidio ${}", 
                        tipoVehiculo, precioOriginal, precioUnitario);
                }
            }

            // 6. Obtener inventario actual
            Inventario inventario = inventarioRepository
                .findByEstacionIdAndCombustibleId(estacion.getId(), combustible.getId())
                .orElseThrow(() -> new RuntimeException("No hay inventario registrado para " + combustible.getNombre()));
            
            double stockActual = inventario.getCantidadDisponible().doubleValue();
            log.info("Stock actual: {} galones, Venta: {} galones", stockActual, cantidadVendida);

            // 7. Validar stock suficiente
            if (stockActual < cantidadVendida) {
                throw new RuntimeException(String.format("Stock insuficiente. Disponible: %.2f galones, Solicitado: %.2f galones", 
                    stockActual, cantidadVendida));
            }

            // 8. Calcular monto total
            double montoTotal = cantidadVendida * precioUnitario;
            log.info("Monto total calculado: ${}", montoTotal);

            // 9. Actualizar inventario
            double nuevoStock = stockActual - cantidadVendida;
            inventario.setCantidadDisponible(BigDecimal.valueOf(nuevoStock));
            inventarioRepository.save(inventario);

            // 10. Registrar venta con subsidio
            Venta venta = new Venta();
            venta.setEstacion(estacion);
            venta.setCombustible(combustible);
            venta.setUsuario(usuario);
            venta.setCantidad(BigDecimal.valueOf(cantidadVendida));
            venta.setPrecioUnitario(BigDecimal.valueOf(precioUnitario));
            venta.setMontoTotal(BigDecimal.valueOf(montoTotal));
            venta.setFechaVenta(LocalDateTime.now());
            venta.setTipoVehiculo(tipoVehiculo);
            venta.setSubsidioAplicado(subsidioAplicado);
            ventaRepository.save(venta);
            log.info("Venta registrada con ID: {}", venta.getId());

            // 11. Registrar en auditoría
            String detallesAuditoria = String.format("Venta de %.2f galones de %s por $%.2f", 
                cantidadVendida, combustible.getNombre(), montoTotal);
            if (subsidioAplicado) {
                detallesAuditoria += String.format(" (Subsidio aplicado para %s: $%.2f → $%.2f)", 
                    tipoVehiculo, precioOriginal, precioUnitario);
            }
            auditoriaService.registrar(usuario.getEmail(), "VENTA", detallesAuditoria, "Venta", venta.getId());

            // 12. Verificar nivel bajo
            boolean nivelBajo = nuevoStock < NIVEL_BAJO_GALONES;
            if (nivelBajo) {
                String mensajeAlerta = String.format("⚠️ ALERTA: El inventario de %s en %s está BAJO (%.2f galones). Umbral mínimo: %.0f galones.", 
                    combustible.getNombre(), estacion.getNombre(), nuevoStock, NIVEL_BAJO_GALONES);
                
                Notificacion notificacion = new Notificacion();
                notificacion.setEstacionNombre(estacion.getNombre());
                notificacion.setInconsistencia(mensajeAlerta);
                notificacion.setEstado("Pendiente");
                notificacion.setFecha(LocalDateTime.now());
                notificacion.setUsuario(usuario);
                notificacionRepository.save(notificacion);
                log.warn(mensajeAlerta);
            }

            // 13. Crear respuesta
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
            
        } catch (Exception e) {
            log.error("Error en registro de venta: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Agrega este método auxiliar
    private double calcularPrecioConSubsidio(String tipoVehiculo, String combustible, double precioOriginal, double cantidad) {
        // Reglas de subsidio según Decreto 1428/2025
        if ("Gasolina Extra".equals(combustible)) {
            return precioOriginal; // Sin subsidio
        }
        
        if ("Gasolina Corriente".equals(combustible)) {
            switch (tipoVehiculo) {
                case "Particular":
                case "Oficial":
                case "Diplomático":
                    return cantidad <= 10 ? precioOriginal * 0.85 : precioOriginal; // 15% descuento hasta 10 galones
                case "Moto":
                    return cantidad <= 5 ? precioOriginal * 0.85 : precioOriginal;
                case "Taxi":
                case "Servicio Público (Bus)":
                case "Camión de carga":
                    return cantidad <= 50 ? precioOriginal * 0.85 : precioOriginal;
                default:
                    return precioOriginal;
            }
        }
        
        if ("ACPM".equals(combustible)) {
            switch (tipoVehiculo) {
                case "Taxi":
                case "Servicio Público (Bus)":
                case "Camión de carga":
                    return cantidad <= 30 ? precioOriginal * 0.90 : precioOriginal; // 10% descuento
                default:
                    return precioOriginal; // Sin subsidio
            }
        }
        
        return precioOriginal;
    }

    public List<VentaResponse> getHistorialVentas() {
        log.info("=== OBTENIENDO HISTORIAL DE VENTAS ===");
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Venta> ventas;
        
        if (usuario.getRol().equals("ADMIN") || usuario.getRol().equals("Empleado de estación")) {
            Estacion estacion = usuario.getEstacion();
            if (estacion == null) {
                throw new RuntimeException("No tienes una estación asociada");
            }
            ventas = ventaRepository.findByEstacionIdOrderByFechaVentaDesc(estacion.getId());
        } else {
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