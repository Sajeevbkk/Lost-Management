package com.campusfind.lostfound.model;

import java.time.LocalDateTime;

public class ItemReturn {
    private Long returnId;
    private Claim claim;
    private FoundItem foundItem;
    private User returnedToUser;
    private User processedByAdmin;
    private LocalDateTime handoverTimestamp;
    private String handoverNotes;

    public ItemReturn() {
        this.handoverTimestamp = LocalDateTime.now();
    }

    public ItemReturn(Claim claim, FoundItem foundItem, User returnedToUser, User processedByAdmin, String handoverNotes) {
        this.claim = claim;
        this.foundItem = foundItem;
        this.returnedToUser = returnedToUser;
        this.processedByAdmin = processedByAdmin;
        this.handoverNotes = handoverNotes;
        this.handoverTimestamp = LocalDateTime.now();
    }

    public Long getReturnId() {
        return returnId;
    }

    public void setReturnId(Long returnId) {
        this.returnId = returnId;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public FoundItem getFoundItem() {
        return foundItem;
    }

    public void setFoundItem(FoundItem foundItem) {
        this.foundItem = foundItem;
    }

    public User getReturnedToUser() {
        return returnedToUser;
    }

    public void setReturnedToUser(User returnedToUser) {
        this.returnedToUser = returnedToUser;
    }

    public User getProcessedByAdmin() {
        return processedByAdmin;
    }

    public void setProcessedByAdmin(User processedByAdmin) {
        this.processedByAdmin = processedByAdmin;
    }

    public LocalDateTime getHandoverTimestamp() {
        return handoverTimestamp;
    }

    public void setHandoverTimestamp(LocalDateTime handoverTimestamp) {
        this.handoverTimestamp = handoverTimestamp;
    }

    public String getHandoverNotes() {
        return handoverNotes;
    }

    public void setHandoverNotes(String handoverNotes) {
        this.handoverNotes = handoverNotes;
    }
}
