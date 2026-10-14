package com.campusfind.lostfound.dao;

import com.campusfind.lostfound.config.DatabaseManager;
import com.campusfind.lostfound.model.Notification;
import com.campusfind.lostfound.model.NotificationType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    private final UserDao userDao = new UserDao();

    public List<Notification> findByUserId(Long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countUnreadByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void markAsRead(Long notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAllAsRead(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Notification save(Notification notif) {
        String sql = "INSERT INTO notifications (user_id, title, message, type, reference_id, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, notif.getUser() != null ? notif.getUser().getUserId() : 1);
            ps.setString(2, notif.getTitle());
            ps.setString(3, notif.getMessage());
            ps.setString(4, notif.getType() != null ? notif.getType().name() : NotificationType.SYSTEM_ALERT.name());
            if (notif.getReferenceId() != null) {
                ps.setLong(5, notif.getReferenceId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.setBoolean(6, notif.getIsRead());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    notif.setNotificationId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notif;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getLong("notification_id"));
        long userId = rs.getLong("user_id");
        n.setUser(userDao.findById(userId));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        String typeStr = rs.getString("type");
        n.setType(typeStr != null ? NotificationType.valueOf(typeStr) : NotificationType.SYSTEM_ALERT);
        long ref = rs.getLong("reference_id");
        if (!rs.wasNull()) {
            n.setReferenceId(ref);
        }
        n.setIsRead(rs.getBoolean("is_read"));
        Timestamp ts = rs.getTimestamp("created_at");
        n.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return n;
    }
}
