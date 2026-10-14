package com.campusfind.lostfound.controller;

import com.campusfind.lostfound.dto.*;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.repository.UserRepository;
import com.campusfind.lostfound.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ClaimService claimService;
    private final ItemReturnService itemReturnService;
    private final ItemService itemService;
    private final MatchingService matchingService;
    private final AuthService authService;

    public AdminController(UserRepository userRepository,
                           ClaimService claimService,
                           ItemReturnService itemReturnService,
                           ItemService itemService,
                           MatchingService matchingService,
                           AuthService authService) {
        this.userRepository = userRepository;
        this.claimService = claimService;
        this.itemReturnService = itemReturnService;
        this.itemService = itemService;
        this.matchingService = matchingService;
        this.authService = authService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getAllUsers() {
        List<UserSummaryDto> users = userRepository.findAll().stream()
                .map(UserSummaryDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("User accounts retrieved", users));
    }

    @GetMapping("/claims")
    public ResponseEntity<ApiResponse<List<Claim>>> getAllClaims() {
        List<Claim> claims = claimService.getAllClaims();
        return ResponseEntity.ok(ApiResponse.ok("All claims retrieved", claims));
    }

    @PutMapping("/claims/{id}/review")
    public ResponseEntity<ApiResponse<Claim>> reviewClaim(
            @PathVariable Long id,
            @Valid @RequestBody ClaimReviewRequest request
    ) {
        try {
            User admin = authService.getCurrentAuthenticatedUser();
            Claim updated = claimService.reviewClaim(id, request, admin);
            return ResponseEntity.ok(ApiResponse.ok("Claim reviewed successfully", updated));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/returns")
    public ResponseEntity<ApiResponse<ItemReturn>> processHandover(
            @Valid @RequestBody ItemReturnRequest request
    ) {
        try {
            User admin = authService.getCurrentAuthenticatedUser();
            ItemReturn itemReturn = itemReturnService.processHandover(request, admin);
            return ResponseEntity.ok(ApiResponse.ok("Item return and handover completed successfully", itemReturn));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/returns")
    public ResponseEntity<ApiResponse<List<ItemReturn>>> getAllReturns() {
        List<ItemReturn> returns = itemReturnService.getAllReturns();
        return ResponseEntity.ok(ApiResponse.ok("Item return history retrieved", returns));
    }

    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<MatchResultDto>>> getAllMatches() {
        List<MatchResultDto> matches = matchingService.getAllPotentialMatches();
        return ResponseEntity.ok(ApiResponse.ok("System-wide matches retrieved", matches));
    }

    @DeleteMapping("/items/lost/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLostItem(@PathVariable Long id) {
        try {
            User admin = authService.getCurrentAuthenticatedUser();
            itemService.deleteLostItem(id, admin);
            return ResponseEntity.ok(ApiResponse.ok("Lost item report removed by administrator"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @DeleteMapping("/items/found/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFoundItem(@PathVariable Long id) {
        try {
            User admin = authService.getCurrentAuthenticatedUser();
            itemService.deleteFoundItem(id, admin);
            return ResponseEntity.ok(ApiResponse.ok("Found item report removed by administrator"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }
}
