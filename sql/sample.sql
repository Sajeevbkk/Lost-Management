-- ============================================================================
-- Smart Lost and Found Management System (CampusFind Desktop)
-- Database Initialization & Sample Data Script for MySQL 8.0+
-- ============================================================================

-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS smart_lost_found_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_lost_found_db;

-- Disable foreign key checks for clean recreation
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Drop existing tables if re-initializing
DROP TABLE IF EXISTS item_returns;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS found_items;
DROP TABLE IF EXISTS lost_items;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 3. Table Definitions
-- ============================================================================

-- Users Table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    college_email VARCHAR(120) NOT NULL UNIQUE,
    campus_id VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(80) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Lost Items Table
CREATE TABLE lost_items (
    lost_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    date_lost DATE NOT NULL,
    location_lost VARCHAR(150) NOT NULL,
    color_features VARCHAR(150) NOT NULL,
    image_url VARCHAR(255),
    contact_phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'REPORTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lost_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Found Items Table
CREATE TABLE found_items (
    found_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    date_found DATE NOT NULL,
    location_found VARCHAR(150) NOT NULL,
    color_features VARCHAR(150) NOT NULL,
    image_url VARCHAR(255),
    storage_location VARCHAR(150) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_found_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Claims Table
CREATE TABLE claims (
    claim_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    found_id BIGINT NOT NULL,
    claimant_id BIGINT NOT NULL,
    item_description_proof TEXT NOT NULL,
    unique_identifying_features TEXT NOT NULL,
    approx_date_lost DATE NOT NULL,
    approx_location_lost VARCHAR(150) NOT NULL,
    supporting_proof_details TEXT,
    status VARCHAR(30) DEFAULT 'PENDING',
    admin_remarks TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_claim_found FOREIGN KEY (found_id) REFERENCES found_items(found_id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_user FOREIGN KEY (claimant_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Item Returns Audit Trail Table (Immutable Handover Records)
CREATE TABLE item_returns (
    return_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT NOT NULL UNIQUE,
    found_id BIGINT NOT NULL,
    returned_to_user_id BIGINT NOT NULL,
    processed_by_admin_id BIGINT NOT NULL,
    handover_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    handover_notes TEXT,
    CONSTRAINT fk_return_claim FOREIGN KEY (claim_id) REFERENCES claims(claim_id) ON DELETE CASCADE,
    CONSTRAINT fk_return_found FOREIGN KEY (found_id) REFERENCES found_items(found_id) ON DELETE CASCADE,
    CONSTRAINT fk_return_claimant FOREIGN KEY (returned_to_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_return_admin FOREIGN KEY (processed_by_admin_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Notifications Table
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. Sample Seed Data
-- ============================================================================

-- A. Users
-- Passwords:
-- Admin@123   -> $2a$12$f4UALKZRglaDytjAP/SV5.x3JwASYnK/ijzh.2KuBgrn3fLjcPVV6
-- Student@123 -> $2a$12$Q6kTFR69Zx2F4DQ3XYjfH.cW7GrfvgGEsHp0U5uCqbziMMNFpS1Pa
-- Staff@123   -> $2a$12$14E0N/2Lk.0ai7uhs4MygOdepda2NSlUqeC8BjZywhS8u.srw6a4q

INSERT INTO users (user_id, full_name, college_email, campus_id, department, phone_number, password_hash, role) VALUES
(1, 'Prof. Arvind Swaminathan', 'admin@college.edu', 'ADM-001', 'Campus Security & Student Affairs', '+91 98765 00001', '$2a$12$f4UALKZRglaDytjAP/SV5.x3JwASYnK/ijzh.2KuBgrn3fLjcPVV6', 'ADMIN'),
(2, 'Rahul Sharma', 'rahul.sharma@college.edu', 'CS-2024-042', 'Computer Science & Engineering', '+91 98765 11101', '$2a$12$Q6kTFR69Zx2F4DQ3XYjfH.cW7GrfvgGEsHp0U5uCqbziMMNFpS1Pa', 'STUDENT'),
(3, 'Priya Patel', 'priya.patel@college.edu', 'EC-2024-118', 'Electronics & Communication', '+91 98765 22202', '$2a$12$Q6kTFR69Zx2F4DQ3XYjfH.cW7GrfvgGEsHp0U5uCqbziMMNFpS1Pa', 'STUDENT'),
(4, 'Dr. Anil Verma', 'anil.verma@college.edu', 'STF-ME-102', 'Mechanical Engineering', '+91 98765 33303', '$2a$12$14E0N/2Lk.0ai7uhs4MygOdepda2NSlUqeC8BjZywhS8u.srw6a4q', 'STAFF'),
(5, 'Sneha Reddy', 'sneha.reddy@college.edu', 'IT-2024-089', 'Information Technology', '+91 98765 44404', '$2a$12$Q6kTFR69Zx2F4DQ3XYjfH.cW7GrfvgGEsHp0U5uCqbziMMNFpS1Pa', 'STUDENT'),
(6, 'Amit Kumar', 'amit.kumar@college.edu', 'ME-2024-055', 'Mechanical Engineering', '+91 98765 55505', '$2a$12$Q6kTFR69Zx2F4DQ3XYjfH.cW7GrfvgGEsHp0U5uCqbziMMNFpS1Pa', 'STUDENT'),
(7, 'Prof. Meera Nair', 'meera.nair@college.edu', 'STF-CS-204', 'Computer Science', '+91 98765 66606', '$2a$12$14E0N/2Lk.0ai7uhs4MygOdepda2NSlUqeC8BjZywhS8u.srw6a4q', 'STAFF');

-- B. Lost Items
INSERT INTO lost_items (lost_id, user_id, item_name, category, description, date_lost, location_lost, color_features, image_url, contact_phone, status, created_at) VALUES
(1, 2, 'Black Leather Wallet', 'WALLETS', 'Men\'s bifold WildHorn black leather wallet. Contains college ID card and bank card.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Central Campus Library, 2nd Floor', 'Black leather with white perimeter stitching', 'wallet', '+91 98765 11101', 'REPORTED', NOW()),
(2, 3, 'College Student Identity Card', 'ID_CARD', 'Official college smart ID card in transparent acrylic card holder with blue campus lanyard.', DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'Science Block Lecture Hall 3', 'Blue university lanyard with silver clip', 'idcard', '+91 98765 22202', 'REPORTED', NOW()),
(3, 5, 'HP Wireless Bluetooth Mouse', 'ELECTRONICS', 'HP 240 silent Bluetooth mouse. Small scratch near the scroll wheel.', DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'Computer Center Lab 2', 'Matte dark grey finish', 'mouse', '+91 98765 44404', 'REPORTED', NOW()),
(4, 4, 'Casio fx-991EX Scientific Calculator', 'ELECTRONICS', 'ClassWiz scientific calculator with sliding hard cover. Has a sticker \'ME-Lab\' on the back.', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Mechanical Workshop Seminar Room', 'Black body with silver patterned front face', 'calculator', '+91 98765 33303', 'RESOLVED', NOW()),
(5, 2, 'Dell Urban Laptop Backpack', 'BAGS', 'Water-resistant navy blue Dell laptop backpack with 2 side mesh bottle pockets.', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Campus Cafeteria Food Court', 'Navy blue with reflective Dell logo strip', 'backpack', '+91 98765 11101', 'REPORTED', NOW()),
(6, 6, 'Apple AirPods Pro Wireless Case', 'ELECTRONICS', 'AirPods Pro white charging case with small red silicone ring strap attached.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Campus Sports Complex Badminton Court', 'White glossy case with red silicone strap', 'airpods', '+91 98765 55505', 'REPORTED', NOW()),
(7, 7, 'Milton Thermosteel Insulated Bottle', 'OTHER', '750ml Milton Thermosteel insulated bottle with carry handle cap.', DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'Library Main Reading Hall', 'Silver metallic stainless steel', 'bottle', '+91 98765 66606', 'REPORTED', NOW()),
(8, 5, 'Set of Room & Bike Keys', 'KEYS', 'Godrej brass door keys and Honda Activa bike ignition key with Batman keychain.', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Main Parking Lot B Near Gate 2', 'Silver & black Batman metal keychain', 'keys', '+91 98765 44404', 'RESOLVED', NOW());

-- C. Found Items
INSERT INTO found_items (found_id, user_id, item_name, category, description, date_found, location_found, color_features, image_url, storage_location, status, created_at) VALUES
(1, 3, 'Black Leather Wallet', 'WALLETS', 'Black bifold wallet picked up from study table row 4. Handed over directly to reception.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Central Campus Library, 2nd Floor', 'Black leather with perimeter white stitch pattern', 'wallet', 'Campus Security Desk - Admin Block Room 102', 'CLAIM_PENDING', NOW()),
(2, 4, 'Student Identity Card with Blue Lanyard', 'ID_CARD', 'Student smart RFID card found dropped between seating rows in science lecture hall.', DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'Science Block Lecture Hall 3', 'Blue lanyard, EC Department card inside', 'idcard', 'Student Affairs Information Helpdesk', 'AVAILABLE', NOW()),
(3, 5, 'Apple AirPods Pro 2nd Gen', 'ELECTRONICS', 'AirPods in white wireless charging case with a small red silicone ring strap attached.', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Campus Sports Complex Badminton Court', 'White case with red protective silicone strap', 'airpods', 'Campus Security Desk - Admin Block Room 102', 'AVAILABLE', NOW()),
(4, 2, 'Stainless Steel Insulated Water Bottle', 'OTHER', '750ml Milton Thermosteel insulated bottle with carry handle cap.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Library Main Reading Hall', 'Silver metallic stainless steel', 'bottle', 'Library Reception Cloakroom Counter', 'AVAILABLE', NOW()),
(5, 3, 'Set of 3 Keys with Batman Keychain', 'KEYS', 'Two brass Godrej door keys and one bike key attached to a metal Batman keychain.', DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'Main Parking Lot B Near Gate 2', 'Silver & black Batman metal keychain', 'keys', 'Main Gate Security Post 1', 'RETURNED', NOW()),
(6, 6, 'Casio ClassWiz Scientific Calculator', 'ELECTRONICS', 'Casio fx-991EX scientific calculator found on table near projector.', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Mechanical Workshop Seminar Room', 'Black with silver front face and ME sticker', 'calculator', 'Mechanical Dept Staff Room 204', 'RETURNED', NOW()),
(7, 2, 'Dark Navy Blue Laptop Sleeve', 'BAGS', 'Padded zipper laptop case for 14-inch laptops.', DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'Campus Cafeteria 1st Floor', 'Dark navy blue textured fabric', 'backpack', 'Campus Security Desk - Admin Block Room 102', 'AVAILABLE', NOW()),
(8, 4, 'Eyeglasses in Matte Black Hard Case', 'OTHER', 'Ray-Ban reading glasses in black protective clamshell case.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Main Auditorium Row G', 'Matte black case with red velvet interior', 'default', 'Main Gate Security Post 1', 'AVAILABLE', NOW());

-- D. Ownership Claims
INSERT INTO claims (claim_id, found_id, claimant_id, item_description_proof, unique_identifying_features, approx_date_lost, approx_location_lost, supporting_proof_details, status, admin_remarks, submitted_at) VALUES
(1, 1, 2, 'WildHorn brand men\'s wallet with coin pocket and driving license pocket.', 'Contains student ID card CS-2024-042 (Rahul Sharma) and an SBI Global debit card ending in digits 4102.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Central Library 2nd Floor study desk', 'I can show matching DigiLocker student ID and debit card SMS alerts on my phone.', 'UNDER_VERIFICATION', 'Claimant details match internal cards reported by security officer. Scheduled for verification at security desk.', NOW()),
(2, 5, 5, 'Set of room keys with black Batman keychain.', 'Bike ignition key is for Honda Activa with number plate ending in 8841.', DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'Parking Lot B', 'Brought bike RC smartcard matching the ignition key.', 'RETURNED', 'Identity verified via RC card. Handover completed safely.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 3, 6, 'Apple AirPods Pro 2nd Generation with MagSafe charging case.', 'Serial number ending in 9J2K. Case has a small triangular scratch on bottom right edge.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Sports Complex Badminton Hall', 'Can show Apple ID Find My device list connected on my iPhone.', 'PENDING', 'Awaiting administrative review at security office.', NOW()),
(4, 6, 4, 'Casio scientific calculator fx-991EX.', 'Has an embossed white Dymo label \'ME-Lab\' pasted on the rear sliding protective shell.', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Mechanical Workshop Seminar Room', 'Faculty identity card STF-ME-102.', 'RETURNED', 'Faculty staff member identified. Returned in person.', DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- E. Item Returns Audit Trail (Physical Handover Records)
INSERT INTO item_returns (return_id, claim_id, found_id, returned_to_user_id, processed_by_admin_id, handover_timestamp, handover_notes) VALUES
(1, 2, 5, 5, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'Keys handed over to Sneha Reddy after physically verifying student ID IT-2024-089 and Honda Activa registration plate 8841.'),
(2, 4, 6, 4, 1, DATE_SUB(NOW(), INTERVAL 12 HOUR), 'Scientific calculator returned to Dr. Anil Verma after confirming ME-Lab label and faculty staff credentials.');

-- F. Notifications
INSERT INTO notifications (notification_id, user_id, title, message, type, reference_id, is_read, created_at) VALUES
(1, 2, 'Potential Match: Black Leather Wallet', 'A found item \'Black Leather Wallet\' at \'Central Campus Library, 2nd Floor\' matches your lost report with 92% confidence score. Review your dashboard.', 'POTENTIAL_MATCH', 1, 0, NOW()),
(2, 3, 'Potential Match: College Student Identity Card', 'A found item \'Student Identity Card with Blue Lanyard\' matches your report with 88% confidence score.', 'POTENTIAL_MATCH', 2, 0, NOW()),
(3, 2, 'Claim Under Verification', 'Your claim for \'Black Leather Wallet\' is under verification with Campus Security. Please monitor for approval.', 'CLAIM_UPDATE', 1, 0, NOW()),
(4, 5, 'Handover Complete: Set of 3 Keys', 'Your keys have been successfully handed over by Campus Security. Handover logged in audit trail.', 'CLAIM_UPDATE', 2, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 6, 'Potential Match: Apple AirPods Pro', 'A discovered item \'Apple AirPods Pro 2nd Gen\' at Sports Complex matches your report with 85% confidence score.', 'POTENTIAL_MATCH', 3, 0, NOW()),
(6, 4, 'Calculator Handover Completed', 'Casio fx-991EX handed over and case closed by Admin Arvind Swaminathan.', 'CLAIM_UPDATE', 4, 1, DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- ============================================================================
-- Verification Summary
-- ============================================================================
SELECT 'Sample data initialized successfully!' AS Status;
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_lost_items FROM lost_items;
SELECT COUNT(*) AS total_found_items FROM found_items;
SELECT COUNT(*) AS total_claims FROM claims;
SELECT COUNT(*) AS total_returns FROM item_returns;
SELECT COUNT(*) AS total_notifications FROM notifications;
