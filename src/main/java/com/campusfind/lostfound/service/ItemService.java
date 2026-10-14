package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dao.FoundItemDao;
import com.campusfind.lostfound.dao.LostItemDao;
import com.campusfind.lostfound.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ItemService {

    private static final ItemService INSTANCE = new ItemService();
    private final LostItemDao lostItemDao = new LostItemDao();
    private final FoundItemDao foundItemDao = new FoundItemDao();
    private final MatchingService matchingService = MatchingService.getInstance();
    private final List<Runnable> changeListeners = new ArrayList<>();

    private ItemService() {}

    public static ItemService getInstance() {
        return INSTANCE;
    }

    public LostItem reportLostItem(User user, String itemName, Category category, String description,
                                  LocalDate dateLost, String locationLost, String colorFeatures,
                                  String contactPhone, String imageUrl) {
        LostItem item = new LostItem();
        item.setUser(user);
        item.setItemName(itemName);
        item.setCategory(category);
        item.setDescription(description);
        item.setDateLost(dateLost);
        item.setLocationLost(locationLost);
        item.setColorFeatures(colorFeatures);
        item.setContactPhone(contactPhone);
        item.setImageUrl(imageUrl);
        item.setStatus(LostStatus.REPORTED);

        LostItem saved = lostItemDao.save(item);

        // Run smart match scan
        matchingService.scanMatchesForLostItem(saved);

        notifyChangeListeners();
        return saved;
    }

    public FoundItem reportFoundItem(User user, String itemName, Category category, String description,
                                    LocalDate dateFound, String locationFound, String colorFeatures,
                                    String storageLocation, String imageUrl) {
        FoundItem item = new FoundItem();
        item.setUser(user);
        item.setItemName(itemName);
        item.setCategory(category);
        item.setDescription(description);
        item.setDateFound(dateFound);
        item.setLocationFound(locationFound);
        item.setColorFeatures(colorFeatures);
        item.setStorageLocation(storageLocation);
        item.setImageUrl(imageUrl);
        item.setStatus(FoundStatus.AVAILABLE);

        FoundItem saved = foundItemDao.save(item);

        // Run smart match scan
        matchingService.scanMatchesForFoundItem(saved);

        notifyChangeListeners();
        return saved;
    }

    public List<LostItem> getAllLostItems() {
        return lostItemDao.findAll();
    }

    public List<LostItem> getLostItemsByUser(Long userId) {
        return lostItemDao.findByUserId(userId);
    }

    public List<LostItem> searchLostItems(String query, Category category, LostStatus status, String location) {
        return lostItemDao.searchAndFilter(query, category, status, location);
    }

    public LostItem getLostItemById(Long id) {
        return lostItemDao.findById(id);
    }

    public void updateLostStatus(Long lostId, LostStatus status) {
        lostItemDao.updateStatus(lostId, status);
        notifyChangeListeners();
    }

    public List<FoundItem> getAllFoundItems() {
        return foundItemDao.findAll();
    }

    public List<FoundItem> getFoundItemsByUser(Long userId) {
        return foundItemDao.findByUserId(userId);
    }

    public List<FoundItem> searchFoundItems(String query, Category category, FoundStatus status, String location) {
        return foundItemDao.searchAndFilter(query, category, status, location);
    }

    public FoundItem getFoundItemById(Long id) {
        return foundItemDao.findById(id);
    }

    public void updateFoundStatus(Long foundId, FoundStatus status) {
        foundItemDao.updateStatus(foundId, status);
        notifyChangeListeners();
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    private void notifyChangeListeners() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
