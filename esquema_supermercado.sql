CREATE DATABASE "baseDatosProductos";

\c "baseDatosProductos"

CREATE TABLE trabajador (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    ci VARCHAR(20) UNIQUE NOT NULL,
    edad INT NOT NULL,
    direccion_vivienda VARCHAR(200),
    salario_por_minuto NUMERIC(10,4) NOT NULL,
    salario_por_mes NUMERIC(10,2) NOT NULL,
    fecha_nacimiento DATE NOT NULL
);

CREATE TABLE cajero (
    id SERIAL PRIMARY KEY,
    id_trabajador INT REFERENCES trabajador(id),
    serial_cajero VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE cliente (
    id SERIAL PRIMARY KEY,
    nit VARCHAR(30) UNIQUE NOT NULL,
    nombre_cliente VARCHAR(150) NOT NULL
);

CREATE TABLE producto (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    precio NUMERIC(10,2) NOT NULL,
    codigo VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE venta (
    id SERIAL PRIMARY KEY,
    precio_total NUMERIC(10,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    nit_cliente VARCHAR(30) NOT NULL,
    id_cajero INT REFERENCES cajero(id),
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detalle_compra (
    id SERIAL PRIMARY KEY,
    id_venta INT REFERENCES venta(id) ON DELETE CASCADE,
    cantidad INT NOT NULL,
    producto VARCHAR(150) NOT NULL,
    precio_producto NUMERIC(10,2) NOT NULL,
    sub_precio NUMERIC(10,2) NOT NULL
);

CREATE TABLE factura (
    id SERIAL PRIMARY KEY,
    id_venta INT REFERENCES venta(id),
    numero_factura VARCHAR(50) NOT NULL,
    cuf VARCHAR(120) NOT NULL,
    estado_impuestos VARCHAR(30) DEFAULT 'VALIDADA',
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO trabajador (nombre, apellido, ci, edad, direccion_vivienda, salario_por_minuto, salario_por_mes, fecha_nacimiento)
VALUES ('Rodolfo', 'Mendez', '8945123', 26, 'Av. Radial 12, Santa Cruz', 0.2291, 3300.00, '2000-05-14');

INSERT INTO cajero (id_trabajador, serial_cajero)
VALUES (1, '00132');

INSERT INTO cliente (nit, nombre_cliente) VALUES
('1020304015', 'Carlos Rojas'),
('4567890012', 'Maria Fernanda Gomez'),
('99002', 'SIN NOMBRE');

INSERT INTO producto (nombre, precio, codigo) VALUES
('Leche Pil 1L', 7.50, '1001'),
('Pan de Batalla (Bolsa 6u)', 5.00, '1002'),
('Aceite Fino 900ml', 14.50, '1003'),
('Arroz Grano de Oro 1kg', 9.00, '1004'),
('Coca Cola 2L', 13.00, '1005'),
('Pollo Entero Sofia (kg)', 16.50, '1006');
