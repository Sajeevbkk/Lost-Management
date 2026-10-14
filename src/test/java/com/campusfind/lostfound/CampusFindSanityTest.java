package com.campusfind.lostfound;

import com.campusfind.lostfound.config.DatabaseManager;
import com.campusfind.lostfound.dao.*;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.service.*;

import java.util.List;

public class CampusFindSanityTest {

    public static void main(String[] args) {
        System.out.println("=== Starting CampusFind Desktop Application Sanity Test ===");

        // 1. Initialize Database & Seed Data
        DatabaseManager.initialize();

        // 2. Test User DAO & Auth
        UserDao userDao = new UserDao();
        List<User> users = userDao.findAll();
        System.out.println("✓ Users loaded from DB: " + users.size() + " accounts.");
        if (users.size() < 5) {
            throw new RuntimeException("Expected at least 5 seeded users, got " + users.size());
        }

        AuthService authService = AuthService.getInstance();
        boolean adminLogin = authService.login("admin@college.edu", "Admin@123");
        System.out.println("✓ Admin login success: " + adminLogin + " (Role: " + authService.getCurrentUser().getRole() + ")");
        if (!adminLogin || !authService.isAdmin()) {
            throw new RuntimeException("Admin login failed or role mismatch");
        }

        // 3. Test Lost & Found DAOs
        LostItemDao lostItemDao = new LostItemDao();
        List<LostItem> lostItems = lostItemDao.findAll();
        System.out.println("✓ Lost Items loaded: " + lostItems.size());

        FoundItemDao foundItemDao = new FoundItemDao();
        List<FoundItem> foundItems = foundItemDao.findAll();
        System.out.println("✓ Found Items loaded: " + foundItems.size());

        // 4. Test AI Multi-factor Matching Engine
        MatchingService matchingService = MatchingService.getInstance();
        List<MatchResult> activeMatches = matchingService.getAllActiveMatches();
        System.out.println("✓ Smart Matches calculated: " + activeMatches.size());
        for (MatchResult m : activeMatches) {
            System.out.println("   -> Match: '" + m.getLostItem().getItemName() + "' <=> '" +
                    m.getFoundItem().getItemName() + "' | Score: " + m.getMatchScore() + "% (" + m.getMatchTier() + ")");
        }

        // 5. Test Claims & Audit Trail
        ClaimDao claimDao = new ClaimDao();
        List<Claim> claims = claimDao.findAll();
        System.out.println("✓ Claims loaded: " + claims.size());

        ItemReturnDao itemReturnDao = new ItemReturnDao();
        List<ItemReturn> returns = itemReturnDao.findAll();
        System.out.println("✓ Handover audit trail records: " + returns.size());

        // 6. Test Stats Service
        StatsService statsService = StatsService.getInstance();
        System.out.println("✓ Total Lost: " + statsService.getTotalLost());
        System.out.println("✓ Total Found: " + statsService.getTotalFound());
        System.out.println("✓ Total Returned: " + statsService.getTotalReturned());
        System.out.println("✓ Active Claims: " + statsService.getActiveClaims());
        System.out.printf("✓ Recovery Rate: %.1f%%\n", statsService.getRecoveryRate());

        System.out.println("=== All CampusFind Desktop Backend & Logic Tests Passed Successfully! ===");
        System.exit(0);
    }
}
