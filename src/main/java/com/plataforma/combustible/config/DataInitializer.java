package com.plataforma.combustible.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.combustible.entity.Combustible;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.Inventario;
import com.plataforma.combustible.entity.Notificacion;
import com.plataforma.combustible.entity.Normativa;
import com.plataforma.combustible.entity.PrecioCombustible;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.entity.Venta;
import com.plataforma.combustible.repository.CombustibleRepository;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.InventarioRepository;
import com.plataforma.combustible.repository.NotificacionRepository;
import com.plataforma.combustible.repository.NormativaRepository;
import com.plataforma.combustible.repository.PrecioCombustibleRepository;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.repository.VentaRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final EstacionRepository estacionRepository;
    private final CombustibleRepository combustibleRepository;
    private final InventarioRepository inventarioRepository;
    private final PrecioCombustibleRepository precioCombustibleRepository;
    private final VentaRepository ventaRepository;
    private final NotificacionRepository notificacionRepository;
    private final NormativaRepository normativaRepository;  // ← AGREGADO
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           EstacionRepository estacionRepository,
                           CombustibleRepository combustibleRepository,
                           InventarioRepository inventarioRepository,
                           PrecioCombustibleRepository precioCombustibleRepository,
                           VentaRepository ventaRepository,
                           NotificacionRepository notificacionRepository,
                           NormativaRepository normativaRepository,  // ← AGREGADO
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.estacionRepository = estacionRepository;
        this.combustibleRepository = combustibleRepository;
        this.inventarioRepository = inventarioRepository;
        this.precioCombustibleRepository = precioCombustibleRepository;
        this.ventaRepository = ventaRepository;
        this.notificacionRepository = notificacionRepository;
        this.normativaRepository = normativaRepository;  // ← AGREGADO
        this.passwordEncoder = passwordEncoder;
        log.info("🔧 DataInitializer CREADO");
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🚀🚀🚀 DataInitializer.run() EJECUTÁNDOSE 🚀🚀🚀");
        
        // 1. COMBUSTIBLES
        log.info("📦 Creando combustibles...");
        Combustible acpm = crearCombustible("ACPM");
        Combustible gasolinaCorriente = crearCombustible("Gasolina Corriente");
        Combustible gasolinaExtra = crearCombustible("Gasolina Extra");
        
        // 2. ESTACIONES
        log.info("🏪 Creando estaciones...");
        Estacion estacionCentro = crearEstacion("Estación Centro", "900123456-1", "Calle 10 #20-30", "6012345678", "Lun-Dom 6:00-22:00");
        Estacion estacionNorte = crearEstacion("Estación Norte", "900123456-2", "Carrera 15 #100-50", "6018765432", "Lun-Dom 24h");
        
        // 3. USUARIOS
        log.info("👥 Creando usuarios...");
        Usuario admin = crearUsuario("admin@test.com", "123456", "ADMIN", "Admin Administrador");
        Usuario empleado = crearUsuario("empleado@test.com", "123456", "Empleado de estación", "Carlos Ramírez");
        Usuario cliente = crearUsuario("cliente@test.com", "123456", "Cliente", "Laura Gómez");
        Usuario tecnico = crearUsuario("tecnico@test.com", "123456", "Equipo técnico", "Andrés Torres");
        Usuario regulador = crearUsuario("regulador@test.com", "123456", "Entidad reguladora", "Sofía Martínez");
        Usuario distribuidor = crearUsuario("distribuidor@test.com", "123456", "Distribuidor", "Pedro Díaz");
        
        // 4. ASOCIAR EMPLEADO A ESTACIÓN
        log.info("🔗 Asociando empleado a estación...");
        if (empleado != null && estacionCentro != null) {
            empleado.setEstacion(estacionCentro);
            usuarioRepository.save(empleado);
            log.info("✅ Empleado asociado a estación: {}", estacionCentro.getNombre());
        }
        
        // 5. INVENTARIO
        log.info("📊 Creando inventario...");
        crearInventario(estacionCentro, acpm, 10000);
        crearInventario(estacionCentro, gasolinaCorriente, 5000);
        crearInventario(estacionCentro, gasolinaExtra, 3000);
        crearInventario(estacionNorte, acpm, 8000);
        crearInventario(estacionNorte, gasolinaCorriente, 4000);
        crearInventario(estacionNorte, gasolinaExtra, 2000);
        
        // 6. PRECIOS DE COMBUSTIBLE
        log.info("💰 Creando precios...");
        LocalDate today = LocalDate.now();
        crearPrecio(estacionCentro, acpm, 9500.00, today);
        crearPrecio(estacionCentro, gasolinaCorriente, 12000.00, today);
        crearPrecio(estacionCentro, gasolinaExtra, 14000.00, today);
        crearPrecio(estacionNorte, acpm, 9600.00, today);
        crearPrecio(estacionNorte, gasolinaCorriente, 11800.00, today);
        crearPrecio(estacionNorte, gasolinaExtra, 13800.00, today);
        
        // 7. VENTAS
        log.info("🧾 Creando ventas...");
        crearVenta(estacionCentro, acpm, cliente, 20, 9500.00, LocalDateTime.now().minusDays(1));
        crearVenta(estacionCentro, gasolinaCorriente, cliente, 15, 12000.00, LocalDateTime.now().minusDays(2));
        crearVenta(estacionNorte, acpm, cliente, 30, 9600.00, LocalDateTime.now().minusDays(3));
        crearVenta(estacionCentro, acpm, empleado, 50, 9500.00, LocalDateTime.now().minusHours(5));
        
        // 8. NOTIFICACIONES
        log.info("🔔 Creando notificaciones...");
        crearNotificacion(estacionCentro.getNombre(), "Precio del ACPM no coincide con el reportado", empleado);
        crearNotificacion(estacionNorte.getNombre(), "Inventario bajo de Gasolina Corriente", empleado);
        crearNotificacion(estacionCentro.getNombre(), "Solicitud de mantenimiento de bombas", admin);
        
        // 9. NORMATIVAS
        log.info("📜 Creando normativas...");
        crearNormativas();
        
        // 10. RESUMEN FINAL
        log.info("=== CARGA DE DATOS COMPLETADA ===");
        log.info("📊 Usuarios: {}", usuarioRepository.count());
        log.info("📊 Estaciones: {}", estacionRepository.count());
        log.info("📊 Combustibles: {}", combustibleRepository.count());
        log.info("📊 Inventario: {}", inventarioRepository.count());
        log.info("📊 Precios: {}", precioCombustibleRepository.count());
        log.info("📊 Ventas: {}", ventaRepository.count());
        log.info("📊 Notificaciones: {}", notificacionRepository.count());
        log.info("📊 Normativas: {}", normativaRepository.count());
        
        // Verificación final
        Usuario empleadoFinal = usuarioRepository.findByEmail("empleado@test.com").orElse(null);
        if (empleadoFinal != null && empleadoFinal.getEstacion() != null) {
            log.info("🎉 EMPLEADO CORRECTAMENTE ASOCIADO A ESTACIÓN: {}", empleadoFinal.getEstacion().getNombre());
        } else {
            log.error("❌ ERROR: Empleado NO tiene estación asociada!");
        }
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private Combustible crearCombustible(String nombre) {
        return combustibleRepository.findByNombre(nombre).orElseGet(() -> {
            Combustible c = new Combustible();
            c.setNombre(nombre);
            c.setActivo(true);
            log.info("   ✅ Combustible creado: {}", nombre);
            return combustibleRepository.save(c);
        });
    }
    
    private Estacion crearEstacion(String nombre, String nit, String ubicacion, String telefono, String horario) {
        return estacionRepository.findByNit(nit).orElseGet(() -> {
            Estacion e = new Estacion();
            e.setNombre(nombre);
            e.setNit(nit);
            e.setUbicacion(ubicacion);
            e.setTelefono(telefono);
            e.setHorario(horario);
            e.setActiva(true);
            e.setFechaRegistro(LocalDateTime.now());
            log.info("   ✅ Estación creada: {}", nombre);
            return estacionRepository.save(e);
        });
    }
    
    private Usuario crearUsuario(String email, String password, String rol, String nombre) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setEmail(email);
            u.setContrasena(passwordEncoder.encode(password));
            u.setRol(rol);
            u.setNombre(nombre);
            u.setEnabled(true);
            log.info("   ✅ Usuario creado: {} ({})", email, rol);
            return usuarioRepository.save(u);
        });
    }
    
    private void crearInventario(Estacion estacion, Combustible combustible, double cantidad) {
        if (inventarioRepository.findByEstacionIdAndCombustibleId(estacion.getId(), combustible.getId()).isEmpty()) {
            Inventario i = new Inventario();
            i.setEstacion(estacion);
            i.setCombustible(combustible);
            i.setCantidadDisponible(BigDecimal.valueOf(cantidad));
            i.setFechaActualizacion(LocalDateTime.now());
            inventarioRepository.save(i);
            log.info("   ✅ Inventario: {} - {}: {} galones", estacion.getNombre(), combustible.getNombre(), cantidad);
        }
    }
    
    private void crearPrecio(Estacion estacion, Combustible combustible, Double precio, LocalDate fecha) {
        boolean existe = precioCombustibleRepository.existsByEstacionIdAndCombustibleIdAndFecha(
            estacion.getId(), combustible.getId(), fecha);
        if (!existe) {
            PrecioCombustible p = new PrecioCombustible();
            p.setEstacion(estacion);
            p.setCombustible(combustible);
            p.setPrecio(precio);
            p.setFecha(fecha);
            p.setPrecioRegulado(true);
            precioCombustibleRepository.save(p);
            log.info("   ✅ Precio: {} - {}: ${}", estacion.getNombre(), combustible.getNombre(), precio);
        }
    }
    
    private void crearVenta(Estacion estacion, Combustible combustible, Usuario usuario, 
                            double cantidad, double precioUnitario, LocalDateTime fecha) {
        Venta v = new Venta();
        v.setEstacion(estacion);
        v.setCombustible(combustible);
        v.setUsuario(usuario);
        v.setCantidad(BigDecimal.valueOf(cantidad));
        v.setPrecioUnitario(BigDecimal.valueOf(precioUnitario));
        v.setMontoTotal(BigDecimal.valueOf(precioUnitario).multiply(BigDecimal.valueOf(cantidad)));
        v.setSubsidioAplicado(false);
        v.setFechaVenta(fecha);
        ventaRepository.save(v);
        log.info("   ✅ Venta: {} - {}: {} galones - ${}", estacion.getNombre(), combustible.getNombre(), cantidad, precioUnitario * cantidad);
    }
    
    private void crearNotificacion(String estacionNombre, String inconsistencia, Usuario usuario) {
        Notificacion n = new Notificacion();
        n.setEstacionNombre(estacionNombre);
        n.setInconsistencia(inconsistencia);
        n.setEstado("Pendiente");
        n.setFecha(LocalDateTime.now());
        n.setUsuario(usuario);
        notificacionRepository.save(n);
        log.info("   ✅ Notificación: {}", inconsistencia);
    }
    
    private void crearNormativas() {
        if (normativaRepository.count() > 0) {
            log.info("⏭️ Normativas ya existen, omitiendo creación");
            return;
        }
        
        Normativa normativa1 = new Normativa();
        normativa1.setNombre("Decreto 1428 de 2025");
        normativa1.setDescripcion("Mecanismo diferencial de estabilización de precios del ACPM");
        normativa1.setFechaInicio(LocalDate.of(2025, 1, 1));
        normativa1.setFechaFin(null);
        normativa1.setActiva(true);
        normativaRepository.save(normativa1);
        log.info("   ✅ Normativa: Decreto 1428 de 2025");

        Normativa normativa2 = new Normativa();
        normativa2.setNombre("Resolución CREG 023 de 2020");
        normativa2.setDescripcion("Condiciones de calidad en distribución de combustibles");
        normativa2.setFechaInicio(LocalDate.of(2020, 6, 15));
        normativa2.setFechaFin(null);
        normativa2.setActiva(true);
        normativaRepository.save(normativa2);
        log.info("   ✅ Normativa: Resolución CREG 023 de 2020");

        Normativa normativa3 = new Normativa();
        normativa3.setNombre("Resolución 40066 de 2022");
        normativa3.setDescripcion("Regulación de precios máximos de combustibles");
        normativa3.setFechaInicio(LocalDate.of(2022, 3, 1));
        normativa3.setFechaFin(null);
        normativa3.setActiva(true);
        normativaRepository.save(normativa3);
        log.info("   ✅ Normativa: Resolución 40066 de 2022");

        Normativa normativa4 = new Normativa();
        normativa4.setNombre("Decreto 1073 de 2015");
        normativa4.setDescripcion("Decreto Único Reglamentario de Minas y Energía");
        normativa4.setFechaInicio(LocalDate.of(2015, 5, 26));
        normativa4.setFechaFin(null);
        normativa4.setActiva(true);
        normativaRepository.save(normativa4);
        log.info("   ✅ Normativa: Decreto 1073 de 2015");
    }
}