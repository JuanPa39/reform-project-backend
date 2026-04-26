package com.plataforma.combustible.service;

import com.plataforma.combustible.entity.Normativa;
import com.plataforma.combustible.entity.ReglaPrecio;
import com.plataforma.combustible.repository.NormativaRepository;
import com.plataforma.combustible.repository.ReglaPrecioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AplicacionDecretoService {

    private final NormativaRepository normativaRepository;
    private final ReglaPrecioRepository reglaPrecioRepository;

    public AplicacionDecretoService(NormativaRepository normativaRepository,
                                    ReglaPrecioRepository reglaPrecioRepository) {
        this.normativaRepository = normativaRepository;
        this.reglaPrecioRepository = reglaPrecioRepository;
    }

    public double calcularPrecioConDecreto(double precioBase, Long combustibleId) {
        List<Normativa> normativas = normativaRepository.findByActivaTrue();
        double precioFinal = precioBase;
        
        for (Normativa normativa : normativas) {
            List<ReglaPrecio> reglas = reglaPrecioRepository.findByNormativaIdAndCombustibleId(
                normativa.getId(), combustibleId);
            
            for (ReglaPrecio regla : reglas) {
                if (regla.getPorcentajeAjuste() != null) {
                    precioFinal = precioBase * (1 + regla.getPorcentajeAjuste() / 100);
                }
            }
        }
        
        return precioFinal;
    }
}