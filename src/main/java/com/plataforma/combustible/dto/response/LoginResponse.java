package com.plataforma.combustible.dto.response;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private String email;
    private String nombre;
    private String rol;
    private boolean enabled;
    private String mensaje;

    public LoginResponse() {}

    public LoginResponse(String token, String refreshToken, String email, String nombre, String rol, boolean enabled, String mensaje) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
        this.enabled = enabled;
        this.mensaje = mensaje;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}