package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dto.ClaimRequest;
import com.campusfind.lostfound.dto.ClaimReviewRequest;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.repository.ClaimRepository;
import com.campusfind.lostfound.repository.FoundItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;
    private final NotificationService notificationService;

    public ClaimService(ClaimRepository claimRepository,
                        FoundItemRepository foundItemRepository,
                        NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.foundItemRepository = foundItemRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Claim submitClaim(ClaimRequest request, User claimant) {
        FoundItem foundItem = foundItemRepository.findById(request.getFoundId())
                .orElseThrow(() -> new IllegalArgumentException("Found item not found"));

        if (foundItem.getStatus() == FoundStatus.RETURNED) {
            throw new IllegalArgumentException("This item has already been returned to its owner");
        }

        if (foundItem.getUser().getUserId().equals(claimant.getUserId())) {
            throw new IllegalArgumentException("You reported this found item; you cannot file a claim against your own report");
        }

        if (claimRepository.existsByFoundItemAndClaimant(foundItem, claimant)) {
            throw new IllegalArgumentException("You have already submitted a claim for this item");
        }

        Claim claim = new Claim();
        claim.setFoundItem(foundItem);
        claim.setClaimant(claimant);
        claim.setItemDescriptionProof(request.getItemDescriptionProof().trim());
        claim.setUniqueIdentifyingFeatures(request.getUniqueIdentifyingFeatures().trim());
        claim.setApproxDateLost(request.getApproxDateLost());
        claim.setApproxLocationLost(request.getApproxLocationLost().trim());
        claim.setSupportingProofDetails(request.getSupportingProofDetails() != null ? request.getSupportingProofDetails().trim() : "");
        claim.setStatus(ClaimStatus.PENDING);

        Claim savedClaim = claimRepository.save(claim);

        // Update found item status to CLAIM_PENDING
        foundItem.setStatus(FoundStatus.CLAIM_PENDING);
        foundItemRepository.save(foundItem);

        // Notify claimant
        notificationService.sendNotification(
                claimant,
                "Claim Submitted Successfully",
                "Your ownership claim for '" + foundItem.getItemName() + "' was recorded with Reference #" +
                savedClaim.getClaimId() + ". An administrator will review your identifying details shortly.",
                NotificationType.CLAIM_UPDATE,
                savedClaim.getClaimId()
        );

        return savedClaim;
    }

    @Transactional
    public Claim reviewClaim(Long claimId, ClaimReviewRequest request, User admin) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));

        claim.setStatus(request.getStatus());
        claim.setAdminRemarks(request.getAdminRemarks());
        Claim updated = claimRepository.save(claim);

        FoundItem item = claim.getFoundItem();

        if (request.getStatus() == ClaimStatus.APPROVED) {
            notificationService.sendNotification(
                    claim.getClaimant(),
                    "Claim Approved! Collect Your Item",
                    "Your claim for '" + item.getItemName() + "' has been approved by campus administration. Please visit " +
                    item.getStorageLocation() + " with your Student/Staff ID (" + claim.getClaimant().getCampusId() + ") for physical handover.",
                    NotificationType.CLAIM_UPDATE,
                    claim.getClaimId()
            );
        } else if (request.getStatus() == ClaimStatus.REJECTED) {
            // Revert found item to AVAILABLE if no other claims are pending
            item.setStatus(FoundStatus.AVAILABLE);
            foundItemRepository.save(item);

            notificationService.sendNotification(
                    claim.getClaimant(),
                    "Claim Decision Notice",
                    "Your claim for '" + item.getItemName() + "' was not approved. Remarks: " +
                    (request.getAdminRemarks() != null ? request.getAdminRemarks() : "Identifying details did not verify."),
                    NotificationType.CLAIM_UPDATE,
                    claim.getClaimId()
            );
        } else if (request.getStatus() == ClaimStatus.UNDER_VERIFICATION) {
            notificationService.sendNotification(
                    claim.getClaimant(),
                    "Claim Under Verification",
                    "Your claim for '" + item.getItemName() + "' is actively being audited by campus security. Remarks: " +
                    (request.getAdminRemarks() != null ? request.getAdminRemarks() : "Under verification."),
                    NotificationType.CLAIM_UPDATE,
                    claim.getClaimId()
            );
        }

        return updated;
    }

    public List<Claim> getUserClaims(User user) {
        return claimRepository.findByClaimantOrderBySubmittedAtDesc(user);
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAllByOrderBySubmittedAtDesc();
    }

    public Claim getClaimById(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + claimId));
    }
}
