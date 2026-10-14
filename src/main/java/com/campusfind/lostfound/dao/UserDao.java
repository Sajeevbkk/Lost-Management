package com.campusfind.lostfound.dao;

import com.campusfind.lostfound.config.DatabaseManager;
import com.campusfind.lostfound.model.Role;
import com.campusfind.lostfound.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public User findById(Long userId) {
        if (userId == null) return null;
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
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

    public User findByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT * FROM users WHERE LOWER(college_email) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
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

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
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

    public User save(User user) {
        if (user.getUserId() == null) {
            String sql = "INSERT INTO users (full_name, college_email, campus_id, department, phone_number, password_hash, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getFullName());
                ps.setString(2, user.getCollegeEmail());
                ps.setString(3, user.getCampusId());
                ps.setString(4, user.getDepartment());
                ps.setString(5, user.getPhoneNumber());
                ps.setString(6, user.getPasswordHash());
                ps.setString(7, user.getRole() != null ? user.getRole().name() : Role.STUDENT.name());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE users SET full_name = ?, college_email = ?, campus_id = ?, department = ?, phone_number = ?, password_hash = ?, role = ? WHERE user_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getFullName());
                ps.setString(2, user.getCollegeEmail());
                ps.setString(3, user.getCampusId());
                ps.setString(4, user.getDepartment());
                ps.setString(5, user.getPhoneNumber());
                ps.setString(6, user.getPasswordHash());
                ps.setString(7, user.getRole() != null ? user.getRole().name() : Role.STUDENT.name());
                ps.setLong(8, user.getUserId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    public boolean existsByCampusId(String campusId) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(campus_id) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campusId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getLong("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setCollegeEmail(rs.getString("college_email"));
        u.setCampusId(rs.getString("campus_id"));
        u.setDepartment(rs.getString("department"));
        u.setPhoneNumber(rs.getString("phone_number"));
        u.setPasswordHash(rs.getString("password_hash"));
        String roleStr = rs.getString("role");
        u.setRole(roleStr != null ? Role.valueOf(roleStr) : Role.STUDENT);
        Timestamp ts = rs.getTimestamp("created_at");
        u.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return u;
    }
}
