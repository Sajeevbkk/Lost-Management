package com.campusfind.lostfound.model;

import java.time.LocalDateTime;

public class User {
    private Long userId;
    private String fullName;
    private String collegeEmail;
    private String campusId;
    private String department;
    private String phoneNumber;
    private String passwordHash;
    private Role role = Role.STUDENT;
    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String fullName, String collegeEmail, String campusId, String department, String phoneNumber, String passwordHash, Role role) {
        this.fullName = fullName;
        this.collegeEmail = collegeEmail;
        this.campusId = campusId;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : Role.STUDENT;
        this.createdAt = LocalDateTime.now();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCollegeEmail() {
        return collegeEmail;
    }

    public void setCollegeEmail(String collegeEmail) {
        this.collegeEmail = collegeEmail;
    }

    public String getCampusId() {
        return campusId;
    }

    public void setCampusId(String campusId) {
        this.campusId = campusId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (" + campusId + " - " + role + ")";
    }
}
