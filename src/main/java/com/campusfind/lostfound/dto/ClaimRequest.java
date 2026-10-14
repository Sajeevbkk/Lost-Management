package com.campusfind.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ClaimRequest {
    @NotNull(message = "Found item ID is required")
    private Long foundId;

    @NotBlank(message = "Item description proof is required")
    private String itemDescriptionProof;

    @NotBlank(message = "Unique identifying features are required")
    private String uniqueIdentifyingFeatures;

    @NotNull(message = "Approximate date lost is required")
    private LocalDate approxDateLost;

    @NotBlank(message = "Approximate location lost is required")
    private String approxLocationLost;

    private String supportingProofDetails;

    public ClaimRequest() {
    }

    public Long getFoundId() {
        return foundId;
    }

    public void setFoundId(Long foundId) {
        this.foundId = foundId;
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
}
