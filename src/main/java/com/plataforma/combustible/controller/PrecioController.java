package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.PrecioRequest;
import com.plataforma.combustible.dto.response.PrecioResponse;
import com.plataforma.combustible.entity.Combustible;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.Normativa;
import com.plataforma.combustible.entity.PrecioCombustible;
import com.plataforma.combustible.entity.ReglaPrecio;
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
import com.plataforma.combustible.repository.NormativaRepository;
import com.plataforma.combustible.repository.ReglaPrecioRepository;
import com.plataforma.combustible.repository.ParametroSistemaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/precios")
public class PrecioController {
    
    // DECLARAR TODOS LOS REPOSITORIOS COMO CAMPOS
    private final PrecioCombustibleRepository precioRepository;
    private final EstacionRepository estacionRepository;
    private final CombustibleRepository combustibleRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final NormativaRepository normativaRepository;
    private final ReglaPrecioRepository reglaPrecioRepository;
    private final ParametroSistemaRepository parametroSistemaRepository;

    // Constructor corregido
    public PrecioController(PrecioCombustibleRepository precioRepository, 
                            EstacionRepository estacionRepository,
                            CombustibleRepository combustibleRepository,
                            UsuarioRepository usuarioRepository,
                            AuditoriaService auditoriaService,
                            NormativaRepository normativaRepository,
                            ReglaPrecioRepository reglaPrecioRepository,
                            ParametroSistemaRepository parametroSistemaRepository) {
        this.precioRepository = precioRepository;
        this.estacionRepository = estacionRepository;
        this.combustibleRepository = combustibleRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.normativaRepository = normativaRepository;
        this.reglaPrecioRepository = reglaPrecioRepository;
        this.parametroSistemaRepository = parametroSistemaRepository;
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
    public ResponseEntity<?> createPrecio(@RequestBody PrecioRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        Estacion estacion = estacionRepository.findById(request.getEstacionId()).orElse(null);
        if (estacion == null) {
            return ResponseEntity.badRequest().body("Estación no encontrada");
        }
        
        Combustible combustible = combustibleRepository.findByNombre(request.getTipoCombustible()).orElse(null);
        if (combustible == null) {
            return ResponseEntity.badRequest().body("Combustible no encontrado");
        }
        
        double precioOriginal = request.getPrecio();
        double precioFinal = precioOriginal;
        String normativaAplicada = "Ninguna";
        
        try {
            List<Normativa> normativasActivas = normativaRepository.findByActivaTrue();
            
            for (Normativa normativa : normativasActivas) {
                // Usar el método correcto
                List<ReglaPrecio> reglas = reglaPrecioRepository.findByNormativaIdAndCombustibleId(
                    normativa.getId(), combustible.getId());
                
                for (ReglaPrecio regla : reglas) {
                    if (regla.getPorcentajeAjuste() != null) {
                        double ajuste = precioOriginal * (regla.getPorcentajeAjuste() / 100);
                        precioFinal = precioOriginal + ajuste;
                        normativaAplicada = normativa.getNombre();
                    }
                }
            }
            
            Double precioMinimo = parametroSistemaRepository.findValorByClave("PRECIO_MINIMO_GALON")
                .map(Double::parseDouble).orElse(0.0);
            
            if (precioFinal < precioMinimo && precioMinimo > 0) {
                precioFinal = precioMinimo;
            }
            
            Double precioMaximo = parametroSistemaRepository.findValorByClave("PRECIO_MAXIMO_GALON")
                .map(Double::parseDouble).orElse(Double.MAX_VALUE);
            
            if (precioFinal > precioMaximo) {
                precioFinal = precioMaximo;
            }
            
        } catch (Exception e) {
            System.err.println("Error al aplicar normativa: " + e.getMessage());
            precioFinal = precioOriginal;
        }
        
        PrecioCombustible precio = new PrecioCombustible();
        precio.setEstacion(estacion);
        precio.setCombustible(combustible);
        precio.setPrecio(precioFinal);
        precio.setPrecioOriginal(precioOriginal);
        precio.setNormativaAplicada(normativaAplicada);
        precio.setFecha(LocalDate.now());
        precio.setPrecioRegulado(true);
        
        PrecioCombustible guardado = precioRepository.save(precio);
        
        if (usuario != null) {
            auditoriaService.registrar(
                usuario.getEmail(),
                "REGISTRO_PRECIO",
                String.format("Precio para %s en %s: $%.2f (Original: $%.2f) - Normativa: %s", 
                    combustible.getNombre(), estacion.getNombre(), precioFinal, precioOriginal, normativaAplicada),
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