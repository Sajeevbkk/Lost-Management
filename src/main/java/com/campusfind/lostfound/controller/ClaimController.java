package com.campusfind.lostfound.controller;

import com.campusfind.lostfound.dto.ApiResponse;
import com.campusfind.lostfound.dto.ClaimRequest;
import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.Role;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final AuthService authService;

    public ClaimController(ClaimService claimService, AuthService authService) {
        this.claimService = claimService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Claim>> submitClaim(@Valid @RequestBody ClaimRequest request) {
        try {
            User claimant = authService.getCurrentAuthenticatedUser();
            Claim claim = claimService.submitClaim(request, claimant);
            return ResponseEntity.ok(ApiResponse.ok("Ownership claim submitted successfully", claim));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Claim>>> getMyClaims() {
        try {
            User claimant = authService.getCurrentAuthenticatedUser();
            List<Claim> claims = claimService.getUserClaims(claimant);
            return ResponseEntity.ok(ApiResponse.ok("User claims retrieved", claims));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Claim>> getClaimById(@PathVariable Long id) {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            Claim claim = claimService.getClaimById(id);
            if (!claim.getClaimant().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
                return ResponseEntity.status(403).body(ApiResponse.error("Unauthorized to view this claim"));
            }
            return ResponseEntity.ok(ApiResponse.ok("Claim details retrieved", claim));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }
}
