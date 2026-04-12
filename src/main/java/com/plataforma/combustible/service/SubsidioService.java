package com.plataforma.combustible.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class SubsidioService {
    
    // Reglas de subsidio según Decreto 1428/2025
    private static final Map<String, Map<String, Double>> REGLAS_SUBSIDIO = new HashMap<>();
    
    static {
        // GASOLINA CORRIENTE
        Map<String, Double> gasolinaCorriente = new HashMap<>();
        gasolinaCorriente.put("Particular", 10.0);
        gasolinaCorriente.put("Oficial", 10.0);
        gasolinaCorriente.put("Diplomático", 10.0);
        gasolinaCorriente.put("Taxi", 50.0);
        gasolinaCorriente.put("Servicio Público (Bus)", 50.0);
        gasolinaCorriente.put("Camión de carga", 50.0);
        gasolinaCorriente.put("Moto", 5.0);
        REGLAS_SUBSIDIO.put("Gasolina Corriente", gasolinaCorriente);
        
        // ACPM
        Map<String, Double> acpm = new HashMap<>();
        acpm.put("Taxi", 30.0);
        acpm.put("Servicio Público (Bus)", 30.0);
        acpm.put("Camión de carga", 30.0);
        acpm.put("Particular", 0.0);
        acpm.put("Oficial", 0.0);
        acpm.put("Diplomático", 0.0);
        acpm.put("Moto", 0.0);
        REGLAS_SUBSIDIO.put("ACPM", acpm);
        
        // GASOLINA EXTRA - NO tiene subsidio
        Map<String, Double> gasolinaExtra = new HashMap<>();
        String[] todos = {"Particular", "Oficial", "Diplomático", "Taxi", "Servicio Público (Bus)", "Camión de carga", "Moto"};
        for (String tipo : todos) {
            gasolinaExtra.put(tipo, 0.0);
        }
        REGLAS_SUBSIDIO.put("Gasolina Extra", gasolinaExtra);
    }
    
    public boolean verificarSubsidio(String tipoVehiculo, String combustible, double cantidad) {
        Map<String, Double> reglas = REGLAS_SUBSIDIO.get(combustible);
        if (reglas == null) {
            return false;
        }
        
        Double limite = reglas.get(tipoVehiculo);
        if (limite == null || limite == 0.0) {
            return false;
        }
        
        return cantidad <= limite;
    }
    
    public String getMensajeSubsidio(String tipoVehiculo, String combustible, double cantidad) {
        Map<String, Double> reglas = REGLAS_SUBSIDIO.get(combustible);
        if (reglas == null) {
            return "No aplica subsidio para " + combustible;
        }
        
        Double limite = reglas.get(tipoVehiculo);
        
        if (limite == null) {
            return "Tipo de vehículo no válido para subsidio";
        }
        
        if (limite == 0.0) {
            if (combustible.equals("ACPM")) {
                return "❌ El ACPM solo tiene subsidio para vehículos de servicio público (Taxis, Buses) y carga";
            } else if (combustible.equals("Gasolina Extra")) {
                return "❌ La Gasolina Extra no tiene subsidio según el Decreto 1428/2025";
            }
            return "❌ No aplica subsidio para " + tipoVehiculo;
        }
        
        if (cantidad <= limite) {
            return String.format("✅ APLICA subsidio: hasta %.0f galones para %s", limite, tipoVehiculo);
        } else {
            return String.format("⚠️ NO aplica subsidio: excede el límite de %.0f galones para %s", limite, tipoVehiculo);
        }
    }
}