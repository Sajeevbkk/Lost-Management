package com.campusfind.lostfound.dto;

import com.campusfind.lostfound.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LostItemRequest {
    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Item category is required")
    private Category category;

    @NotBlank(message = "Item description is required")
    private String description;

    @NotNull(message = "Date lost is required")
    private LocalDate dateLost;

    @NotBlank(message = "Location lost is required")
    private String locationLost;

    @NotBlank(message = "Color and identifying features are required")
    private String colorFeatures;

    private String imageUrl;

    @NotBlank(message = "Contact phone number is required")
    private String contactPhone;

    public LostItemRequest() {
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

    public LocalDate getDateLost() {
        return dateLost;
    }

    public void setDateLost(LocalDate dateLost) {
        this.dateLost = dateLost;
    }

    public String getLocationLost() {
        return locationLost;
    }

    public void setLocationLost(String locationLost) {
        this.locationLost = locationLost;
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

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
