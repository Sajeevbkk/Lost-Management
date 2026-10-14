package com.campusfind.lostfound.controller;

import com.campusfind.lostfound.dto.ApiResponse;
import com.campusfind.lostfound.model.Notification;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications() {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            List<Notification> list = notificationService.getUserNotifications(user);
            return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", list));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            long count = notificationService.getUnreadCount(user);
            return ResponseEntity.ok(ApiResponse.ok("Unread count retrieved", Map.of("unreadCount", count)));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            notificationService.markAsRead(id, user);
            return ResponseEntity.ok(ApiResponse.ok("Notification marked as read"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        try {
            User user = authService.getCurrentAuthenticatedUser();
            notificationService.markAllAsRead(user);
            return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }
}
