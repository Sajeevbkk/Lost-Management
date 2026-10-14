# Smart Lost and Found Management System
## System Specification & Architecture Document (Antigravity Artifact)

---

### 1. Executive Summary & Objective

The **Smart Lost and Found Management System** is a modern, responsive, full-stack campus web application engineered to centralize, streamline, and secure the process of reporting, discovering, verifying, and recovering lost personal belongings across university campuses.

#### Problem Statement
Campus community members regularly misplace essential belongings (ID cards, books, wallets, electronics, keys, bags). Traditional manual recovery channels (physical notice boards, bulletin circulars, informal social media channels) are fragmented, slow, error-prone, and lack verification safeguards.

#### Solution
A structured platform connecting students, faculty, and campus administrators featuring:
* Centralized reporting workflows for both lost and discovered belongings.
* An automated similarity-matching engine pairing lost and found records.
* A multi-stage claim verification pipeline to eliminate wrongful claims while masking sensitive personal data.
* Dedicated real-time dashboards for campus users and campus authority administrators.

---

### 2. Design System & User Interface Specifications

| Attribute | Specification |
| :--- | :--- |
| **Color Palette** | **Primary:** Navy / Royal Blue (`#1E3A8A`, `#2563EB`)<br>**Accent / Secondary:** Deep Purple / Indigo (`#6D28D9`, `#7C3AED`)<br>**Background:** Crisp Off-White / Slate Tint (`#F8FAFC`, `#FFFFFF`)<br>**Status Badges:** Emerald Green (Returned), Amber Yellow (Pending/Under Verification), Rose Red (Lost/Rejected) |
| **Typography** | Modern sans-serif stack (`Inter`, `system-ui`, `-apple-system`, `sans-serif`) |
| **Layout & Responsiveness**| Mobile-first responsive fluid grid (breakpoints: Mobile `<640px`, Tablet `640px-1024px`, Desktop `>1024px`) |
| **Component Library** | Card-based item listings with status pills, modal dialogs for claims, intuitive multi-step form wizards, accessible dashboard metric widgets |

---

### 3. System Architecture & Tech Stack

```
+-------------------------------------------------------------+
|                     Client Tier (Browser)                   |
|       HTML5 • CSS3 (Custom Responsive / Flex / Grid) • JS    |
+------------------------------+------------------------------+
                               | REST APIs (JSON / Multipart)
+------------------------------v------------------------------+
|                   Application Tier (Java)                   |
|                 Spring Boot 3.x Framework                   |
|  - Security & Auth (Spring Security / JWT / RBAC)           |
|  - REST Controllers (Users, Items, Claims, Stats)           |
|  - Service Layer & Smart Matching Engine                    |
|  - Spring Data JPA / Hibernate ORM Layer                    |
+------------------------------+------------------------------+
                               | JDBC / Parameterized Queries
+------------------------------v------------------------------+
|                     Persistence Tier                        |
|                     MySQL 8.x Database                      |
+-------------------------------------------------------------+
```

---

### 4. Database Schema (MySQL DDL Blueprint)

```sql
-- Database Initialization
CREATE DATABASE IF NOT EXISTS smart_lost_found_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_lost_found_db;

-- 1. Users Table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    college_email VARCHAR(120) NOT NULL UNIQUE,
    campus_id VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(80) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'STAFF', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Lost Items Table
CREATE TABLE lost_items (
    lost_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category ENUM('ID_CARD', 'ELECTRONICS', 'BOOKS', 'BAGS', 'WALLETS', 'KEYS', 'OTHER') NOT NULL,
    description TEXT NOT NULL,
    date_lost DATE NOT NULL,
    location_lost VARCHAR(150) NOT NULL,
    color_features VARCHAR(150) NOT NULL,
    image_url VARCHAR(255) NULL,
    contact_phone VARCHAR(20) NOT NULL,
    status ENUM('REPORTED', 'RESOLVED', 'CLOSED') DEFAULT 'REPORTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Found Items Table
CREATE TABLE found_items (
    found_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category ENUM('ID_CARD', 'ELECTRONICS', 'BOOKS', 'BAGS', 'WALLETS', 'KEYS', 'OTHER') NOT NULL,
    description TEXT NOT NULL,
    date_found DATE NOT NULL,
    location_found VARCHAR(150) NOT NULL,
    color_features VARCHAR(150) NOT NULL,
    image_url VARCHAR(255) NULL,
    storage_location VARCHAR(150) NOT NULL,
    status ENUM('AVAILABLE', 'CLAIM_PENDING', 'RETURNED') DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. Claims Table
CREATE TABLE claims (
    claim_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    found_id BIGINT NOT NULL,
    claimant_id BIGINT NOT NULL,
    item_description_proof TEXT NOT NULL,
    unique_identifying_features TEXT NOT NULL,
    approx_date_lost DATE NOT NULL,
    approx_location_lost VARCHAR(150) NOT NULL,
    supporting_proof_details TEXT NULL,
    status ENUM('PENDING', 'UNDER_VERIFICATION', 'APPROVED', 'REJECTED', 'RETURNED') DEFAULT 'PENDING',
    admin_remarks TEXT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (found_id) REFERENCES found_items(found_id) ON DELETE CASCADE,
    FOREIGN KEY (claimant_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Notifications Table
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('POTENTIAL_MATCH', 'CLAIM_UPDATE', 'ADMIN_ALERT') NOT NULL,
    reference_id BIGINT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 6. Item Returns Table
CREATE TABLE item_returns (
    return_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id BIGINT NOT NULL UNIQUE,
    found_id BIGINT NOT NULL,
    returned_to_user_id BIGINT NOT NULL,
    processed_by_admin_id BIGINT NOT NULL,
    handover_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    handover_notes TEXT NULL,
    FOREIGN KEY (claim_id) REFERENCES claims(claim_id),
    FOREIGN KEY (found_id) REFERENCES found_items(found_id),
    FOREIGN KEY (returned_to_user_id) REFERENCES users(user_id),
    FOREIGN KEY (processed_by_admin_id) REFERENCES users(user_id)
);
```

