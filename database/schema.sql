-- JanDrishti MySQL Database Schema
-- Database: jandrishti

CREATE DATABASE IF NOT EXISTS jandrishti;
USE jandrishti;

SET FOREIGN_KEY_CHECKS = 0;

-- Clean drop existing tables
DROP TABLE IF EXISTS issue_status_history;
DROP TABLE IF EXISTS issue_assignments;
DROP TABLE IF EXISTS issues;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(20),
    role ENUM('CITIZEN', 'OFFICER', 'ADMIN') NOT NULL DEFAULT 'CITIZEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Locations Table
CREATE TABLE locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    state_name VARCHAR(100) NOT NULL,
    district_name VARCHAR(100) NOT NULL,
    city_name VARCHAR(100) NOT NULL,
    area_name VARCHAR(150) NOT NULL,
    road_name VARCHAR(180) NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_location UNIQUE (state_name, district_name, city_name, area_name, road_name)
);

-- 3. Issues Table
CREATE TABLE issues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location_id BIGINT NOT NULL,
    reporter_id BIGINT,
    category VARCHAR(100) NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'Reported',
    department VARCHAR(120) DEFAULT 'Public Works Dept (PWD)',
    assigned_officer VARCHAR(120),
    citizen_verified BOOLEAN NOT NULL DEFAULT FALSE,
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_issues_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE,
    CONSTRAINT fk_issues_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 4. Issue Status History Table
CREATE TABLE issue_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by VARCHAR(120) NOT NULL,
    remarks VARCHAR(255),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX ix_issues_status ON issues(status);
CREATE INDEX ix_issues_priority ON issues(priority);
CREATE INDEX ix_issues_category ON issues(category);
CREATE INDEX ix_issues_location ON issues(location_id);
CREATE INDEX ix_history_issue ON issue_status_history(issue_id);

-- Operational View: Hotspots Summary
CREATE OR REPLACE VIEW hotspot_summary AS
SELECT l.state_name, l.district_name, l.city_name, l.area_name,
       COUNT(i.id) AS issue_count,
       SUM(i.status <> 'Resolved') AS open_count
FROM locations l
JOIN issues i ON i.location_id = l.id
GROUP BY l.state_name, l.district_name, l.city_name, l.area_name
ORDER BY open_count DESC, issue_count DESC;

SET FOREIGN_KEY_CHECKS = 1;
