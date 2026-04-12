package com.plataforma.combustible.dto.request;

public class NotificacionRequest {
    private Long estacionId;
    private String inconsistencia;

    public Long getEstacionId() { return estacionId; }
    public void setEstacionId(Long estacionId) { this.estacionId = estacionId; }
    
    public String getInconsistencia() { return inconsistencia; }
    public void setInconsistencia(String inconsistencia) { this.inconsistencia = inconsistencia; }
}