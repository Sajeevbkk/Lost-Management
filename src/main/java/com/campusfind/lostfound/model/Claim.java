package com.campusfind.lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private Long claimId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "found_id", nullable = false)
    private FoundItem foundItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "claimant_id", nullable = false)
    private User claimant;

    @Column(name = "item_description_proof", nullable = false, columnDefinition = "TEXT")
    private String itemDescriptionProof;

    @Column(name = "unique_identifying_features", nullable = false, columnDefinition = "TEXT")
    private String uniqueIdentifyingFeatures;

    @Column(name = "approx_date_lost", nullable = false)
    private LocalDate approxDateLost;

    @Column(name = "approx_location_lost", nullable = false, length = 150)
    private String approxLocationLost;

    @Column(name = "supporting_proof_details", columnDefinition = "TEXT")
    private String supportingProofDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ClaimStatus status = ClaimStatus.PENDING;

    @Column(name = "admin_remarks", columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ClaimStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Claim() {
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
