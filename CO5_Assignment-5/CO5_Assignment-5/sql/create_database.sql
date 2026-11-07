-- Local TEST database created only for Assignment 5 (never use real data or a real server).
CREATE DATABASE IF NOT EXISTS transport_test;
USE transport_test;

-- B2: users table for the login / SQL-injection experiment
CREATE TABLE IF NOT EXISTS users (
    id  VARCHAR(30) PRIMARY KEY,
    pwd VARCHAR(60) NOT NULL
) ENGINE=InnoDB;

-- B3 / B4: bus-pass table
CREATE TABLE IF NOT EXISTS bus_pass (
    pass_id  VARCHAR(20)   PRIMARY KEY,
    reg_no   VARCHAR(15)   NOT NULL,
    name     VARCHAR(60)   NOT NULL,
    route_no INT           NOT NULL,
    amount   DECIMAL(10,2) NOT NULL,
    INDEX idx_route (route_no)
) ENGINE=InnoDB;

-- Optional: create a dedicated user (run as root)
-- CREATE USER 'tester'@'localhost' IDENTIFIED BY 'test123';
-- GRANT ALL ON transport_test.* TO 'tester'@'localhost';
