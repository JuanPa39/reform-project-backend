-- ============================================
-- ELIMINAR TABLAS EXISTENTES (si existen)
-- ============================================
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS usuario_distribuidor CASCADE;
DROP TABLE IF EXISTS usuario_estacion CASCADE;
DROP TABLE IF EXISTS reporte CASCADE;
DROP TABLE IF EXISTS venta CASCADE;
DROP TABLE IF EXISTS regla_precio CASCADE;
DROP TABLE IF EXISTS normativa CASCADE;
DROP TABLE IF EXISTS entrega CASCADE;
DROP TABLE IF EXISTS distribuidor CASCADE;
DROP TABLE IF EXISTS inventario CASCADE;
DROP TABLE IF EXISTS estacion CASCADE;
DROP TABLE IF EXISTS vehiculo CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS combustible CASCADE;
DROP TABLE IF EXISTS tipo_vehiculo CASCADE;

-- ============================================
-- TABLA: tipo_vehiculo
-- ============================================
CREATE TABLE tipo_vehiculo (
    id SMALLINT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    activo BOOLEAN DEFAULT TRUE,
    orden_display SMALLINT DEFAULT 0
);

-- Insertar tipos de vehículo
INSERT INTO tipo_vehiculo (id, nombre, descripcion, orden_display) VALUES
(1, 'particular', 'Vehículos de uso particular', 1),
(2, 'diplomatico', 'Vehículos de cuerpo diplomático', 2),
(3, 'oficial', 'Vehículos oficiales del gobierno', 3),
(4, 'publico', 'Transporte público (buses, taxis)', 4),
(5, 'carga', 'Vehículos de carga (camiones)', 5),
(6, 'todos', 'Aplica a todos los tipos de vehículo', 0);

-- ============================================
-- TABLA: combustible
-- ============================================
CREATE TABLE combustible (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    precio_base DECIMAL(10,2) NOT NULL
);

-- ============================================
-- TABLA: usuario (CORREGIDA - ahora coincide con la entidad Java)
-- ============================================
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    rol VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT false,
    confirmation_token VARCHAR(255),
    confirmation_token_expiry TIMESTAMP,
    reset_password_token VARCHAR(255),
    reset_password_expiry TIMESTAMP
);

-- ============================================
-- TABLA: refresh_tokens (para JWT)
-- ============================================
CREATE TABLE refresh_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    usuario_id INT NOT NULL REFERENCES usuario(id),
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE
);

-- ============================================
-- TABLA: vehiculo
-- ============================================
CREATE TABLE vehiculo (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL,
    placa VARCHAR(20) UNIQUE NOT NULL,
    tipo_vehiculo_id SMALLINT NOT NULL,
    combustible_id INT NOT NULL,
    marca VARCHAR(100),
    modelo VARCHAR(100),

    CONSTRAINT fk_vehiculo_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_vehiculo_tipo
        FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo(id),
    
    CONSTRAINT fk_vehiculo_combustible
        FOREIGN KEY (combustible_id) REFERENCES combustible(id) ON DELETE RESTRICT
);

