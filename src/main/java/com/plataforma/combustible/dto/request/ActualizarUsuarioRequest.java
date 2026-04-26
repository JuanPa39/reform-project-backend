
package com.plataforma.combustible.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ActualizarUsuarioRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Email(message = "Email inválido")
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    private String telefono;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}