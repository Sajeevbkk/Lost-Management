package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dao.ClaimDao;
import com.campusfind.lostfound.dao.FoundItemDao;
import com.campusfind.lostfound.dao.LostItemDao;
import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.ClaimStatus;
import com.campusfind.lostfound.model.FoundItem;
import com.campusfind.lostfound.model.FoundStatus;
import com.campusfind.lostfound.model.LostItem;

import java.util.List;

public class StatsService {

    private static final StatsService INSTANCE = new StatsService();
    private final LostItemDao lostItemDao = new LostItemDao();
    private final FoundItemDao foundItemDao = new FoundItemDao();
    private final ClaimDao claimDao = new ClaimDao();

    private StatsService() {}

    public static StatsService getInstance() {
        return INSTANCE;
    }

    public int getTotalLost() {
        return lostItemDao.findAll().size();
    }

    public int getTotalFound() {
        return foundItemDao.findAll().size();
    }

    public int getTotalReturned() {
        List<FoundItem> found = foundItemDao.findAll();
        int count = 0;
        for (FoundItem f : found) {
            if (f.getStatus() == FoundStatus.RETURNED) {
                count++;
            }
        }
        return count;
    }

    public int getActiveClaims() {
        List<Claim> claims = claimDao.findAll();
        int count = 0;
        for (Claim c : claims) {
            if (c.getStatus() == ClaimStatus.PENDING ||
                c.getStatus() == ClaimStatus.UNDER_VERIFICATION ||
                c.getStatus() == ClaimStatus.APPROVED) {
                count++;
            }
        }
        return count;
    }

    public double getRecoveryRate() {
        int totalFound = getTotalFound();
        int returned = getTotalReturned();
        if (totalFound == 0) return 0.0;
        return ((double) returned / totalFound) * 100.0;
    }
}
