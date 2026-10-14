package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dto.FoundItemRequest;
import com.campusfind.lostfound.dto.LostItemRequest;
import com.campusfind.lostfound.dto.MatchResultDto;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.repository.FoundItemRepository;
import com.campusfind.lostfound.repository.LostItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ItemService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final MatchingService matchingService;
    private final NotificationService notificationService;

    public ItemService(LostItemRepository lostItemRepository,
                       FoundItemRepository foundItemRepository,
                       MatchingService matchingService,
                       NotificationService notificationService) {
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
    }

    @Transactional
    public LostItem reportLost(LostItemRequest request, User user) {
        LostItem item = new LostItem();
        item.setUser(user);
        item.setItemName(request.getItemName().trim());
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription().trim());
        item.setDateLost(request.getDateLost());
        item.setLocationLost(request.getLocationLost().trim());
        item.setColorFeatures(request.getColorFeatures().trim());
        item.setImageUrl(request.getImageUrl());
        item.setContactPhone(request.getContactPhone().trim());
        item.setStatus(LostStatus.REPORTED);

        LostItem saved = lostItemRepository.save(item);

        // Run smart match scanning in real time
        matchingService.scanMatchesForLostItem(saved);

        // Notify user of report registration
        notificationService.sendNotification(
                user,
                "Lost Item Report Registered",
                "Your report for '" + saved.getItemName() + "' was successfully recorded. The system is scanning for matching items.",
                NotificationType.ADMIN_ALERT,
                saved.getLostId()
        );

        return saved;
    }

    @Transactional
    public FoundItem reportFound(FoundItemRequest request, User user) {
        FoundItem item = new FoundItem();
        item.setUser(user);
        item.setItemName(request.getItemName().trim());
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription().trim());
        item.setDateFound(request.getDateFound());
        item.setLocationFound(request.getLocationFound().trim());
        item.setColorFeatures(request.getColorFeatures().trim());
        item.setImageUrl(request.getImageUrl());
        item.setStorageLocation(request.getStorageLocation().trim());
        item.setStatus(FoundStatus.AVAILABLE);

        FoundItem saved = foundItemRepository.save(item);

        // Run smart match scanning in real time
        matchingService.scanMatchesForFoundItem(saved);

        notificationService.sendNotification(
                user,
                "Found Item Report Registered",
                "Thank you for reporting found item '" + saved.getItemName() + "'. It is now cataloged in the system for owners to claim.",
                NotificationType.ADMIN_ALERT,
                saved.getFoundId()
        );

        return saved;
    }

    public List<LostItem> searchLostItems(Category category, LostStatus status, String query, String location, LocalDate startDate, LocalDate endDate) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanLocation = (location != null && !location.trim().isEmpty()) ? location.trim() : null;
        return lostItemRepository.searchAndFilter(status, category, cleanQuery, cleanLocation, startDate, endDate);
    }

    public List<FoundItem> searchFoundItems(Category category, FoundStatus status, String query, String location, LocalDate startDate, LocalDate endDate) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanLocation = (location != null && !location.trim().isEmpty()) ? location.trim() : null;
        return foundItemRepository.searchAndFilter(status, category, cleanQuery, cleanLocation, startDate, endDate);
    }

    public LostItem getLostItemById(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lost item report not found with ID: " + id));
    }

    public FoundItem getFoundItemById(Long id) {
        return foundItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Found item report not found with ID: " + id));
    }

    public List<LostItem> getUserLostItems(User user) {
        return lostItemRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<FoundItem> getUserFoundItems(User user) {
        return foundItemRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public LostItem updateLostStatus(Long id, LostStatus status, User user) {
        LostItem item = getLostItemById(id);
        if (!item.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Unauthorized to modify this report");
        }
        item.setStatus(status);
        return lostItemRepository.save(item);
    }

    @Transactional
    public FoundItem updateFoundStatus(Long id, FoundStatus status, User user) {
        FoundItem item = getFoundItemById(id);
        if (!item.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Unauthorized to modify this report");
        }
        item.setStatus(status);
        return foundItemRepository.save(item);
    }

    @Transactional
    public void deleteLostItem(Long id, User user) {
        LostItem item = getLostItemById(id);
        if (!item.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Unauthorized to delete this report");
        }
        lostItemRepository.delete(item);
    }

    @Transactional
    public void deleteFoundItem(Long id, User user) {
        FoundItem item = getFoundItemById(id);
        if (!item.getUser().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Unauthorized to delete this report");
        }
        foundItemRepository.delete(item);
    }
}
