package com.campusfind.lostfound.model;

import java.time.LocalDateTime;

public class Notification {
    private Long notificationId;
    private User user;
    private String title;
    private String message;
    private NotificationType type;
    private Long referenceId;
    private Boolean isRead = false;
    private LocalDateTime createdAt;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public Notification(User user, String title, String message, NotificationType type, Long referenceId) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Boolean getIsRead() {
        return isRead != null ? isRead : false;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
