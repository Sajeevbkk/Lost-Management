package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dto.MatchResultDto;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.repository.FoundItemRepository;
import com.campusfind.lostfound.repository.LostItemRepository;
import com.campusfind.lostfound.util.SimilarityUtil;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MatchingService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final NotificationService notificationService;

    public MatchingService(LostItemRepository lostItemRepository,
                           FoundItemRepository foundItemRepository,
                           NotificationService notificationService) {
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
        this.notificationService = notificationService;
    }

    /**
     * Compute match score and comparison details between a lost item and a found item
     */
    public MatchResultDto evaluateMatch(LostItem lost, FoundItem found) {
        boolean categoryMatch = (lost.getCategory() == found.getCategory());
        if (!categoryMatch) {
            return new MatchResultDto(lost, found, 0, false, 0, 0, 0, 0, "LOW", "Categories do not match");
        }

        // 1. Name similarity (weight 25%)
        double nameSim = SimilarityUtil.nameSimilarity(lost.getItemName(), found.getItemName());
        int nameScore = (int) Math.round(nameSim * 100);

        // 2. Location proximity (weight 20%)
        double locSim = SimilarityUtil.locationSimilarity(lost.getLocationLost(), found.getLocationFound());
        int locScore = (int) Math.round(locSim * 100);

        // 3. Date consistency (weight 15%)
        long deltaDays = ChronoUnit.DAYS.between(lost.getDateLost(), found.getDateFound());
        int dateScore;
        if (deltaDays >= 0 && deltaDays <= 3) {
            dateScore = 100;
        } else if (deltaDays >= 4 && deltaDays <= 10) {
            dateScore = 65;
        } else if (deltaDays >= 11 && deltaDays <= 30) {
            dateScore = 40;
        } else if (deltaDays >= -2 && deltaDays < 0) {
            dateScore = 20; // Allow slight date reporting discrepancy
        } else {
            dateScore = 10;
        }

        // 4. Color & Feature / Description attributes (weight 10%)
        String combinedLostDesc = lost.getColorFeatures() + " " + lost.getDescription();
        String combinedFoundDesc = found.getColorFeatures() + " " + found.getDescription();
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
        reason.append("Category match: ").append(lost.getCategory().name()).append(". ");
        if (nameScore >= 70) reason.append("Strong title similarity (").append(nameScore).append("%). ");
        if (locScore >= 70) reason.append("Close campus proximity near ").append(found.getLocationFound()).append(". ");
        if (deltaDays >= 0 && deltaDays <= 5) reason.append("Discovered within ").append(deltaDays).append(" day(s) of reported loss. ");
        if (featureScore >= 50) reason.append("Matching color & keyword attributes.");

        return new MatchResultDto(lost, found, totalScore, true, nameScore, locScore, dateScore, featureScore, matchStatus, reason.toString());
    }

    /**
     * Scan when a new lost item is reported against all active found items
     */
    public List<MatchResultDto> scanMatchesForLostItem(LostItem lostItem) {
        List<FoundItem> availableFound = foundItemRepository.findByStatusOrderByCreatedAtDesc(FoundStatus.AVAILABLE);
        List<MatchResultDto> matches = new ArrayList<>();

        for (FoundItem found : availableFound) {
            MatchResultDto match = evaluateMatch(lostItem, found);
            if (match.getMatchScore() >= 60) {
                matches.add(match);

                // Notify lost item owner
                notificationService.sendNotification(
                        lostItem.getUser(),
                        "Potential Match: " + lostItem.getItemName(),
                        "A found item '" + found.getItemName() + "' at '" + found.getLocationFound() +
                        "' matches your lost item report with " + match.getMatchScore() + "% confidence score. Check your dashboard to review and submit a claim.",
                        NotificationType.POTENTIAL_MATCH,
                        found.getFoundId()
                );

                // Notify found item reporter
                if (!found.getUser().getUserId().equals(lostItem.getUser().getUserId())) {
                    notificationService.sendNotification(
                            found.getUser(),
                            "Matching Report for Found Item: " + found.getItemName(),
                            "A student reported a lost '" + lostItem.getItemName() + "' matching the item you discovered (Score: " +
                            match.getMatchScore() + "%).",
                            NotificationType.POTENTIAL_MATCH,
                            lostItem.getLostId()
                    );
                }
            }
        }

        matches.sort(Comparator.comparingInt(MatchResultDto::getMatchScore).reversed());
        return matches;
    }

    /**
     * Scan when a new found item is reported against all active lost items
     */
    public List<MatchResultDto> scanMatchesForFoundItem(FoundItem foundItem) {
        List<LostItem> reportedLost = lostItemRepository.findByStatusOrderByCreatedAtDesc(LostStatus.REPORTED);
        List<MatchResultDto> matches = new ArrayList<>();

        for (LostItem lost : reportedLost) {
            MatchResultDto match = evaluateMatch(lost, foundItem);
            if (match.getMatchScore() >= 60) {
                matches.add(match);

                // Notify lost item owner
                notificationService.sendNotification(
                        lost.getUser(),
                        "Potential Match Discovered!",
                        "A found item '" + foundItem.getItemName() + "' discovered at '" + foundItem.getLocationFound() +
                        "' matches your report for '" + lost.getItemName() + "' (" + match.getMatchScore() + "% match). Visit your dashboard to submit an ownership claim.",
                        NotificationType.POTENTIAL_MATCH,
                        foundItem.getFoundId()
                );

                // Notify found item reporter
                if (!foundItem.getUser().getUserId().equals(lost.getUser().getUserId())) {
                    notificationService.sendNotification(
                            foundItem.getUser(),
                            "Possible Owner Identified",
                            "A reported missing item '" + lost.getItemName() + "' matches the item you registered (" +
                            match.getMatchScore() + "% match).",
                            NotificationType.POTENTIAL_MATCH,
                            lost.getLostId()
                    );
                }
            }
        }

        matches.sort(Comparator.comparingInt(MatchResultDto::getMatchScore).reversed());
        return matches;
    }

    /**
     * Get all active potential matches across the whole system
     */
    public List<MatchResultDto> getAllPotentialMatches() {
        List<LostItem> reportedLost = lostItemRepository.findByStatusOrderByCreatedAtDesc(LostStatus.REPORTED);
        List<FoundItem> availableFound = foundItemRepository.findByStatusOrderByCreatedAtDesc(FoundStatus.AVAILABLE);

        List<MatchResultDto> allMatches = new ArrayList<>();
        for (LostItem lost : reportedLost) {
            for (FoundItem found : availableFound) {
                if (lost.getCategory() == found.getCategory()) {
                    MatchResultDto match = evaluateMatch(lost, found);
                    if (match.getMatchScore() >= 55) {
                        allMatches.add(match);
                    }
                }
            }
        }

        allMatches.sort(Comparator.comparingInt(MatchResultDto::getMatchScore).reversed());
        return allMatches;
    }

    /**
     * Get potential matches specifically relevant to a user
     */
    public List<MatchResultDto> getMatchesForUser(User user) {
        List<LostItem> userLost = lostItemRepository.findByUserOrderByCreatedAtDesc(user);
        List<FoundItem> availableFound = foundItemRepository.findByStatusOrderByCreatedAtDesc(FoundStatus.AVAILABLE);

        List<MatchResultDto> userMatches = new ArrayList<>();
        for (LostItem lost : userLost) {
            if (lost.getStatus() == LostStatus.REPORTED) {
                for (FoundItem found : availableFound) {
                    if (lost.getCategory() == found.getCategory()) {
                        MatchResultDto match = evaluateMatch(lost, found);
                        if (match.getMatchScore() >= 50) {
                            userMatches.add(match);
                        }
                    }
                }
            }
        }

        userMatches.sort(Comparator.comparingInt(MatchResultDto::getMatchScore).reversed());
        return userMatches;
    }
}
