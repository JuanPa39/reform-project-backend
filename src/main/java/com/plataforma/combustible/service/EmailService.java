package com.plataforma.combustible.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConfirmationEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Confirma tu cuenta - Plataforma Combustibles");
            message.setText("Para confirmar tu cuenta, haz clic en el siguiente enlace:\n"
                    + baseUrl + "/api/auth/confirmar-cuenta?token=" + token
                    + "\n\nEste enlace expirará en 24 horas.\n\n"
                    + "Si no solicitaste este registro, ignora este mensaje.");
            
            mailSender.send(message);
            System.out.println("Email de confirmación enviado a: " + to);
        } catch (Exception e) {
            System.err.println("Error al enviar email de confirmación a: " + to + " - " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Recuperación de contraseña - Plataforma Combustibles");
            message.setText("Para restablecer tu contraseña, haz clic en el siguiente enlace:\n"
                    + baseUrl + "/reset-password?token=" + token
                    + "\n\nEste enlace expirará en 1 hora.\n\n"
                    + "Si no solicitaste este cambio, ignora este mensaje.");
            
            mailSender.send(message);
            System.out.println("Email de recuperación enviado a: " + to);
        } catch (Exception e) {
            System.err.println("Error al enviar email de recuperación a: " + to + " - " + e.getMessage());
        }
    }
}