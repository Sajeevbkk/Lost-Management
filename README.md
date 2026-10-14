# Smart Lost and Found Management System (CampusFind)

A modern, responsive, full-stack campus web application engineered to centralize, streamline, and secure the process of reporting, discovering, verifying, and recovering lost personal belongings across university campuses.

---

## 1. Key Features

- **College-Oriented Design System**: Clean responsive layout featuring a **Navy Blue, Purple, and White** color theme.
- **Role-Based Security**: Secure JWT authentication and BCrypt password encryption for Students, Faculty/Staff, and Campus Administrators.
- **Lost & Found Reporting**: Intuitive forms with file upload support, category classification, campus location tagging, and safe collection desks.
- **Smart AI Matching Engine**: Multi-factor similarity scoring (Category hard gate, Levenshtein title distance, campus proximity, date consistency, and color/feature token overlap).
- **Automated Alerts & Notifications**: Real-time in-app alerts sent when similarity score exceeds 60% or when ownership claims are reviewed.
- **Two-Tier Verification Pipeline**: Claimants furnish confidential unlisted identifying details (serial numbers, internal contents, lock screens). No automated release; physical handover audited by administrators.
- **Physical Handover Audit Trail**: Completed returns logged in MySQL `item_returns` table with verified student ID signoff and admin notes.
- **Interactive Dashboards**:
  - **User Dashboard**: Track reported lost items, cataloged found belongings, submitted claims progress stepper, personal matches, and notification inbox.
  - **Admin Console**: Moderate reports, review claims with secret evidence, conduct physical handovers, and browse campus user directory.

---

## 2. Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.5
- **ORM / Persistence**: Spring Data JPA, Hibernate, MySQL Connector/J
- **Security**: Spring Security 6, JJWT (io.jsonwebtoken 0.12.6), BCrypt
- **Database**: MySQL 8.0 (`smart_lost_found_db`)
- **Frontend**: Responsive HTML5, CSS3 (Custom Variables, Flexbox, Grid), JavaScript (Vanilla ES6+ Fetch API, Single-Page routing)

---

## 3. Database Schema (`smart_lost_found_db`)

The database consists of 6 relational tables:
1. `users`: Stores campus members (Student, Staff, Admin) with encrypted credentials.
2. `lost_items`: Stores missing item reports with contact info, status (`REPORTED`, `RESOLVED`, `CLOSED`).
3. `found_items`: Stores discovered items with safe storage pickup point (`AVAILABLE`, `CLAIM_PENDING`, `RETURNED`).
4. `claims`: Ownership claims with confidential proof and verification status (`PENDING`, `UNDER_VERIFICATION`, `APPROVED`, `REJECTED`, `RETURNED`).
5. `notifications`: Alert records for potential matches, claim reviews, and admin updates.
6. `item_returns`: Immutable audit trail for physical handovers with admin signature and timestamp.

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
- MySQL 8.0 running on `localhost:3306` with database `smart_lost_found_db`

### Start Application
```powershell
mvn spring-boot:run
```

The web application will be accessible at:
```
http://localhost:8080
```

---

## 6. REST API Endpoints Overview

| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register student or faculty member |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user, receive JWT token |
| `GET` | `/api/v1/items/lost` | Public | Search and filter lost items |
| `POST` | `/api/v1/items/lost` | Authenticated | Report missing item (triggers match scan) |
| `GET` | `/api/v1/items/found` | Public | Search and filter found items |
| `POST` | `/api/v1/items/found` | Authenticated | Report found item (triggers match scan) |
| `POST` | `/api/v1/claims` | Authenticated | Submit ownership claim with secret proof |
| `GET` | `/api/v1/claims/my` | Authenticated | View submitted claims & recovery progress |
| `GET` | `/api/v1/matches/my` | Authenticated | View matches for user's reports |
| `GET` | `/api/v1/matches/public` | Public | View active campus matches |
| `GET` | `/api/v1/notifications` | Authenticated | Fetch notifications and alerts |
| `GET` | `/api/v1/admin/claims` | Admin Only | Inspect incoming ownership claims |
| `PUT` | `/api/v1/admin/claims/{id}/review` | Admin Only | Approve, reject, or verify claims |
| `POST` | `/api/v1/admin/returns` | Admin Only | Log physical handover and close case |
| `GET` | `/api/v1/admin/users` | Admin Only | Manage campus user accounts |
| `GET` | `/api/v1/stats/summary` | Public | Aggregated counts of lost, found, returned |
