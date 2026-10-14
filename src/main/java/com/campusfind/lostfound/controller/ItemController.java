package com.campusfind.lostfound.controller;

import com.campusfind.lostfound.dto.ApiResponse;
import com.campusfind.lostfound.dto.FoundItemRequest;
import com.campusfind.lostfound.dto.LostItemRequest;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;
    private final AuthService authService;

    public ItemController(ItemService itemService, AuthService authService) {
        this.itemService = itemService;
        this.authService = authService;
    }

    // --- LOST ITEMS ---

    @GetMapping("/lost")
    public ResponseEntity<ApiResponse<List<LostItem>>> getLostItems(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) LostStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<LostItem> items = itemService.searchLostItems(category, status, query, location, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Lost items retrieved successfully", items));
    }

    @PostMapping("/lost")
    public ResponseEntity<ApiResponse<LostItem>> reportLostItem(@Valid @RequestBody LostItemRequest request) {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            LostItem item = itemService.reportLost(request, user);
            return ResponseEntity.ok(ApiResponse.ok("Lost item reported successfully", item));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/lost/{id}")
    public ResponseEntity<ApiResponse<LostItem>> getLostItemById(@PathVariable Long id) {
        try {
            LostItem item = itemService.getLostItemById(id);
            return ResponseEntity.ok(ApiResponse.ok("Lost item details retrieved", item));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @DeleteMapping("/lost/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLostItem(@PathVariable Long id) {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            itemService.deleteLostItem(id, user);
            return ResponseEntity.ok(ApiResponse.ok("Lost item report deleted"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    // --- FOUND ITEMS ---

    @GetMapping("/found")
    public ResponseEntity<ApiResponse<List<FoundItem>>> getFoundItems(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) FoundStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<FoundItem> items = itemService.searchFoundItems(category, status, query, location, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok("Found items retrieved successfully", items));
    }

    @PostMapping("/found")
    public ResponseEntity<ApiResponse<FoundItem>> reportFoundItem(@Valid @RequestBody FoundItemRequest request) {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            FoundItem item = itemService.reportFound(request, user);
            return ResponseEntity.ok(ApiResponse.ok("Found item reported successfully", item));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/found/{id}")
    public ResponseEntity<ApiResponse<FoundItem>> getFoundItemById(@PathVariable Long id) {
        try {
            FoundItem item = itemService.getFoundItemById(id);
            return ResponseEntity.ok(ApiResponse.ok("Found item details retrieved", item));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @DeleteMapping("/found/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFoundItem(@PathVariable Long id) {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            itemService.deleteFoundItem(id, user);
            return ResponseEntity.ok(ApiResponse.ok("Found item report deleted"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    // --- USER ITEMS ---

    @GetMapping("/my/lost")
    public ResponseEntity<ApiResponse<List<LostItem>>> getMyLostItems() {
        User user = authService.getCurrentAuthenticatedUser();
        List<LostItem> items = itemService.getUserLostItems(user);
        return ResponseEntity.ok(ApiResponse.ok("User lost items retrieved", items));
    }

    @GetMapping("/my/found")
    public ResponseEntity<ApiResponse<List<FoundItem>>> getMyFoundItems() {
        User user = authService.getCurrentAuthenticatedUser();
        List<FoundItem> items = itemService.getUserFoundItems(user);
        return ResponseEntity.ok(ApiResponse.ok("User found items retrieved", items));
    }
}
