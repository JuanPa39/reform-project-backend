package com.plataforma.combustible.dto.response;

import com.plataforma.combustible.entity.Usuario;

public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;  
    private String rol;
    private boolean enabled;  

    public UsuarioResponse(Usuario u) {
        this.id = u.getId();
        this.nombre = u.getNombre();
        this.email = u.getEmail();
        this.telefono = u.getTelefono();  
        this.rol = u.getRol();
        this.enabled = u.isEnabled();     
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }  
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isEnabled() { return enabled; }    // ← agregar
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}