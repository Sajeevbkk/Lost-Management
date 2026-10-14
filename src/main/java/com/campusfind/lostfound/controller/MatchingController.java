package com.campusfind.lostfound.controller;

import com.campusfind.lostfound.dto.ApiResponse;
import com.campusfind.lostfound.dto.MatchResultDto;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchingController {

    private final MatchingService matchingService;
    private final AuthService authService;

    public MatchingController(MatchingService matchingService, AuthService authService) {
        this.matchingService = matchingService;
        this.authService = authService;
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MatchResultDto>>> getMyMatches() {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            List<MatchResultDto> matches = matchingService.getMatchesForUser(user);
            return ResponseEntity.ok(ApiResponse.ok("User potential matches retrieved", matches));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<MatchResultDto>>> getPublicMatches() {
        List<MatchResultDto> matches = matchingService.getAllPotentialMatches();
        return ResponseEntity.ok(ApiResponse.ok("Active potential matches retrieved", matches));
    }
}
