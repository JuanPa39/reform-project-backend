package com.plataforma.combustible.service;

import com.plataforma.combustible.dto.request.LoginRequest;
import com.plataforma.combustible.dto.request.RegisterRequest;
import com.plataforma.combustible.dto.response.LoginResponse;
import com.plataforma.combustible.dto.response.MensajeResponse;
import com.plataforma.combustible.entity.Usuario;
import com.plataforma.combustible.repository.UsuarioRepository;
import com.plataforma.combustible.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AutenticacionService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AuditoriaService auditoriaService;

    public AutenticacionService(UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder,
                                JwtService jwtService,
                                AuthenticationManager authenticationManager,
                                EmailService emailService,
                                AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public MensajeResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            MensajeResponse response = new MensajeResponse();
            response.setExito(false);
            response.setMensaje("El email ya está registrado");
            return response;
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setContrasena(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefono(request.getTelefono());
        
        String rol = request.getRol();
        if (rol == null || rol.isEmpty()) {
            rol = "CLIENTE";
        }
        usuario.setRol(rol);
        
        usuario.setEnabled(false);
        usuario.setConfirmationToken(UUID.randomUUID().toString());
        usuario.setConfirmationTokenExpiry(LocalDateTime.now().plusHours(24));
        
        usuarioRepository.save(usuario);
        
        // ✅ REGISTRAR EN AUDITORÍA (después de guardar)
        auditoriaService.registrar(
            usuario.getEmail(),
            "REGISTRO_USUARIO",
            String.format("Nuevo usuario registrado con rol: %s", rol),
            "Usuario",
            usuario.getId()
        );
        
        emailService.sendConfirmationEmail(usuario.getEmail(), usuario.getConfirmationToken());
        
        MensajeResponse response = new MensajeResponse();
        response.setExito(true);
        response.setMensaje("Usuario registrado. Por favor revisa tu email para confirmar tu cuenta");
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!usuario.isEnabled()) {
            LoginResponse response = new LoginResponse();
            response.setEnabled(false);
            response.setMensaje("Cuenta no confirmada. Por favor revisa tu email");
            return response;
        }
        
        String jwtToken = jwtService.generateToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);
        
        // ✅ REGISTRAR EN AUDITORÍA (después de obtener el usuario, antes del return)
        auditoriaService.registrar(
            usuario.getEmail(),
            "LOGIN",
            "Inicio de sesión exitoso",
            "Usuario",
            usuario.getId()
        );
        
        LoginResponse response = new LoginResponse();
        response.setToken(jwtToken);
        response.setRefreshToken(refreshToken);
        response.setEmail(usuario.getEmail());
        response.setNombre(usuario.getNombre());
        response.setRol(usuario.getRol());
        response.setEnabled(usuario.isEnabled());
        response.setMensaje("Login exitoso");
        return response;
    }

    @Transactional
    public MensajeResponse confirmarCuenta(String token) {
        Usuario usuario = usuarioRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));
        
        if (usuario.getConfirmationTokenExpiry().isBefore(LocalDateTime.now())) {
            MensajeResponse response = new MensajeResponse();
            response.setExito(false);
            response.setMensaje("El token ha expirado");
            return response;
        }
        
        usuario.setEnabled(true);
        usuario.setConfirmationToken(null);
        usuario.setConfirmationTokenExpiry(null);
        usuarioRepository.save(usuario);
        
        // ✅ REGISTRAR EN AUDITORÍA
        auditoriaService.registrar(
            usuario.getEmail(),
            "CONFIRMACION_EMAIL",
            "Cuenta confirmada exitosamente",
            "Usuario",
            usuario.getId()
        );
        
        MensajeResponse response = new MensajeResponse();
        response.setExito(true);
        response.setMensaje("Cuenta confirmada exitosamente");
        return response;
    }

    public LoginResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (jwtService.isTokenValid(refreshToken, usuario)) {
            String newToken = jwtService.generateToken(usuario);
            
            LoginResponse response = new LoginResponse();
            response.setToken(newToken);
            response.setRefreshToken(refreshToken);
            response.setEmail(usuario.getEmail());
            response.setNombre(usuario.getNombre());
            response.setRol(usuario.getRol());
            response.setEnabled(usuario.isEnabled());
            response.setMensaje("Token refrescado exitosamente");
            return response;
        }
        
        throw new RuntimeException("Refresh token inválido");
    }
}