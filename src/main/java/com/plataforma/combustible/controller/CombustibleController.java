package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.response.CombustibleResponse;
import com.plataforma.combustible.entity.Combustible;
import com.plataforma.combustible.repository.CombustibleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/combustibles")
public class CombustibleController {

    private final CombustibleRepository combustibleRepository;

    public CombustibleController(CombustibleRepository combustibleRepository) {
        this.combustibleRepository = combustibleRepository;
    }

    @GetMapping
    public ResponseEntity<List<CombustibleResponse>> getAllCombustibles() {
        List<CombustibleResponse> response = combustibleRepository.findAll().stream()
            .map(c -> new CombustibleResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.isActivo()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}