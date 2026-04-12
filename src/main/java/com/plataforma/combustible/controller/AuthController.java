package com.plataforma.combustible.controller;

import com.plataforma.combustible.dto.request.LoginRequest;
import com.plataforma.combustible.dto.request.RegisterRequest;
import com.plataforma.combustible.dto.response.LoginResponse;
import com.plataforma.combustible.dto.response.MensajeResponse;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.service.AutenticacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AutenticacionService autenticacionService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AutenticacionService autenticacionService, 
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.autenticacionService = autenticacionService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(autenticacionService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<MensajeResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(autenticacionService.register(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(autenticacionService.refreshToken(refreshToken));
    }

    @GetMapping("/confirmar-cuenta")
    public ResponseEntity<MensajeResponse> confirmarCuenta(@RequestParam String token) {
        return ResponseEntity.ok(autenticacionService.confirmarCuenta(token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MensajeResponse> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) {
            MensajeResponse response = new MensajeResponse();
            response.setExito(false);
            response.setMensaje("El correo no existe");
            return ResponseEntity.badRequest().body(response);
        }
        
        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        
        MensajeResponse response = new MensajeResponse();
        response.setExito(true);
        response.setMensaje("Contraseña actualizada correctamente");
        return ResponseEntity.ok(response);
    }
}