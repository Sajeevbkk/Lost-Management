package com.campusfind.lostfound.service;

import com.campusfind.lostfound.dao.NotificationDao;
import com.campusfind.lostfound.model.Notification;
import com.campusfind.lostfound.model.NotificationType;
import com.campusfind.lostfound.model.User;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private static final NotificationService INSTANCE = new NotificationService();
    private final NotificationDao notificationDao = new NotificationDao();
    private final List<Runnable> notificationListeners = new ArrayList<>();

    private NotificationService() {}

    public static NotificationService getInstance() {
        return INSTANCE;
    }

    public void sendNotification(User user, String title, String message, NotificationType type, Long referenceId) {
        if (user == null) return;
        Notification n = new Notification(user, title, message, type, referenceId);
        notificationDao.save(n);
        notifyListeners();
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        if (userId == null) return new ArrayList<>();
        return notificationDao.findByUserId(userId);
    }

    public int getUnreadCount(Long userId) {
        if (userId == null) return 0;
        return notificationDao.countUnreadByUserId(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationDao.markAsRead(notificationId);
        notifyListeners();
    }

    public void markAllAsRead(Long userId) {
        notificationDao.markAllAsRead(userId);
        notifyListeners();
    }

    public void addNotificationListener(Runnable listener) {
        notificationListeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : notificationListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
