package com.campusfind.lostfound.model;

public class MatchResult {
    private LostItem lostItem;
    private FoundItem foundItem;
    private int matchScore;
    private boolean isMatched;
    private int nameSimilarity;
    private int locationSimilarity;
    private int dateProximity;
    private int featureSimilarity;
    private String matchTier; // HIGH, MEDIUM, LOW
    private String matchReason;

    public MatchResult() {}

    public MatchResult(LostItem lostItem, FoundItem foundItem, int matchScore, boolean isMatched,
                       int nameSimilarity, int locationSimilarity, int dateProximity,
                       int featureSimilarity, String matchTier, String matchReason) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.matchScore = matchScore;
        this.isMatched = isMatched;
        this.nameSimilarity = nameSimilarity;
        this.locationSimilarity = locationSimilarity;
        this.dateProximity = dateProximity;
        this.featureSimilarity = featureSimilarity;
        this.matchTier = matchTier;
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

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
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

    public int getDateProximity() {
        return dateProximity;
    }

    public void setDateProximity(int dateProximity) {
        this.dateProximity = dateProximity;
    }

    public int getFeatureSimilarity() {
        return featureSimilarity;
    }

    public void setFeatureSimilarity(int featureSimilarity) {
        this.featureSimilarity = featureSimilarity;
    }

    public String getMatchTier() {
        return matchTier;
    }

    public void setMatchTier(String matchTier) {
        this.matchTier = matchTier;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }
}
