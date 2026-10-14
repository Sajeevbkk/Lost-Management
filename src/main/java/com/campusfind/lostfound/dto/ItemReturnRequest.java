package com.campusfind.lostfound.dto;

import jakarta.validation.constraints.NotNull;

public class ItemReturnRequest {
    @NotNull(message = "Claim ID is required")
    private Long claimId;

    private String handoverNotes;

    public ItemReturnRequest() {
    }

    public ItemReturnRequest(Long claimId, String handoverNotes) {
        this.claimId = claimId;
        this.handoverNotes = handoverNotes;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public String getHandoverNotes() {
        return handoverNotes;
    }

    public void setHandoverNotes(String handoverNotes) {
        this.handoverNotes = handoverNotes;
    }
}
