package com.campusfind.lostfound.dto;

import com.campusfind.lostfound.model.ClaimStatus;
import jakarta.validation.constraints.NotNull;

public class ClaimReviewRequest {
    @NotNull(message = "Review status is required")
    private ClaimStatus status;

    private String adminRemarks;

    public ClaimReviewRequest() {
    }

    public ClaimReviewRequest(ClaimStatus status, String adminRemarks) {
        this.status = status;
        this.adminRemarks = adminRemarks;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }
}
