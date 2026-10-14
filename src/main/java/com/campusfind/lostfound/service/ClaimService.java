package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dao.ClaimDao;
import com.campusfind.lostfound.dao.FoundItemDao;
import com.campusfind.lostfound.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClaimService {

    private static final ClaimService INSTANCE = new ClaimService();
    private final ClaimDao claimDao = new ClaimDao();
    private final FoundItemDao foundItemDao = new FoundItemDao();
    private final NotificationService notificationService = NotificationService.getInstance();
    private final List<Runnable> changeListeners = new ArrayList<>();

    private ClaimService() {}

    public static ClaimService getInstance() {
        return INSTANCE;
    }

    public Claim submitClaim(FoundItem foundItem, User claimant, String descProof, String features,
                             LocalDate approxDateLost, String approxLocationLost, String supportingProof) {
        Claim claim = new Claim();
        claim.setFoundItem(foundItem);
        claim.setClaimant(claimant);
        claim.setItemDescriptionProof(descProof);
        claim.setUniqueIdentifyingFeatures(features);
        claim.setApproxDateLost(approxDateLost);
        claim.setApproxLocationLost(approxLocationLost);
        claim.setSupportingProofDetails(supportingProof);
        claim.setStatus(ClaimStatus.PENDING);
        claim.setAdminRemarks("Claim submitted. Awaiting administrative review.");

        Claim saved = claimDao.save(claim);

        // Update found item status to CLAIM_PENDING
        foundItemDao.updateStatus(foundItem.getFoundId(), FoundStatus.CLAIM_PENDING);

        // Send notification to claimant
        notificationService.sendNotification(
                claimant,
                "Claim Submitted: " + foundItem.getItemName(),
                "Your ownership claim has been submitted. Campus security will review your confidential identifying evidence.",
                NotificationType.CLAIM_UPDATE,
                saved.getClaimId()
        );

        notifyChangeListeners();
        return saved;
    }

    public void reviewClaim(Long claimId, ClaimStatus newStatus, String adminRemarks, User adminUser) {
        Claim claim = claimDao.findById(claimId);
        if (claim == null) return;

        claimDao.updateStatusAndRemarks(claimId, newStatus, adminRemarks);

        FoundItem item = claim.getFoundItem();
        if (newStatus == ClaimStatus.APPROVED) {
            notificationService.sendNotification(
                    claim.getClaimant(),
                    "Claim Approved: " + item.getItemName(),
                    "Your claim has been APPROVED! Please visit " + item.getStorageLocation() +
                            " with your Campus ID to collect your item. Remarks: " + adminRemarks,
                    NotificationType.CLAIM_UPDATE,
                    claimId
            );
        } else if (newStatus == ClaimStatus.REJECTED) {
            // Check if any other claims are active on this found item
            List<Claim> allClaims = claimDao.findByFoundItemId(item.getFoundId());
            boolean hasOtherActive = allClaims.stream().anyMatch(c ->
                    !c.getClaimId().equals(claimId) &&
                    (c.getStatus() == ClaimStatus.PENDING || c.getStatus() == ClaimStatus.UNDER_VERIFICATION || c.getStatus() == ClaimStatus.APPROVED));
            if (!hasOtherActive) {
                foundItemDao.updateStatus(item.getFoundId(), FoundStatus.AVAILABLE);
            }

            notificationService.sendNotification(
                    claim.getClaimant(),
                    "Claim Update: " + item.getItemName(),
                    "Your claim was not approved. Remarks: " + (adminRemarks != null && !adminRemarks.isEmpty() ? adminRemarks : "Details did not match stored evidence."),
                    NotificationType.CLAIM_UPDATE,
                    claimId
            );
        } else if (newStatus == ClaimStatus.UNDER_VERIFICATION) {
            notificationService.sendNotification(
                    claim.getClaimant(),
                    "Claim Under Verification: " + item.getItemName(),
                    "Your claim is currently under physical verification. Admin note: " + adminRemarks,
                    NotificationType.CLAIM_UPDATE,
                    claimId
            );
        }

        notifyChangeListeners();
    }

    public List<Claim> getClaimsByClaimant(Long userId) {
        return claimDao.findByClaimantId(userId);
    }

    public List<Claim> getClaimsByFoundItem(Long foundId) {
        return claimDao.findByFoundItemId(foundId);
    }

    public List<Claim> getAllClaims() {
        return claimDao.findAll();
    }

    public List<Claim> getPendingClaims() {
        List<Claim> pending = claimDao.findByStatus(ClaimStatus.PENDING);
        List<Claim> underVerif = claimDao.findByStatus(ClaimStatus.UNDER_VERIFICATION);
        List<Claim> combined = new ArrayList<>(pending);
        combined.addAll(underVerif);
        return combined;
    }

    public List<Claim> getApprovedClaims() {
        return claimDao.findByStatus(ClaimStatus.APPROVED);
    }

    public Claim getClaimById(Long claimId) {
        return claimDao.findById(claimId);
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    private void notifyChangeListeners() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
