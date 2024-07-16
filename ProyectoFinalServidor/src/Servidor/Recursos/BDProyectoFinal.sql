/* 
 *  Licencia Mario Raya
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  Mario
 * Created: 4 may 2024
 */

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS BDProyectoFinal;

-- Seleccionar la base de datos
USE BDProyectoFinal;

-- Crear la tabla Clientes
CREATE TABLE Clientes (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    NombreUsuario VARCHAR(255) NOT NULL,
    Contraseña VARCHAR(255) NOT NULL
);

-- Crear la tabla Piezas
CREATE TABLE Piezas (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Tipo VARCHAR(255) NOT NULL,
    Nombre VARCHAR(255) NOT NULL,
    Descripción TEXT,
    Precio DOUBLE NOT NULL,
    Imagen TEXT NOT NULL
);

-- Crear la tabla PiezasClientes
CREATE TABLE PiezasClientes (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ClienteID INT,
    PiezaID INT,
    FOREIGN KEY (PiezaID) REFERENCES Piezas(ID) ON DELETE CASCADE,
    FOREIGN KEY (ClienteID) REFERENCES Clientes(ID)
);

-- Crear la tabla PiezasVendedores
CREATE TABLE PiezasVendedores (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    VendedorID INT,
    PiezaID INT,
    FOREIGN KEY (VendedorID) REFERENCES Vendedores(ID) ON DELETE CASCADE,
    FOREIGN KEY (PiezaID) REFERENCES Piezas(ID) ON DELETE CASCADE
);

-- Crear la tabla Vendedores
CREATE TABLE Vendedores (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(255) NOT NULL,
    Contraseña VARCHAR(255) NOT NULL,
    Ubicación TEXT
);