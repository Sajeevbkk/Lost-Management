package com.campusfind.lostfound.dao;

import com.campusfind.lostfound.config.DatabaseManager;
import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.ClaimStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClaimDao {

    private final FoundItemDao foundItemDao = new FoundItemDao();
    private final UserDao userDao = new UserDao();

    public Claim findById(Long id) {
        if (id == null) return null;
        String sql = "SELECT * FROM claims WHERE claim_id = ?";
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

    public List<Claim> findAll() {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM claims ORDER BY submitted_at DESC";
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

    public List<Claim> findByClaimantId(Long claimantId) {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM claims WHERE claimant_id = ? ORDER BY submitted_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, claimantId);
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

    public List<Claim> findByFoundItemId(Long foundId) {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM claims WHERE found_id = ? ORDER BY submitted_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, foundId);
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

    public List<Claim> findByStatus(ClaimStatus status) {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM claims WHERE status = ? ORDER BY submitted_at DESC";
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

    public Claim save(Claim claim) {
        if (claim.getClaimId() == null) {
            String sql = "INSERT INTO claims (found_id, claimant_id, item_description_proof, unique_identifying_features, approx_date_lost, approx_location_lost, supporting_proof_details, status, admin_remarks, submitted_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, claim.getFoundItem() != null ? claim.getFoundItem().getFoundId() : 1);
                ps.setLong(2, claim.getClaimant() != null ? claim.getClaimant().getUserId() : 1);
                ps.setString(3, claim.getItemDescriptionProof());
                ps.setString(4, claim.getUniqueIdentifyingFeatures());
                ps.setDate(5, claim.getApproxDateLost() != null ? Date.valueOf(claim.getApproxDateLost()) : Date.valueOf(LocalDate.now()));
                ps.setString(6, claim.getApproxLocationLost());
                ps.setString(7, claim.getSupportingProofDetails());
                ps.setString(8, claim.getStatus() != null ? claim.getStatus().name() : ClaimStatus.PENDING.name());
                ps.setString(9, claim.getAdminRemarks());
                ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        claim.setClaimId(rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE claims SET item_description_proof = ?, unique_identifying_features = ?, approx_date_lost = ?, approx_location_lost = ?, supporting_proof_details = ?, status = ?, admin_remarks = ?, updated_at = ? WHERE claim_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, claim.getItemDescriptionProof());
                ps.setString(2, claim.getUniqueIdentifyingFeatures());
                ps.setDate(3, claim.getApproxDateLost() != null ? Date.valueOf(claim.getApproxDateLost()) : Date.valueOf(LocalDate.now()));
                ps.setString(4, claim.getApproxLocationLost());
                ps.setString(5, claim.getSupportingProofDetails());
                ps.setString(6, claim.getStatus() != null ? claim.getStatus().name() : ClaimStatus.PENDING.name());
                ps.setString(7, claim.getAdminRemarks());
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.setLong(9, claim.getClaimId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return claim;
    }

    public void updateStatusAndRemarks(Long claimId, ClaimStatus status, String adminRemarks) {
        String sql = "UPDATE claims SET status = ?, admin_remarks = ?, updated_at = ? WHERE claim_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, adminRemarks);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(4, claimId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Claim mapRow(ResultSet rs) throws SQLException {
        Claim claim = new Claim();
        claim.setClaimId(rs.getLong("claim_id"));
        long foundId = rs.getLong("found_id");
        claim.setFoundItem(foundItemDao.findById(foundId));
        long claimantId = rs.getLong("claimant_id");
        claim.setClaimant(userDao.findById(claimantId));
        claim.setItemDescriptionProof(rs.getString("item_description_proof"));
        claim.setUniqueIdentifyingFeatures(rs.getString("unique_identifying_features"));
        Date d = rs.getDate("approx_date_lost");
        claim.setApproxDateLost(d != null ? d.toLocalDate() : LocalDate.now());
        claim.setApproxLocationLost(rs.getString("approx_location_lost"));
        claim.setSupportingProofDetails(rs.getString("supporting_proof_details"));
        String st = rs.getString("status");
        claim.setStatus(st != null ? ClaimStatus.valueOf(st) : ClaimStatus.PENDING);
        claim.setAdminRemarks(rs.getString("admin_remarks"));
        Timestamp subTs = rs.getTimestamp("submitted_at");
        claim.setSubmittedAt(subTs != null ? subTs.toLocalDateTime() : LocalDateTime.now());
        Timestamp upTs = rs.getTimestamp("updated_at");
        claim.setUpdatedAt(upTs != null ? upTs.toLocalDateTime() : LocalDateTime.now());
        return claim;
    }
}
