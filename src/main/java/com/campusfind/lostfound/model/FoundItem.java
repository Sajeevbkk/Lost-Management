package com.campusfind.lostfound.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FoundItem {
    private Long foundId;
    private User user;
    private String itemName;
    private Category category;
    private String description;
    private LocalDate dateFound;
    private String locationFound;
    private String colorFeatures;
    private String imageUrl;
    private String storageLocation;
    private FoundStatus status = FoundStatus.AVAILABLE;
    private LocalDateTime createdAt;

    public FoundItem() {
        this.createdAt = LocalDateTime.now();
        this.status = FoundStatus.AVAILABLE;
    }

    public Long getFoundId() {
        return foundId;
    }

    public void setFoundId(Long foundId) {
        this.foundId = foundId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateFound() {
        return dateFound;
    }

    public void setDateFound(LocalDate dateFound) {
        this.dateFound = dateFound;
    }

    public String getLocationFound() {
        return locationFound;
    }

    public void setLocationFound(String locationFound) {
        this.locationFound = locationFound;
    }

    public String getColorFeatures() {
        return colorFeatures;
    }

    public void setColorFeatures(String colorFeatures) {
        this.colorFeatures = colorFeatures;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public FoundStatus getStatus() {
        return status;
    }

    public void setStatus(FoundStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return itemName + " [" + (category != null ? category.name() : "") + "]";
    }
}
