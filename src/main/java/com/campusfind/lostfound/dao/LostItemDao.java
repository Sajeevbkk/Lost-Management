package com.campusfind.lostfound.dao;

import com.campusfind.lostfound.config.DatabaseManager;
import com.campusfind.lostfound.model.Category;
import com.campusfind.lostfound.model.LostItem;
import com.campusfind.lostfound.model.LostStatus;
import com.campusfind.lostfound.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LostItemDao {

    private final UserDao userDao = new UserDao();

    public LostItem findById(Long id) {
        if (id == null) return null;
        String sql = "SELECT * FROM lost_items WHERE lost_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<LostItem> findAll() {
        List<LostItem> list = new ArrayList<>();
        String sql = "SELECT * FROM lost_items ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<LostItem> findByUserId(Long userId) {
        List<LostItem> list = new ArrayList<>();
        String sql = "SELECT * FROM lost_items WHERE user_id = ? ORDER BY created_at DESC";
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

    public List<LostItem> findByStatus(LostStatus status) {
        List<LostItem> list = new ArrayList<>();
        String sql = "SELECT * FROM lost_items WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
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

    public List<LostItem> searchAndFilter(String keyword, Category category, LostStatus status, String location) {
        List<LostItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM lost_items WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(item_name) LIKE ? OR LOWER(description) LIKE ? OR LOWER(color_features) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (category != null) {
            sql.append("AND category = ? ");
            params.add(category.name());
        }

        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status.name());
        }

        if (location != null && !location.trim().isEmpty()) {
            sql.append("AND LOWER(location_lost) LIKE ? ");
            params.add("%" + location.trim().toLowerCase() + "%");
        }

        sql.append("ORDER BY created_at DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    public LostItem save(LostItem item) {
        if (item.getLostId() == null) {
            String sql = "INSERT INTO lost_items (user_id, item_name, category, description, date_lost, location_lost, color_features, image_url, contact_phone, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, item.getUser() != null ? item.getUser().getUserId() : 1);
                ps.setString(2, item.getItemName());
                ps.setString(3, item.getCategory() != null ? item.getCategory().name() : Category.OTHER.name());
                ps.setString(4, item.getDescription());
                ps.setDate(5, item.getDateLost() != null ? Date.valueOf(item.getDateLost()) : Date.valueOf(LocalDate.now()));
                ps.setString(6, item.getLocationLost());
                ps.setString(7, item.getColorFeatures());
                ps.setString(8, item.getImageUrl());
                ps.setString(9, item.getContactPhone());
                ps.setString(10, item.getStatus() != null ? item.getStatus().name() : LostStatus.REPORTED.name());
                ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setLostId(rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE lost_items SET item_name = ?, category = ?, description = ?, date_lost = ?, location_lost = ?, color_features = ?, image_url = ?, contact_phone = ?, status = ? WHERE lost_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getItemName());
                ps.setString(2, item.getCategory() != null ? item.getCategory().name() : Category.OTHER.name());
                ps.setString(3, item.getDescription());
                ps.setDate(4, item.getDateLost() != null ? Date.valueOf(item.getDateLost()) : Date.valueOf(LocalDate.now()));
                ps.setString(5, item.getLocationLost());
                ps.setString(6, item.getColorFeatures());
                ps.setString(7, item.getImageUrl());
                ps.setString(8, item.getContactPhone());
                ps.setString(9, item.getStatus() != null ? item.getStatus().name() : LostStatus.REPORTED.name());
                ps.setLong(10, item.getLostId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return item;
    }

    public void updateStatus(Long lostId, LostStatus status) {
        String sql = "UPDATE lost_items SET status = ? WHERE lost_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, lostId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private LostItem mapRow(ResultSet rs) throws SQLException {
        LostItem item = new LostItem();
        item.setLostId(rs.getLong("lost_id"));
        long userId = rs.getLong("user_id");
        item.setUser(userDao.findById(userId));
        item.setItemName(rs.getString("item_name"));
        String catStr = rs.getString("category");
        item.setCategory(catStr != null ? Category.valueOf(catStr) : Category.OTHER);
        item.setDescription(rs.getString("description"));
        Date d = rs.getDate("date_lost");
        item.setDateLost(d != null ? d.toLocalDate() : LocalDate.now());
        item.setLocationLost(rs.getString("location_lost"));
        item.setColorFeatures(rs.getString("color_features"));
        item.setImageUrl(rs.getString("image_url"));
        item.setContactPhone(rs.getString("contact_phone"));
        String st = rs.getString("status");
        item.setStatus(st != null ? LostStatus.valueOf(st) : LostStatus.REPORTED);
        Timestamp ts = rs.getTimestamp("created_at");
        item.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return item;
    }
}
