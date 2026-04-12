package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.response.NormativaResponse;
import com.plataforma.combustible.entity.Normativa;
import com.plataforma.combustible.repository.NormativaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/normativas")
public class NormativaController {

    private final NormativaRepository normativaRepository;

    public NormativaController(NormativaRepository normativaRepository) {
        this.normativaRepository = normativaRepository;
    }

    @GetMapping
    public ResponseEntity<List<NormativaResponse>> getNormativas() {
        List<Normativa> normativas = normativaRepository.findByActivaTrueOrderByFechaInicioDesc();
        
        List<NormativaResponse> response = normativas.stream().map(n -> {
            NormativaResponse resp = new NormativaResponse();
            resp.setId(n.getId());
            resp.setNombre(n.getNombre());
            resp.setDescripcion(n.getDescripcion());
            resp.setFechaInicio(n.getFechaInicio().toString());
            resp.setFechaFin(n.getFechaFin() != null ? n.getFechaFin().toString() : "Vigente");
            resp.setActiva(n.isActiva());
            return resp;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}