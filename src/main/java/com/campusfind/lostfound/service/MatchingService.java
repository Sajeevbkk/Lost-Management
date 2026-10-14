package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dao.FoundItemDao;
import com.campusfind.lostfound.dao.LostItemDao;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.util.SimilarityUtil;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MatchingService {

    private static final MatchingService INSTANCE = new MatchingService();
    private final LostItemDao lostItemDao = new LostItemDao();
    private final FoundItemDao foundItemDao = new FoundItemDao();
    private final NotificationService notificationService = NotificationService.getInstance();

    private MatchingService() {}

    public static MatchingService getInstance() {
        return INSTANCE;
    }

    /**
     * Compute match score and comparison details between a lost item and a found item
     */
    public MatchResult evaluateMatch(LostItem lost, FoundItem found) {
        if (lost == null || found == null) {
            return new MatchResult(lost, found, 0, false, 0, 0, 0, 0, "LOW", "Invalid items");
        }

        boolean categoryMatch = (lost.getCategory() == found.getCategory());
        if (!categoryMatch) {
            return new MatchResult(lost, found, 0, false, 0, 0, 0, 0, "LOW", "Categories do not match");
        }

        // 1. Name similarity (weight 25%)
        double nameSim = SimilarityUtil.nameSimilarity(lost.getItemName(), found.getItemName());
        int nameScore = (int) Math.round(nameSim * 100);

        // 2. Location proximity (weight 20%)
        double locSim = SimilarityUtil.locationSimilarity(lost.getLocationLost(), found.getLocationFound());
        int locScore = (int) Math.round(locSim * 100);

        // 3. Date consistency (weight 15%)
        long deltaDays = 0;
        if (lost.getDateLost() != null && found.getDateFound() != null) {
            deltaDays = ChronoUnit.DAYS.between(lost.getDateLost(), found.getDateFound());
        }
        int dateScore;
        if (deltaDays >= 0 && deltaDays <= 3) {
            dateScore = 100;
        } else if (deltaDays >= 4 && deltaDays <= 10) {
            dateScore = 65;
        } else if (deltaDays >= 11 && deltaDays <= 30) {
            dateScore = 40;
        } else if (deltaDays >= -2 && deltaDays < 0) {
            dateScore = 20; // Slight reporting date discrepancy
        } else {
            dateScore = 10;
        }

        // 4. Color & Feature / Description attributes (weight 10%)
        String combinedLostDesc = (lost.getColorFeatures() != null ? lost.getColorFeatures() : "") + " " +
                (lost.getDescription() != null ? lost.getDescription() : "");
        String combinedFoundDesc = (found.getColorFeatures() != null ? found.getColorFeatures() : "") + " " +
                (found.getDescription() != null ? found.getDescription() : "");
        double descSim = SimilarityUtil.jaccardSimilarity(combinedLostDesc, combinedFoundDesc);
        int featureScore = (int) Math.round(descSim * 100);

        // Total weighted score (Category is 30% baseline match)
        double totalWeighted = (30.0 * 1.0) +
                (25.0 * (nameScore / 100.0)) +
                (20.0 * (locScore / 100.0)) +
                (15.0 * (dateScore / 100.0)) +
                (10.0 * (featureScore / 100.0));

        int totalScore = (int) Math.min(100, Math.round(totalWeighted));

        String matchStatus;
        if (totalScore >= 75) {
            matchStatus = "HIGH";
        } else if (totalScore >= 60) {
            matchStatus = "MEDIUM";
        } else {
            matchStatus = "LOW";
        }

        StringBuilder reason = new StringBuilder();
        reason.append("Category match: ").append(lost.getCategory().getDisplayName()).append(". ");
        if (nameScore >= 70) reason.append("Strong title similarity (").append(nameScore).append("%). ");
        if (locScore >= 70) reason.append("Close campus proximity near ").append(found.getLocationFound()).append(". ");
        if (deltaDays >= 0 && deltaDays <= 5) reason.append("Discovered within ").append(deltaDays).append(" day(s) of reported loss. ");
        if (featureScore >= 50) reason.append("Matching color & keyword attributes.");

        return new MatchResult(lost, found, totalScore, true, nameScore, locScore, dateScore, featureScore, matchStatus, reason.toString().trim());
    }

    /**
     * Scan when a new lost item is reported against all active found items
     */
    public List<MatchResult> scanMatchesForLostItem(LostItem lostItem) {
        List<FoundItem> availableFound = foundItemDao.findByStatus(FoundStatus.AVAILABLE);
        List<MatchResult> matches = new ArrayList<>();

        for (FoundItem found : availableFound) {
            MatchResult match = evaluateMatch(lostItem, found);
            if (match.getMatchScore() >= 60) {
                matches.add(match);

                // Notify lost item owner
                if (lostItem.getUser() != null) {
                    notificationService.sendNotification(
                            lostItem.getUser(),
                            "Potential Match: " + lostItem.getItemName(),
                            "A found item '" + found.getItemName() + "' at '" + found.getLocationFound() +
                                    "' matches your report with " + match.getMatchScore() + "% confidence. Review your dashboard to claim.",
                            NotificationType.POTENTIAL_MATCH,
                            found.getFoundId()
                    );
                }
            }
        }

        matches.sort(Comparator.comparingInt(MatchResult::getMatchScore).reversed());
        return matches;
    }

    /**
     * Scan when a new found item is reported against all active lost items
     */
    public List<MatchResult> scanMatchesForFoundItem(FoundItem foundItem) {
        List<LostItem> activeLost = lostItemDao.findByStatus(LostStatus.REPORTED);
        List<MatchResult> matches = new ArrayList<>();

        for (LostItem lost : activeLost) {
            MatchResult match = evaluateMatch(lost, foundItem);
            if (match.getMatchScore() >= 60) {
                matches.add(match);

                // Notify lost item owner
                if (lost.getUser() != null) {
                    notificationService.sendNotification(
                            lost.getUser(),
                            "Potential Match: " + lost.getItemName(),
                            "A found item '" + foundItem.getItemName() + "' at '" + foundItem.getLocationFound() +
                                    "' matches your lost item with " + match.getMatchScore() + "% confidence.",
                            NotificationType.POTENTIAL_MATCH,
                            foundItem.getFoundId()
                    );
                }
            }
        }

        matches.sort(Comparator.comparingInt(MatchResult::getMatchScore).reversed());
        return matches;
    }

    /**
     * Get all high and medium matches across all active lost & found items
     */
    public List<MatchResult> getAllActiveMatches() {
        List<LostItem> activeLost = lostItemDao.findByStatus(LostStatus.REPORTED);
        List<FoundItem> availableFound = foundItemDao.findByStatus(FoundStatus.AVAILABLE);

        List<MatchResult> matches = new ArrayList<>();
        for (LostItem lost : activeLost) {
            for (FoundItem found : availableFound) {
                MatchResult match = evaluateMatch(lost, found);
                if (match.getMatchScore() >= 60) {
                    matches.add(match);
                }
            }
        }

        matches.sort(Comparator.comparingInt(MatchResult::getMatchScore).reversed());
        return matches;
    }

    /**
     * Get all matches relevant to a specific user (their lost items matching found items)
     */
    public List<MatchResult> getMatchesForUser(User user) {
        if (user == null) return new ArrayList<>();
        List<LostItem> userLost = lostItemDao.findByUserId(user.getUserId());
        List<FoundItem> availableFound = foundItemDao.findByStatus(FoundStatus.AVAILABLE);

        List<MatchResult> matches = new ArrayList<>();
        for (LostItem lost : userLost) {
            if (lost.getStatus() != LostStatus.REPORTED) continue;
            for (FoundItem found : availableFound) {
                MatchResult match = evaluateMatch(lost, found);
                if (match.getMatchScore() >= 60) {
                    matches.add(match);
                }
            }
        }

        matches.sort(Comparator.comparingInt(MatchResult::getMatchScore).reversed());
        return matches;
    }
}
