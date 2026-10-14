package com.campusfind.lostfound.dao;

import com.campusfind.lostfound.config.DatabaseManager;
import com.campusfind.lostfound.model.Category;
import com.campusfind.lostfound.model.FoundItem;
import com.campusfind.lostfound.model.FoundStatus;
import com.campusfind.lostfound.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FoundItemDao {

    private final UserDao userDao = new UserDao();

    public FoundItem findById(Long id) {
        if (id == null) return null;
        String sql = "SELECT * FROM found_items WHERE found_id = ?";
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

    public List<FoundItem> findAll() {
        List<FoundItem> list = new ArrayList<>();
        String sql = "SELECT * FROM found_items ORDER BY created_at DESC";
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

    public List<FoundItem> findByUserId(Long userId) {
        List<FoundItem> list = new ArrayList<>();
        String sql = "SELECT * FROM found_items WHERE user_id = ? ORDER BY created_at DESC";
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

    public List<FoundItem> findByStatus(FoundStatus status) {
        List<FoundItem> list = new ArrayList<>();
        String sql = "SELECT * FROM found_items WHERE status = ? ORDER BY created_at DESC";
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

    public List<FoundItem> searchAndFilter(String keyword, Category category, FoundStatus status, String location) {
        List<FoundItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM found_items WHERE 1=1 ");
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
            sql.append("AND (LOWER(location_found) LIKE ? OR LOWER(storage_location) LIKE ?) ");
            String locTerm = "%" + location.trim().toLowerCase() + "%";
            params.add(locTerm);
            params.add(locTerm);
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

    public FoundItem save(FoundItem item) {
        if (item.getFoundId() == null) {
            String sql = "INSERT INTO found_items (user_id, item_name, category, description, date_found, location_found, color_features, image_url, storage_location, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, item.getUser() != null ? item.getUser().getUserId() : 1);
                ps.setString(2, item.getItemName());
                ps.setString(3, item.getCategory() != null ? item.getCategory().name() : Category.OTHER.name());
                ps.setString(4, item.getDescription());
                ps.setDate(5, item.getDateFound() != null ? Date.valueOf(item.getDateFound()) : Date.valueOf(LocalDate.now()));
                ps.setString(6, item.getLocationFound());
                ps.setString(7, item.getColorFeatures());
                ps.setString(8, item.getImageUrl());
                ps.setString(9, item.getStorageLocation());
                ps.setString(10, item.getStatus() != null ? item.getStatus().name() : FoundStatus.AVAILABLE.name());
                ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setFoundId(rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE found_items SET item_name = ?, category = ?, description = ?, date_found = ?, location_found = ?, color_features = ?, image_url = ?, storage_location = ?, status = ? WHERE found_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getItemName());
                ps.setString(2, item.getCategory() != null ? item.getCategory().name() : Category.OTHER.name());
                ps.setString(3, item.getDescription());
                ps.setDate(4, item.getDateFound() != null ? Date.valueOf(item.getDateFound()) : Date.valueOf(LocalDate.now()));
                ps.setString(5, item.getLocationFound());
                ps.setString(6, item.getColorFeatures());
                ps.setString(7, item.getImageUrl());
                ps.setString(8, item.getStorageLocation());
                ps.setString(9, item.getStatus() != null ? item.getStatus().name() : FoundStatus.AVAILABLE.name());
                ps.setLong(10, item.getFoundId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return item;
    }

    public void updateStatus(Long foundId, FoundStatus status) {
        String sql = "UPDATE found_items SET status = ? WHERE found_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, foundId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private FoundItem mapRow(ResultSet rs) throws SQLException {
        FoundItem item = new FoundItem();
        item.setFoundId(rs.getLong("found_id"));
        long userId = rs.getLong("user_id");
        item.setUser(userDao.findById(userId));
        item.setItemName(rs.getString("item_name"));
        String catStr = rs.getString("category");
        item.setCategory(catStr != null ? Category.valueOf(catStr) : Category.OTHER);
        item.setDescription(rs.getString("description"));
        Date d = rs.getDate("date_found");
        item.setDateFound(d != null ? d.toLocalDate() : LocalDate.now());
        item.setLocationFound(rs.getString("location_found"));
        item.setColorFeatures(rs.getString("color_features"));
        item.setImageUrl(rs.getString("image_url"));
        item.setStorageLocation(rs.getString("storage_location"));
        String st = rs.getString("status");
        item.setStatus(st != null ? FoundStatus.valueOf(st) : FoundStatus.AVAILABLE);
        Timestamp ts = rs.getTimestamp("created_at");
        item.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return item;
    }
}