-- ============================================
-- TABLA: estacion
-- ============================================
CREATE TABLE estacion (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    nit VARCHAR(50) UNIQUE NOT NULL,
    ubicacion TEXT,
    telefono VARCHAR(20),
    horario VARCHAR(100),
    latitud DECIMAL(10,8),
    longitud DECIMAL(11,8),
    activa BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABLA: precios_combustible
-- ============================================
CREATE TABLE precios_combustible (
    id SERIAL PRIMARY KEY,
    estacion_id INT NOT NULL REFERENCES estacion(id),
    combustible_id INT NOT NULL REFERENCES combustible(id),
    precio DECIMAL(10,2) NOT NULL,
    fecha DATE NOT NULL,
    precio_regulado BOOLEAN DEFAULT FALSE
);

-- ============================================
-- TABLA: inventario
-- ============================================
CREATE TABLE inventario (
    id SERIAL PRIMARY KEY,
    estacion_id INT NOT NULL,
    combustible_id INT NOT NULL,
    cantidad_disponible DECIMAL(10,2) NOT NULL DEFAULT 0,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_estacion_combustible UNIQUE(estacion_id, combustible_id),

    FOREIGN KEY (estacion_id) REFERENCES estacion(id) ON DELETE CASCADE,
    FOREIGN KEY (combustible_id) REFERENCES combustible(id) ON DELETE CASCADE
);

-- ============================================
-- TABLA: distribuidor
-- ============================================
CREATE TABLE distribuidor (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    zona_operacion VARCHAR(100)
);

-- ============================================
-- TABLA: entrega
-- ============================================
CREATE TABLE entrega (
    id SERIAL PRIMARY KEY,
    distribuidor_id INT NOT NULL,
    estacion_id INT NOT NULL,
    combustible_id INT NOT NULL,
    cantidad_entregada DECIMAL(10,2) NOT NULL,
    fecha_entrega TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (distribuidor_id) REFERENCES distribuidor(id) ON DELETE RESTRICT,
    FOREIGN KEY (estacion_id) REFERENCES estacion(id) ON DELETE RESTRICT,
    FOREIGN KEY (combustible_id) REFERENCES combustible(id) ON DELETE RESTRICT
);

-- ============================================
-- TABLA: normativa
-- ============================================
CREATE TABLE normativa (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    aplica_subsidio BOOLEAN DEFAULT FALSE,
    porcentaje_ajuste DECIMAL(5,2),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    activa BOOLEAN DEFAULT TRUE
);

-- ============================================
-- TABLA: regla_precio
-- ============================================
CREATE TABLE regla_precio (
    id SERIAL PRIMARY KEY,
    normativa_id INT NOT NULL,
    combustible_id INT NOT NULL,
    tipo_vehiculo_id SMALLINT NOT NULL,
    porcentaje_ajuste DECIMAL(5,2),

    CONSTRAINT unique_normativa_combustible_tipo UNIQUE(normativa_id, combustible_id, tipo_vehiculo_id),

    FOREIGN KEY (normativa_id) REFERENCES normativa(id) ON DELETE CASCADE,
    FOREIGN KEY (combustible_id) REFERENCES combustible(id) ON DELETE CASCADE,
    FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo(id)
);

-- ============================================
-- TABLA: venta
-- ============================================
CREATE TABLE venta (
    id SERIAL PRIMARY KEY,
    estacion_id INT NOT NULL,
    usuario_id INT,
    vehiculo_id INT,
    combustible_id INT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    subsidio_aplicado BOOLEAN DEFAULT FALSE,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    normativa_id INT,

    FOREIGN KEY (estacion_id) REFERENCES estacion(id) ON DELETE RESTRICT,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(id) ON DELETE SET NULL,
    FOREIGN KEY (combustible_id) REFERENCES combustible(id) ON DELETE RESTRICT,
    FOREIGN KEY (normativa_id) REFERENCES normativa(id) ON DELETE SET NULL
);

-- ============================================
-- TABLA: reporte
-- ============================================
CREATE TABLE reporte (
    id SERIAL PRIMARY KEY,
    estacion_id INT NOT NULL,
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_ventas DECIMAL(12,2),
    total_combustible_vendido DECIMAL(12,2),
    detalles JSONB,

    FOREIGN KEY (estacion_id) REFERENCES estacion(id) ON DELETE CASCADE
);

-- ============================================
-- TABLA: usuario_estacion
-- ============================================
CREATE TABLE usuario_estacion (
    usuario_id INT PRIMARY KEY,
    estacion_id INT NOT NULL,

    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (estacion_id) REFERENCES estacion(id) ON DELETE CASCADE
);

-- ============================================
-- TABLA: usuario_distribuidor
-- ============================================
CREATE TABLE usuario_distribuidor (
    usuario_id INT PRIMARY KEY,
    distribuidor_id INT NOT NULL,

    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (distribuidor_id) REFERENCES distribuidor(id) ON DELETE CASCADE
);

-- ============================================
-- ÍNDICES PARA OPTIMIZAR CONSULTAS
-- ============================================
CREATE INDEX idx_vehiculo_usuario ON vehiculo(usuario_id);
CREATE INDEX idx_vehiculo_tipo ON vehiculo(tipo_vehiculo_id);
CREATE INDEX idx_venta_estacion ON venta(estacion_id);
CREATE INDEX idx_venta_fecha ON venta(fecha_venta);
CREATE INDEX idx_venta_usuario ON venta(usuario_id);
CREATE INDEX idx_regla_normativa ON regla_precio(normativa_id);
CREATE INDEX idx_inventario_estacion ON inventario(estacion_id);
CREATE INDEX idx_entrega_fecha ON entrega(fecha_entrega);
CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_refresh_token ON refresh_tokens(token);

-- ============================================
-- DATOS DE PRUEBA
-- ============================================

-- Combustibles
INSERT INTO combustible (nombre, precio_base) VALUES
('ACPM', 9500.00),
('Gasolina Corriente', 10500.00);

-- Estaciones de servicio
INSERT INTO estacion (nombre, nit, ubicacion, telefono, horario, activa) VALUES
('Estación Centro', '900123456-1', 'Calle 10 #20-30', '6012345678', 'Lun-Dom 6:00-22:00', TRUE),
('Estación Norte', '900123456-2', 'Carrera 15 #100-50', '6018765432', 'Lun-Dom 24h', TRUE);

-- Distribuidor mayorista
INSERT INTO distribuidor (nombre, zona_operacion) VALUES
('Distribuidor Nacional S.A.', 'Zona Centro');

-- Normativa: Decreto 1428/2025
INSERT INTO normativa (nombre, descripcion, aplica_subsidio, porcentaje_ajuste, fecha_inicio, fecha_fin, activa)
VALUES (
    'Decreto 1428/2025', 
    'Mecanismo diferencial de estabilización de precios del ACPM para vehículos particulares, diplomáticos y oficiales, excluyendo transporte público y carga.', 
    TRUE, 
    -5.00, 
    '2025-01-01', 
    NULL, 
    TRUE
);

-- Reglas de precio asociadas al decreto
INSERT INTO regla_precio (normativa_id, combustible_id, tipo_vehiculo_id, porcentaje_ajuste)
VALUES 
    (1, 1, 1, -5.00),
    (1, 1, 2, -5.00),
    (1, 1, 3, -5.00);

-- Usuario consumidor (password: 123456)
INSERT INTO usuario (nombre, email, contrasena, telefono, rol, enabled) 
VALUES (
    'Juan Perez', 
    'juan@email.com', 
    '$2a$10$8Un1VRYxJXQzYvQvqLbYPO6qZqXqXqXqXqXqXqXqXqXqXqXqXqXq',
    '1234567', 
    'CLIENTE',
    true
);

-- Vehículo del consumidor
INSERT INTO vehiculo (usuario_id, placa, tipo_vehiculo_id, combustible_id, marca, modelo) 
VALUES (1, 'ABC123', 1, 1, 'Toyota', 'Hilux');

-- Precios de combustible
INSERT INTO precios_combustible (estacion_id, combustible_id, precio, fecha) VALUES
(1, 1, 9500.00, CURRENT_DATE),
(1, 2, 10500.00, CURRENT_DATE),
(2, 1, 9600.00, CURRENT_DATE),
(2, 2, 10600.00, CURRENT_DATE);

-- Inventario inicial
INSERT INTO inventario (estacion_id, combustible_id, cantidad_disponible) VALUES
(1, 1, 10000.00),
(1, 2, 5000.00),
(2, 1, 8000.00),
(2, 2, 6000.00);

-- ============================================
-- VERIFICACIÓN DE DATOS
-- ============================================
SELECT COUNT(*) FROM usuario;
SELECT COUNT(*) FROM vehiculo;
SELECT COUNT(*) FROM estacion;