---

### 5. Smart Matching Engine Logic

When a user submits a **Lost Item** or a **Found Item**, the Matching Engine calculates a Match Confidence Score ($S_{total} \in [0, 100]$) against opposing items of the same category:

$$S_{total} = w_{cat} \cdot S_{cat} + w_{name} \cdot S_{name} + w_{loc} \cdot S_{loc} + w_{date} \cdot S_{date} + w_{desc} \cdot S_{desc}$$

#### Component Weightings & Criteria
1. **Category ($w_{cat} = 30\%$):** Hard gate. Must match identical enum type ($S_{cat} = 100$ or match fails).
2. **Item Name / Keyword Similarity ($w_{name} = 25\%$):** Tokenized Levenshtein Distance & Jaccard index similarity.
3. **Location Proximity ($w_{loc} = 20\%$):** Exact zone / building match = $100\%$, neighboring zone = $50\%$.
4. **Date Consistency ($w_{date} = 15\%$):** $\Delta t = (	ext{date\_found} - 	ext{date\_lost})$. If $0 \le \Delta t \le 3 	ext{ days} \implies 100\%$; if $4 \le \Delta t \le 10 	ext{ days} \implies 60\%$; if $\Delta t < 0$, heavily penalized.
5. **Color & Feature Attributes ($w_{desc} = 10\%$):** Keyword intersection across color/description tokens.

*Threshold:* If $S_{total} \ge 65\%$, an automated `POTENTIAL_MATCH` notification is issued to both reporter users.

---

### 6. Verification & Security Rules

* **Zero Public Leakage of Sensitive Contact Data:** Phone numbers, student registration numbers, and precise serial numbers are hidden from public view. Only safe meeting points (e.g., Campus Security Desk, Admin Office Room 102) are visible.
* **Non-Automated Ownership Confirmation:** No item is confirmed automatically based on photographic or name matching. Claimants must furnish non-public details (lock screen wallpaper, inner wallet card details, notebook markings).
* **Role-Based API Protection:** Admin dashboard and review endpoints are guarded via role checks (`hasRole('ADMIN')`).
* **Input Sanitization:** Parameterized queries via JPA Criteria / Repositories prevent SQL injection; strict request body validation guards against XSS.

---

### 7. Key REST API Endpoints Specification

| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register student or faculty member |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user, return session/token |
| `GET` | `/api/v1/items/lost` | Public | Paginated, filtered list of reported lost items |
| `POST` | `/api/v1/items/lost` | Authenticated | Report a missing item (triggers match scan) |
| `GET` | `/api/v1/items/found` | Public | Paginated, filtered list of reported found items |
| `POST` | `/api/v1/items/found` | Authenticated | Report a found item (triggers match scan) |
| `POST` | `/api/v1/claims` | Authenticated | Submit an ownership claim for a found item |
| `GET` | `/api/v1/user/dashboard` | Authenticated | Fetch active reports, matches, and claim statuses |
| `GET` | `/api/v1/admin/claims` | Admin Only | Inspect incoming ownership claims |
| `PUT` | `/api/v1/admin/claims/{id}/status`| Admin Only | Approve, reject, or mark claim as returned |
| `GET` | `/api/v1/stats/summary` | Public | Aggregated counts of lost, found, and returned items |

---

### 8. Verification Workflow Pipeline

```
[Student Reports Found Item]
             |
             v
   [Cataloged in System] <==== [Smart Matching Engine Alerts Potential Owner]
             |
             v
[Claimant Files Claim with Distinct Unlisted Identifiers]
             |
             v
[Admin Audit via Admin Dashboard]
      |                      |
      | (Insufficient Proof) | (Verified Evidence)
      v                      v
 [Claim Rejected]       [Claim Approved]
                             |
                             v
               [Physical Handover at Security Desk]
                             |
                             v
               [Status Updated: RETURNED & Logged]
```