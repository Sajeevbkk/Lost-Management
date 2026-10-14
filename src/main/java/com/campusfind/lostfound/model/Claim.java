package com.campusfind.lostfound.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Claim {
    private Long claimId;
    private FoundItem foundItem;
    private User claimant;
    private String itemDescriptionProof;
    private String uniqueIdentifyingFeatures;
    private LocalDate approxDateLost;
    private String approxLocationLost;
    private String supportingProofDetails;
    private ClaimStatus status = ClaimStatus.PENDING;
    private String adminRemarks;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    public Claim() {
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ClaimStatus.PENDING;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public FoundItem getFoundItem() {
        return foundItem;
    }

    public void setFoundItem(FoundItem foundItem) {
        this.foundItem = foundItem;
    }

    public User getClaimant() {
        return claimant;
    }

    public void setClaimant(User claimant) {
        this.claimant = claimant;
    }

    public String getItemDescriptionProof() {
        return itemDescriptionProof;
    }

    public void setItemDescriptionProof(String itemDescriptionProof) {
        this.itemDescriptionProof = itemDescriptionProof;
    }

    public String getUniqueIdentifyingFeatures() {
        return uniqueIdentifyingFeatures;
    }

    public void setUniqueIdentifyingFeatures(String uniqueIdentifyingFeatures) {
        this.uniqueIdentifyingFeatures = uniqueIdentifyingFeatures;
    }

    public LocalDate getApproxDateLost() {
        return approxDateLost;
    }

    public void setApproxDateLost(LocalDate approxDateLost) {
        this.approxDateLost = approxDateLost;
    }

    public String getApproxLocationLost() {
        return approxLocationLost;
    }

    public void setApproxLocationLost(String approxLocationLost) {
        this.approxLocationLost = approxLocationLost;
    }

    public String getSupportingProofDetails() {
        return supportingProofDetails;
    }

    public void setSupportingProofDetails(String supportingProofDetails) {
        this.supportingProofDetails = supportingProofDetails;
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
