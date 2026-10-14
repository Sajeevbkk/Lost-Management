# Smart Lost and Found Management System (CampusFind Desktop)

A modern, responsive, pure **Java Swing Desktop Application** engineered to centralize, streamline, and secure the process of reporting, discovering, verifying, and recovering lost personal belongings across university campuses.

> **Note**: This application is a **100% pure Java desktop application** (No Spring Boot, no external web server required). It runs natively on the desktop with an embedded zero-configuration database and modern FlatLaf UI.

---

## 1. Key Features

- **College-Oriented Design System**: Modern desktop UI featuring the **Navy Blue (`#1E3A8A`), Purple (`#7C3AED`), and White** theme styled with FlatLaf.
- **Role-Based Desktop Security**: BCrypt password encryption for Students, Faculty/Staff, and Campus Administrators, plus 1-click demo profile switching.
- **Lost & Found Reporting**: Modal dialogs with category classification, campus location tagging, color/identifying attributes, and safe storage pickup desks.
- **Smart AI Matching Engine**: Multi-factor similarity scoring:
  - Category Hard Gate (30% Baseline)
  - Title Similarity (25% Weight, Levenshtein distance + Jaccard index)
  - Campus Location Proximity (20% Weight)
  - Chronological Closeness (15% Weight)
  - Color & Description Token Overlap (10% Weight)
  - Score Tiers: **HIGH (≥75%)**, **MEDIUM (≥60%)**, **LOW (<60%)**
- **Automated Alerts & Notifications**: Real-time notification center with live unread badge for potential matches and claim review updates.
- **Two-Tier Verification Pipeline**: Claimants furnish confidential unlisted identifying details (serial numbers, internal contents, lock screens). Physical handover audited and confirmed by administrators.
- **Physical Handover Audit Trail**: Completed returns recorded in `item_returns` table with verified student ID signoff and admin notes.
- **Interactive Dashboards**:
  - **Home Dashboard**: Live university recovery stats (Total Lost, Total Found, Successful Returns, Active Claims, Recovery Rate %), Quick Search, and Recent Items.
  - **User Dashboard**: Track reported lost items, cataloged found belongings, and active ownership claims with a visual 4-step progress stepper (`Submitted -> Under Verification -> Approved -> Handover Complete`).
  - **Admin Console**: Moderate claims, review secret proof, execute physical handovers, and browse campus user directory.

---

## 2. Technology Stack

- **UI Framework**: Java Swing (Pure Desktop, no Spring Boot)
- **Look and Feel**: FlatLaf 3.5.4 (Modern Flat UI with High-DPI support)
- **Runtime**: Java 21+ (Tested on JDK 21 and JDK 26)
- **Build Tool**: Apache Maven 3.9+
- **Persistence**: Pure JDBC with embedded H2 Database (`./data/campusfind_db.mv.db`, MySQL compatibility mode) — zero external setup required!
- **Security**: jBCrypt (0.4) for password hashing

---

## 3. Database Schema

The database consists of 6 relational tables managed via embedded JDBC:
1. `users`: Stores campus members (Student, Staff, Admin) with encrypted credentials.
2. `lost_items`: Stores missing item reports with contact info, status (`REPORTED`, `RESOLVED`, `CLOSED`).
3. `found_items`: Stores discovered items with safe storage pickup point (`AVAILABLE`, `CLAIM_PENDING`, `RETURNED`).
4. `claims`: Ownership claims with confidential proof and verification status (`PENDING`, `UNDER_VERIFICATION`, `APPROVED`, `REJECTED`, `RETURNED`).
5. `item_returns`: Immutable audit trail for physical handovers with admin signature and timestamp.
6. `notifications`: Alert records for potential matches, claim reviews, and admin updates.

---

## 4. Default Demo Accounts

The system automatically initializes seed data on startup. You can sign in using 1-click quick demo buttons in the UI or with the following credentials:

| Role | Email | Password | Campus ID |
| :--- | :--- | :--- | :--- |
| **Campus Admin** | `admin@college.edu` | `Admin@123` | `ADM-001` |
| **Student (Rahul)** | `rahul.sharma@college.edu` | `Student@123` | `CS-2024-042` |
| **Student (Priya)** | `priya.patel@college.edu` | `Student@123` | `EC-2024-118` |
| **Faculty (Dr. Anil)** | `anil.verma@college.edu` | `Staff@123` | `STF-ME-102` |
| **Student (Sneha)** | `sneha.reddy@college.edu` | `Student@123` | `IT-2024-089` |

---

## 5. How to Run the Application

### Prerequisites
- JDK 21 or higher installed
- Apache Maven 3.9+ installed

### Option A: Run directly via Maven
```powershell
mvn clean compile exec:java
```

### Option B: Build and run standalone fat JAR
```powershell
mvn clean package -DskipTests
java -jar target/campusfind-desktop-1.0.0.jar
```
