package com.plataforma.combustible;  // ← Este es el paquete raíz

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CombustibleApplication {
    public static void main(String[] args) {
        SpringApplication.run(CombustibleApplication.class, args);
    }
}