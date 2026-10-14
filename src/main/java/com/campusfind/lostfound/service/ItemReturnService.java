package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dao.ClaimDao;
import com.campusfind.lostfound.dao.FoundItemDao;
import com.campusfind.lostfound.dao.ItemReturnDao;
import com.campusfind.lostfound.dao.LostItemDao;
import com.campusfind.lostfound.model.*;

import java.util.ArrayList;
import java.util.List;

public class ItemReturnService {

    private static final ItemReturnService INSTANCE = new ItemReturnService();
    private final ItemReturnDao itemReturnDao = new ItemReturnDao();
    private final ClaimDao claimDao = new ClaimDao();
    private final FoundItemDao foundItemDao = new FoundItemDao();
    private final LostItemDao lostItemDao = new LostItemDao();
    private final NotificationService notificationService = NotificationService.getInstance();
    private final List<Runnable> changeListeners = new ArrayList<>();

    private ItemReturnService() {}

    public static ItemReturnService getInstance() {
        return INSTANCE;
    }

    public ItemReturn executeHandover(Claim claim, String handoverNotes, User adminUser) {
        if (claim == null || adminUser == null) return null;

        FoundItem foundItem = claim.getFoundItem();
        User claimant = claim.getClaimant();

        // 1. Create audit trail record
        ItemReturn itemReturn = new ItemReturn(claim, foundItem, claimant, adminUser, handoverNotes);
        ItemReturn saved = itemReturnDao.save(itemReturn);

        // 2. Mark Claim as RETURNED
        claimDao.updateStatusAndRemarks(claim.getClaimId(), ClaimStatus.RETURNED, "Handover completed by " + adminUser.getFullName() + ". Notes: " + handoverNotes);

        // 3. Mark Found Item as RETURNED
        foundItemDao.updateStatus(foundItem.getFoundId(), FoundStatus.RETURNED);

        // 4. Resolve any matching lost item reported by this claimant
        List<LostItem> claimantLostItems = lostItemDao.findByUserId(claimant.getUserId());
        for (LostItem lost : claimantLostItems) {
            if (lost.getStatus() == LostStatus.REPORTED && lost.getCategory() == foundItem.getCategory()) {
                lostItemDao.updateStatus(lost.getLostId(), LostStatus.RESOLVED);
            }
        }

        // 5. Send notification to claimant
        notificationService.sendNotification(
                claimant,
                "Handover Complete: " + foundItem.getItemName(),
                "Your item has been successfully handed over and verified by Campus Administration. Case closed.",
                NotificationType.CLAIM_UPDATE,
                saved.getReturnId()
        );

        notifyChangeListeners();
        return saved;
    }

    public List<ItemReturn> getAllReturns() {
        return itemReturnDao.findAll();
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
