-- ============================================
-- CodeCravers Canteen Food Ordering System
-- Galgotias University
-- Database Schema
-- Compatible with MySQL and TiDB Cloud
-- ============================================

CREATE DATABASE IF NOT EXISTS canteen_db;
USE canteen_db;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT','ADMIN') DEFAULT 'STUDENT',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY email (email)
);

-- Food Items Table
CREATE TABLE IF NOT EXISTS food_items (
    food_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    price DECIMAL(10,2) NOT NULL,
    available TINYINT(1) DEFAULT 1,
    PRIMARY KEY (food_id)
);

-- Orders Table
CREATE TABLE IF NOT EXISTS orders (
    order_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH','ONLINE') DEFAULT 'CASH',
    status ENUM(
        'PLACED',
        'ACCEPTED',
        'PREPARING',
        'READY',
        'COMPLETED',
        'CANCELLED'
    ) DEFAULT 'PLACED',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id),
    KEY user_id (user_id),
    CONSTRAINT orders_ibfk_1
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_item_id),
    KEY order_id (order_id),
    KEY food_id (food_id),
    CONSTRAINT order_items_ibfk_1
        FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT order_items_ibfk_2
        FOREIGN KEY (food_id) REFERENCES food_items(food_id)
);

SHOW TABLES;
