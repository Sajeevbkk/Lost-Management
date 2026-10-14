package com.campusfind.lostfound.dto;

import com.campusfind.lostfound.model.Role;
import com.campusfind.lostfound.model.User;
import java.time.LocalDateTime;

public class UserSummaryDto {
    private Long userId;
    private String fullName;
    private String collegeEmail;
    private String campusId;
    private String department;
    private String phoneNumber;
    private Role role;
    private LocalDateTime createdAt;

    public UserSummaryDto() {
    }

    public UserSummaryDto(User user) {
        this.userId = user.getUserId();
        this.fullName = user.getFullName();
        this.collegeEmail = user.getCollegeEmail();
        this.campusId = user.getCampusId();
        this.department = user.getDepartment();
        this.phoneNumber = user.getPhoneNumber();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
