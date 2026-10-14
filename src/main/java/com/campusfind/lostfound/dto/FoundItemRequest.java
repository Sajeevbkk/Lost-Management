package com.campusfind.lostfound.dto;

import com.campusfind.lostfound.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class FoundItemRequest {
    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Item category is required")
    private Category category;

    @NotBlank(message = "Item description is required")
    private String description;

    @NotNull(message = "Date found is required")
    private LocalDate dateFound;

    @NotBlank(message = "Location found is required")
    private String locationFound;

    @NotBlank(message = "Color and identifying features are required")
    private String colorFeatures;

    private String imageUrl;

    @NotBlank(message = "Current safe storage location is required")
    private String storageLocation;

    public FoundItemRequest() {
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

    public String locationFound() {
        return locationFound;
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
}
