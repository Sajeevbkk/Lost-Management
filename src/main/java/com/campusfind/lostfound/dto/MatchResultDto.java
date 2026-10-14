package com.campusfind.lostfound.dto;

import com.campusfind.lostfound.model.FoundItem;
import com.campusfind.lostfound.model.LostItem;

public class MatchResultDto {
    private LostItem lostItem;
    private FoundItem foundItem;
    private int matchScore;
    private boolean categoryMatch;
    private int nameSimilarity;
    private int locationSimilarity;
    private int dateScore;
    private int featureScore;
    private String matchStatus;
    private String matchReason;

    public MatchResultDto() {
    }

    public MatchResultDto(LostItem lostItem, FoundItem foundItem, int matchScore, boolean categoryMatch,
                          int nameSimilarity, int locationSimilarity, int dateScore, int featureScore,
                          String matchStatus, String matchReason) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.matchScore = matchScore;
        this.categoryMatch = categoryMatch;
        this.nameSimilarity = nameSimilarity;
        this.locationSimilarity = locationSimilarity;
        this.dateScore = dateScore;
        this.featureScore = featureScore;
        this.matchStatus = matchStatus;
        this.matchReason = matchReason;
    }

    public LostItem getLostItem() {
        return lostItem;
    }

    public void setLostItem(LostItem lostItem) {
        this.lostItem = lostItem;
    }

    public FoundItem getFoundItem() {
        return foundItem;
    }

    public void setFoundItem(FoundItem foundItem) {
        this.foundItem = foundItem;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public boolean isCategoryMatch() {
        return categoryMatch;
    }

    public void setCategoryMatch(boolean categoryMatch) {
        this.categoryMatch = categoryMatch;
    }

    public int getNameSimilarity() {
        return nameSimilarity;
    }

    public void setNameSimilarity(int nameSimilarity) {
        this.nameSimilarity = nameSimilarity;
    }

    public int getLocationSimilarity() {
        return locationSimilarity;
    }

    public void setLocationSimilarity(int locationSimilarity) {
        this.locationSimilarity = locationSimilarity;
    }

    public int getDateScore() {
        return dateScore;
    }

    public void setDateScore(int dateScore) {
        this.dateScore = dateScore;
    }

    public int getFeatureScore() {
        return featureScore;
    }

    public void setFeatureScore(int featureScore) {
        this.featureScore = featureScore;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }
}
