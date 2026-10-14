package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dto.ItemReturnRequest;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.repository.ClaimRepository;
import com.campusfind.lostfound.repository.FoundItemRepository;
import com.campusfind.lostfound.repository.ItemReturnRepository;
import com.campusfind.lostfound.repository.LostItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemReturnService {

    private final ItemReturnRepository itemReturnRepository;
    private final ClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;
    private final LostItemRepository lostItemRepository;
    private final NotificationService notificationService;

    public ItemReturnService(ItemReturnRepository itemReturnRepository,
                             ClaimRepository claimRepository,
                             FoundItemRepository foundItemRepository,
                             LostItemRepository lostItemRepository,
                             NotificationService notificationService) {
        this.itemReturnRepository = itemReturnRepository;
        this.claimRepository = claimRepository;
        this.foundItemRepository = foundItemRepository;
        this.lostItemRepository = lostItemRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ItemReturn processHandover(ItemReturnRequest request, User admin) {
        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + request.getClaimId()));

        if (claim.getStatus() == ClaimStatus.RETURNED) {
            throw new IllegalArgumentException("This item has already been marked as returned");
        }

        FoundItem foundItem = claim.getFoundItem();
        User claimant = claim.getClaimant();

        // Update statuses
        claim.setStatus(ClaimStatus.RETURNED);
        claimRepository.save(claim);

        foundItem.setStatus(FoundStatus.RETURNED);
        foundItemRepository.save(foundItem);

        // If claimant had a matching active lost item, mark it RESOLVED
        List<LostItem> claimantLostItems = lostItemRepository.findByUserOrderByCreatedAtDesc(claimant);
        for (LostItem lost : claimantLostItems) {
            if (lost.getStatus() == LostStatus.REPORTED && lost.getCategory() == foundItem.getCategory()) {
                lost.setStatus(LostStatus.RESOLVED);
                lostItemRepository.save(lost);
                break;
            }
        }

        // Save return record
        ItemReturn itemReturn = new ItemReturn(
                claim,
                foundItem,
                claimant,
                admin,
                request.getHandoverNotes() != null ? request.getHandoverNotes().trim() : "Handover completed at security desk."
        );
        ItemReturn savedReturn = itemReturnRepository.save(itemReturn);

        // Send completion notification
        notificationService.sendNotification(
                claimant,
                "Item Successfully Recovered!",
                "Great news! Your item '" + foundItem.getItemName() +
                "' was successfully returned to you. The recovery case is now officially completed and closed.",
                NotificationType.CLAIM_UPDATE,
                savedReturn.getReturnId()
        );

        return savedReturn;
    }

    public List<ItemReturn> getAllReturns() {
        return itemReturnRepository.findAllByOrderByHandoverTimestampDesc();
    }

    public List<ItemReturn> getUserReturns(User user) {
        return itemReturnRepository.findByReturnedToUserOrderByHandoverTimestampDesc(user);
    }
}
