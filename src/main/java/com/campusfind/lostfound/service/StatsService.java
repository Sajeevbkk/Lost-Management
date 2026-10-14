package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dto.StatsSummaryDto;
import com.campusfind.lostfound.model.ClaimStatus;
import com.campusfind.lostfound.model.FoundStatus;
import com.campusfind.lostfound.repository.ClaimRepository;
import com.campusfind.lostfound.repository.FoundItemRepository;
import com.campusfind.lostfound.repository.LostItemRepository;
import com.campusfind.lostfound.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final MatchingService matchingService;

    public StatsService(LostItemRepository lostItemRepository,
                        FoundItemRepository foundItemRepository,
                        ClaimRepository claimRepository,
                        UserRepository userRepository,
                        MatchingService matchingService) {
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.matchingService = matchingService;
    }

    public StatsSummaryDto getSummaryStats() {
        long totalLost = lostItemRepository.count();
        long totalFound = foundItemRepository.count();
        long totalReturned = foundItemRepository.countByStatus(FoundStatus.RETURNED);
        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING)
                + claimRepository.countByStatus(ClaimStatus.UNDER_VERIFICATION);
        long totalUsers = userRepository.count();
        long potentialMatches = matchingService.getAllPotentialMatches().size();

        return new StatsSummaryDto(
                totalLost,
                totalFound,
                totalReturned,
                pendingClaims,
                totalUsers,
                potentialMatches
        );
    }
}
