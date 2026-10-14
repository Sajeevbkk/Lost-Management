package com.campusfind.lostfound.dao;

import com.campusfind.lostfound.config.DatabaseManager;
import com.campusfind.lostfound.model.ItemReturn;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemReturnDao {

    private final ClaimDao claimDao = new ClaimDao();
    private final FoundItemDao foundItemDao = new FoundItemDao();
    private final UserDao userDao = new UserDao();

    public ItemReturn findById(Long id) {
        if (id == null) return null;
        String sql = "SELECT * FROM item_returns WHERE return_id = ?";
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

    public List<ItemReturn> findAll() {
        List<ItemReturn> list = new ArrayList<>();
        String sql = "SELECT * FROM item_returns ORDER BY handover_timestamp DESC";
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

    public ItemReturn save(ItemReturn itemReturn) {
        String sql = "INSERT INTO item_returns (claim_id, found_id, returned_to_user_id, processed_by_admin_id, handover_timestamp, handover_notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, itemReturn.getClaim() != null ? itemReturn.getClaim().getClaimId() : 1);
            ps.setLong(2, itemReturn.getFoundItem() != null ? itemReturn.getFoundItem().getFoundId() : 1);
            ps.setLong(3, itemReturn.getReturnedToUser() != null ? itemReturn.getReturnedToUser().getUserId() : 1);
            ps.setLong(4, itemReturn.getProcessedByAdmin() != null ? itemReturn.getProcessedByAdmin().getUserId() : 1);
            ps.setTimestamp(5, Timestamp.valueOf(itemReturn.getHandoverTimestamp() != null ? itemReturn.getHandoverTimestamp() : LocalDateTime.now()));
            ps.setString(6, itemReturn.getHandoverNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    itemReturn.setReturnId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemReturn;
    }

    private ItemReturn mapRow(ResultSet rs) throws SQLException {
        ItemReturn ir = new ItemReturn();
        ir.setReturnId(rs.getLong("return_id"));
        long claimId = rs.getLong("claim_id");
        ir.setClaim(claimDao.findById(claimId));
        long foundId = rs.getLong("found_id");
        ir.setFoundItem(foundItemDao.findById(foundId));
        long retUserId = rs.getLong("returned_to_user_id");
        ir.setReturnedToUser(userDao.findById(retUserId));
        long adminId = rs.getLong("processed_by_admin_id");
        ir.setProcessedByAdmin(userDao.findById(adminId));
        Timestamp ts = rs.getTimestamp("handover_timestamp");
        ir.setHandoverTimestamp(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        ir.setHandoverNotes(rs.getString("handover_notes"));
        return ir;
    }
}
