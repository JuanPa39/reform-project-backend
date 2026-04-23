package com.plataforma.combustible.service;

import com.plataforma.combustible.dto.response.ReporteZonaResponse;
import com.plataforma.combustible.dto.response.DetalleEstacionResponse;
import com.plataforma.combustible.entity.Estacion;
import com.plataforma.combustible.entity.Venta;
import com.plataforma.combustible.repository.EstacionRepository;
import com.plataforma.combustible.repository.VentaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final EstacionRepository estacionRepository;
    private final VentaRepository ventaRepository;

    public ReporteService(EstacionRepository estacionRepository, VentaRepository ventaRepository) {
        this.estacionRepository = estacionRepository;
        this.ventaRepository = ventaRepository;
    }

    public List<ReporteZonaResponse> getReporteConsumoPorZona(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Estacion> estaciones = estacionRepository.findAll();
        
        // Agrupar por zona
        Map<String, List<Estacion>> estacionesPorZona = estaciones.stream()
                .filter(e -> e.getZona() != null && !e.getZona().isEmpty())
                .collect(Collectors.groupingBy(Estacion::getZona));
        
        List<ReporteZonaResponse> reportes = new ArrayList<>();
        
        for (Map.Entry<String, List<Estacion>> entry : estacionesPorZona.entrySet()) {
            String zona = entry.getKey();
            List<Estacion> estacionesZona = entry.getValue();
            
            double totalGalonesZona = 0;
            double totalVentasZona = 0;
            List<DetalleEstacionResponse> detalles = new ArrayList<>();
            
            for (Estacion estacion : estacionesZona) {
                List<Venta> ventas = ventaRepository.findByEstacionIdAndFechaBetween(
                    estacion.getId(), fechaInicio, fechaFin);
                
                double galones = ventas.stream()
                        .mapToDouble(v -> v.getCantidad().doubleValue())
                        .sum();
                double monto = ventas.stream()
                        .mapToDouble(v -> v.getMontoTotal().doubleValue())
                        .sum();
                
                totalGalonesZona += galones;
                totalVentasZona += monto;
                
                DetalleEstacionResponse detalle = new DetalleEstacionResponse();
                detalle.setEstacionId(estacion.getId());
                detalle.setEstacionNombre(estacion.getNombre());
                detalle.setGalonesVendidos(galones);
                detalle.setMontoTotal(monto);
                detalles.add(detalle);
            }
            
            ReporteZonaResponse reporte = new ReporteZonaResponse();
            reporte.setZona(zona);
            reporte.setTotalGalones(totalGalonesZona);
            reporte.setTotalVentas(totalVentasZona);
            reporte.setDetalleEstaciones(detalles);
            reportes.add(reporte);
        }
        
        return reportes;
    }
}