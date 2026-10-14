package com.campusfind.lostfound.repository;

import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.ClaimStatus;
import com.campusfind.lostfound.model.FoundItem;
import com.campusfind.lostfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByClaimantOrderBySubmittedAtDesc(User claimant);
    List<Claim> findByFoundItemOrderBySubmittedAtDesc(FoundItem foundItem);
    List<Claim> findByStatusOrderBySubmittedAtDesc(ClaimStatus status);
    List<Claim> findAllByOrderBySubmittedAtDesc();
    long countByStatus(ClaimStatus status);
    boolean existsByFoundItemAndClaimant(FoundItem foundItem, User claimant);
}
