package com.campusfind.lostfound.config;

import com.campusfind.lostfound.util.PasswordUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;

public class DatabaseManager {

    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/smart_lost_found_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASS = "password";

    private static String activeUrl = DEFAULT_DB_URL;
    private static String activeUser = DEFAULT_DB_USER;
    private static String activePass = DEFAULT_DB_PASS;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
            } catch (Exception e) {
                System.err.println("Could not load db.properties: " + e.getMessage());
            }
        } else {
            try (InputStream is = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) props.load(is);
            } catch (Exception ignored) {
            }
        }

        activeUrl = props.getProperty("db.url", DEFAULT_DB_URL);
        activeUser = props.getProperty("db.user", DEFAULT_DB_USER);
        activePass = props.getProperty("db.password", DEFAULT_DB_PASS);
    }

    public static synchronized Connection getConnection() throws SQLException {
        return DriverManager.getConnection(activeUrl, activeUser, activePass);
    }

    public static synchronized void initialize() {
        try {
            // Register MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Connecting to MySQL Database at: " + activeUrl);

            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                // 1. Users Table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        full_name VARCHAR(100) NOT NULL,
                        college_email VARCHAR(120) NOT NULL UNIQUE,
                        campus_id VARCHAR(50) NOT NULL UNIQUE,
                        department VARCHAR(80) NOT NULL,
                        phone_number VARCHAR(20) NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(20) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                // 2. Lost Items Table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS lost_items (
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
                        FOREIGN KEY (user_id) REFERENCES users(user_id)
                    );
                """);

                // 3. Found Items Table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS found_items (
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
                        FOREIGN KEY (user_id) REFERENCES users(user_id)
                    );
                """);

                // 4. Claims Table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS claims (
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
                        FOREIGN KEY (found_id) REFERENCES found_items(found_id),
                        FOREIGN KEY (claimant_id) REFERENCES users(user_id)
                    );
                """);

                // 5. Item Returns Audit Trail Table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS item_returns (
                        return_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        claim_id BIGINT NOT NULL UNIQUE,
                        found_id BIGINT NOT NULL,
                        returned_to_user_id BIGINT NOT NULL,
                        processed_by_admin_id BIGINT NOT NULL,
                        handover_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        handover_notes TEXT,
                        FOREIGN KEY (claim_id) REFERENCES claims(claim_id),
                        FOREIGN KEY (found_id) REFERENCES found_items(found_id),
                        FOREIGN KEY (returned_to_user_id) REFERENCES users(user_id),
                        FOREIGN KEY (processed_by_admin_id) REFERENCES users(user_id)
                    );
                """);

                // 6. Notifications Table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        title VARCHAR(150) NOT NULL,
                        message TEXT NOT NULL,
                        type VARCHAR(30) NOT NULL,
                        reference_id BIGINT,
                        is_read BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(user_id)
                    );
                """);
            }

            // Seed demo data if database is empty
            seedInitialDataIfEmpty();

        } catch (Exception e) {
            System.err.println("MySQL database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialDataIfEmpty() {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Data already exists
                }
            }

            System.out.println(">>> Seeding MySQL database with demo campus accounts and records...");

            // Insert Users
            String insertUserSql = "INSERT INTO users (full_name, college_email, campus_id, department, phone_number, password_hash, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                // Admin
                ps.setString(1, "Prof. Arvind Swaminathan");
                ps.setString(2, "admin@college.edu");
                ps.setString(3, "ADM-001");
                ps.setString(4, "Campus Security & Student Affairs");
                ps.setString(5, "+91 98765 00001");
                ps.setString(6, PasswordUtil.hashPassword("Admin@123"));
                ps.setString(7, "ADMIN");
                ps.executeUpdate();

                // Rahul Sharma (Student)
                ps.setString(1, "Rahul Sharma");
                ps.setString(2, "rahul.sharma@college.edu");
                ps.setString(3, "CS-2024-042");
                ps.setString(4, "Computer Science & Engineering");
                ps.setString(5, "+91 98765 11101");
                ps.setString(6, PasswordUtil.hashPassword("Student@123"));
                ps.setString(7, "STUDENT");
                ps.executeUpdate();

                // Priya Patel (Student)
                ps.setString(1, "Priya Patel");
                ps.setString(2, "priya.patel@college.edu");
                ps.setString(3, "EC-2024-118");
                ps.setString(4, "Electronics & Communication");
                ps.setString(5, "+91 98765 22202");
                ps.setString(6, PasswordUtil.hashPassword("Student@123"));
                ps.setString(7, "STUDENT");
                ps.executeUpdate();

                // Dr. Anil Verma (Staff)
                ps.setString(1, "Dr. Anil Verma");
                ps.setString(2, "anil.verma@college.edu");
                ps.setString(3, "STF-ME-102");
                ps.setString(4, "Mechanical Engineering");
                ps.setString(5, "+91 98765 33303");
                ps.setString(6, PasswordUtil.hashPassword("Staff@123"));
                ps.setString(7, "STAFF");
                ps.executeUpdate();

                // Sneha Reddy (Student)
                ps.setString(1, "Sneha Reddy");
                ps.setString(2, "sneha.reddy@college.edu");
                ps.setString(3, "IT-2024-089");
                ps.setString(4, "Information Technology");
                ps.setString(5, "+91 98765 44404");
                ps.setString(6, PasswordUtil.hashPassword("Student@123"));
                ps.setString(7, "STUDENT");
                ps.executeUpdate();
            }

            LocalDate today = LocalDate.now();

            // Insert Lost Items
            String insertLostSql = "INSERT INTO lost_items (user_id, item_name, category, description, date_lost, location_lost, color_features, image_url, contact_phone, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertLostSql)) {
                // Lost 1 (Rahul)
                ps.setLong(1, 2); // Rahul
                ps.setString(2, "Black Leather Wallet");
                ps.setString(3, "WALLETS");
                ps.setString(4, "Men's bifold WildHorn black leather wallet. Contains college ID card and bank card.");
                ps.setDate(5, Date.valueOf(today.minusDays(2)));
                ps.setString(6, "Central Campus Library, 2nd Floor");
                ps.setString(7, "Black leather with white stitching along edges");
                ps.setString(8, "wallet");
                ps.setString(9, "+91 98765 11101");
                ps.setString(10, "REPORTED");
                ps.executeUpdate();

                // Lost 2 (Priya)
                ps.setLong(1, 3); // Priya
                ps.setString(2, "College Student Identity Card");
                ps.setString(3, "ID_CARD");
                ps.setString(4, "Official college smart ID card in transparent acrylic card holder with blue campus lanyard.");
                ps.setDate(5, Date.valueOf(today.minusDays(3)));
                ps.setString(6, "Science Block Lecture Hall 3");
                ps.setString(7, "Blue university lanyard with silver clip");
                ps.setString(8, "idcard");
                ps.setString(9, "+91 98765 22202");
                ps.setString(10, "REPORTED");
                ps.executeUpdate();

                // Lost 3 (Sneha)
                ps.setLong(1, 5); // Sneha
                ps.setString(2, "HP Wireless Bluetooth Mouse");
                ps.setString(3, "ELECTRONICS");
                ps.setString(4, "HP 240 silent Bluetooth mouse. Small scratch near the scroll wheel.");
                ps.setDate(5, Date.valueOf(today.minusDays(4)));
                ps.setString(6, "Computer Center Lab 2");
                ps.setString(7, "Matte dark grey finish");
                ps.setString(8, "mouse");
                ps.setString(9, "+91 98765 44404");
                ps.setString(10, "REPORTED");
                ps.executeUpdate();

                // Lost 4 (Dr. Anil)
                ps.setLong(1, 4); // Anil
                ps.setString(2, "Casio fx-991EX Scientific Calculator");
                ps.setString(3, "ELECTRONICS");
                ps.setString(4, "ClassWiz scientific calculator with sliding hard cover. Has a sticker 'ME-Lab' on the back.");
                ps.setDate(5, Date.valueOf(today.minusDays(1)));
                ps.setString(6, "Mechanical Workshop Seminar Room");
                ps.setString(7, "Black body with silver patterned front face");
                ps.setString(8, "calculator");
                ps.setString(9, "+91 98765 33303");
                ps.setString(10, "REPORTED");
                ps.executeUpdate();

                // Lost 5 (Rahul)
                ps.setLong(1, 2); // Rahul
                ps.setString(2, "Dell Urban Backpack");
                ps.setString(3, "BAGS");
                ps.setString(4, "Water-resistant navy blue Dell laptop backpack with 2 side mesh bottle pockets.");
                ps.setDate(5, Date.valueOf(today.minusDays(5)));
                ps.setString(6, "Campus Cafeteria Food Court");
                ps.setString(7, "Navy blue with reflective Dell logo strip");
                ps.setString(8, "backpack");
                ps.setString(9, "+91 98765 11101");
                ps.setString(10, "REPORTED");
                ps.executeUpdate();
            }

            // Insert Found Items
            String insertFoundSql = "INSERT INTO found_items (user_id, item_name, category, description, date_found, location_found, color_features, image_url, storage_location, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertFoundSql)) {
                // Found 1 (Priya found wallet)
                ps.setLong(1, 3); // Priya
                ps.setString(2, "Black Leather Wallet");
                ps.setString(3, "WALLETS");
                ps.setString(4, "Black bifold wallet picked up from study table row 4. Handed over directly to reception.");
                ps.setDate(5, Date.valueOf(today.minusDays(2)));
                ps.setString(6, "Central Campus Library, 2nd Floor");
                ps.setString(7, "Black leather with perimeter white stitch pattern");
                ps.setString(8, "wallet");
                ps.setString(9, "Campus Security Desk - Admin Block Room 102");
                ps.setString(10, "CLAIM_PENDING");
                ps.executeUpdate();

                // Found 2 (Dr. Anil found ID Card)
                ps.setLong(1, 4); // Anil
                ps.setString(2, "Student Identity Card with Blue Lanyard");
                ps.setString(3, "ID_CARD");
                ps.setString(4, "Student smart RFID card found dropped between seating rows in science lecture hall.");
                ps.setDate(5, Date.valueOf(today.minusDays(3)));
                ps.setString(6, "Science Block Lecture Hall 3");
                ps.setString(7, "Blue lanyard, EC Department card inside");
                ps.setString(8, "idcard");
                ps.setString(9, "Student Affairs Information Helpdesk");
                ps.setString(10, "AVAILABLE");
                ps.executeUpdate();

                // Found 3 (Sneha found AirPods)
                ps.setLong(1, 5); // Sneha
                ps.setString(2, "Apple AirPods Pro 2nd Gen");
                ps.setString(3, "ELECTRONICS");
                ps.setString(4, "AirPods in white wireless charging case with a small red silicone ring strap attached.");
                ps.setDate(5, Date.valueOf(today.minusDays(1)));
                ps.setString(6, "Campus Sports Complex Badminton Court");
                ps.setString(7, "White case with red protective silicone case");
                ps.setString(8, "airpods");
                ps.setString(9, "Campus Security Desk - Admin Block Room 102");
                ps.setString(10, "AVAILABLE");
                ps.executeUpdate();

                // Found 4 (Rahul found Bottle)
                ps.setLong(1, 2); // Rahul
                ps.setString(2, "Stainless Steel Insulated Water Bottle");
                ps.setString(3, "OTHER");
                ps.setString(4, "750ml Milton Thermosteel insulated bottle with carry handle cap.");
                ps.setDate(5, Date.valueOf(today.minusDays(2)));
                ps.setString(6, "Library Main Reading Hall");
                ps.setString(7, "Silver metallic stainless steel");
                ps.setString(8, "bottle");
                ps.setString(9, "Library Reception Cloakroom Counter");
                ps.setString(10, "AVAILABLE");
                ps.executeUpdate();

                // Found 5 (Priya found Keys - Returned)
                ps.setLong(1, 3); // Priya
                ps.setString(2, "Set of 3 Keys with Batman Keychain");
                ps.setString(3, "KEYS");
                ps.setString(4, "Two brass Godrej door keys and one bike key attached to a metal Batman keychain.");
                ps.setDate(5, Date.valueOf(today.minusDays(4)));
                ps.setString(6, "Main Parking Lot B Near Gate 2");
                ps.setString(7, "Silver & black Batman metal keychain");
                ps.setString(8, "keys");
                ps.setString(9, "Main Gate Security Post 1");
                ps.setString(10, "RETURNED");
                ps.executeUpdate();
            }

            // Insert Sample Claims
            String insertClaimSql = "INSERT INTO claims (found_id, claimant_id, item_description_proof, unique_identifying_features, approx_date_lost, approx_location_lost, supporting_proof_details, status, admin_remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertClaimSql)) {
                // Claim 1: Rahul claiming found wallet #1
                ps.setLong(1, 1);
                ps.setLong(2, 2);
                ps.setString(3, "WildHorn brand men's wallet with coin pocket and driving license pocket.");
                ps.setString(4, "Contains student ID card CS-2024-042 (Rahul Sharma) and an SBI Global debit card ending in digits 4102.");
                ps.setDate(5, Date.valueOf(today.minusDays(2)));
                ps.setString(6, "Central Library 2nd Floor study desk");
                ps.setString(7, "I can show matching DigiLocker student ID and debit card SMS alerts on my phone.");
                ps.setString(8, "UNDER_VERIFICATION");
                ps.setString(9, "Claimant details match internal cards reported by security officer. Scheduled for verification at security desk.");
                ps.executeUpdate();

                // Claim 2: Sneha claimed keys #5 (Completed return)
                ps.setLong(1, 5);
                ps.setLong(2, 5);
                ps.setString(3, "Set of room keys with black Batman keychain.");
                ps.setString(4, "Bike ignition key is for Honda Activa with number plate ending in 8841.");
                ps.setDate(5, Date.valueOf(today.minusDays(4)));
                ps.setString(6, "Parking Lot B");
                ps.setString(7, "Brought bike RC smartcard matching the ignition key.");
                ps.setString(8, "RETURNED");
                ps.setString(9, "Identity verified via RC card. Handover completed safely.");
                ps.executeUpdate();
            }

            // Insert Item Return Record
            String insertReturnSql = "INSERT INTO item_returns (claim_id, found_id, returned_to_user_id, processed_by_admin_id, handover_notes) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertReturnSql)) {
                ps.setLong(1, 2); // Claim 2
                ps.setLong(2, 5); // Found 5
                ps.setLong(3, 5); // Sneha
                ps.setLong(4, 1); // Admin
                ps.setString(5, "Keys handed over to Sneha Reddy after verifying Honda Activa registration plate 8841.");
                ps.executeUpdate();
            }

            // Insert Notifications
            String insertNotifSql = "INSERT INTO notifications (user_id, title, message, type, reference_id, is_read) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertNotifSql)) {
                // Notif 1 (Rahul)
                ps.setLong(1, 2);
                ps.setString(2, "Potential Match: Black Leather Wallet");
                ps.setString(3, "A found item 'Black Leather Wallet' at 'Central Campus Library, 2nd Floor' matches your lost item with 92% confidence score. Review your dashboard.");
                ps.setString(4, "POTENTIAL_MATCH");
                ps.setLong(5, 1);
                ps.setBoolean(6, false);
                ps.executeUpdate();

                // Notif 2 (Priya)
                ps.setLong(1, 3);
                ps.setString(2, "Potential Match: College Student Identity Card");
                ps.setString(3, "A found item 'Student Identity Card with Blue Lanyard' matches your report with 88% confidence score.");
                ps.setString(4, "POTENTIAL_MATCH");
                ps.setLong(5, 2);
                ps.setBoolean(6, false);
                ps.executeUpdate();

                // Notif 3 (Rahul)
                ps.setLong(1, 2);
                ps.setString(2, "Claim Under Verification");
                ps.setString(3, "Your claim for 'Black Leather Wallet' is under verification with Campus Security. Please monitor for approval.");
                ps.setString(4, "CLAIM_UPDATE");
                ps.setLong(5, 1);
                ps.setBoolean(6, false);
                ps.executeUpdate();
            }

            System.out.println(">>> MySQL CampusFind demo database seeded successfully!");

        } catch (Exception e) {
            System.err.println("Error seeding demo data in MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
