package com.campusfind.lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_returns")
public class ItemReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Long returnId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "found_id", nullable = false)
    private FoundItem foundItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "returned_to_user_id", nullable = false)
    private User returnedToUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "processed_by_admin_id", nullable = false)
    private User processedByAdmin;

    @Column(name = "handover_timestamp", updatable = false)
    private LocalDateTime handoverTimestamp;

    @Column(name = "handover_notes", columnDefinition = "TEXT")
    private String handoverNotes;

    @PrePersist
    protected void onCreate() {
        this.handoverTimestamp = LocalDateTime.now();
    }

    public ItemReturn() {
    }

    public ItemReturn(Claim claim, FoundItem foundItem, User returnedToUser, User processedByAdmin, String handoverNotes) {
        this.claim = claim;
        this.foundItem = foundItem;
        this.returnedToUser = returnedToUser;
        this.processedByAdmin = processedByAdmin;
        this.handoverNotes = handoverNotes;
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
