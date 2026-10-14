package com.campusfind.lostfound.dto;

import com.campusfind.lostfound.model.Role;

public class AuthResponse {
    private String token;
    private Long userId;
    private String fullName;
    private String collegeEmail;
    private String campusId;
    private String department;
    private String phoneNumber;
    private Role role;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String fullName, String collegeEmail, String campusId, String department, String phoneNumber, Role role) {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.collegeEmail = collegeEmail;
        this.campusId = campusId;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
