package com.plataforma.combustible.service;

import com.plataforma.combustible.entity.Auditoria;
import com.plataforma.combustible.repository.AuditoriaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final HttpServletRequest request;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, HttpServletRequest request) {
        this.auditoriaRepository = auditoriaRepository;
        this.request = request;
    }

    @Transactional
    public void registrar(String usuarioEmail, String accion, String detalles, String entidad, Long idEntidad) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuarioEmail(usuarioEmail);
        auditoria.setAccion(accion);
        auditoria.setDetalles(detalles);
        auditoria.setEntidad(entidad);
        auditoria.setIdEntidad(idEntidad);
        auditoria.setFecha(LocalDateTime.now());
        
        // Obtener IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        auditoria.setIpAddress(ip);
        
        auditoriaRepository.save(auditoria);
        System.out.println("✅ Auditoría registrada: " + accion + " - " + usuarioEmail);
    }

    public List<Auditoria> obtenerTodas() {
        return auditoriaRepository.findAllByOrderByFechaDesc();
    }
    
    public List<Auditoria> obtenerPorUsuario(String email) {
        return auditoriaRepository.findByUsuarioEmailOrderByFechaDesc(email);
    }
}