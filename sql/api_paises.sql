-- Taller: Consumo de APIs Públicas en Java con MySQL (patrón MVC)
-- La aplicación también ejecuta esto sola al iniciar (Conexion.inicializar()),
-- pero puedes correrlo a mano en MySQL Workbench / HeidiSQL / consola.

CREATE DATABASE IF NOT EXISTS api_paises
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE api_paises;

CREATE TABLE IF NOT EXISTS pais(
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    capital VARCHAR(100),
    region VARCHAR(100),
    poblacion BIGINT,
    moneda VARCHAR(100)
) DEFAULT CHARSET = utf8mb4;
