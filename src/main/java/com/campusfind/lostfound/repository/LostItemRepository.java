package com.campusfind.lostfound.repository;

import com.campusfind.lostfound.model.Category;
import com.campusfind.lostfound.model.LostItem;
import com.campusfind.lostfound.model.LostStatus;
import com.campusfind.lostfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    List<LostItem> findByUserOrderByCreatedAtDesc(User user);

    List<LostItem> findByStatusOrderByCreatedAtDesc(LostStatus status);

    List<LostItem> findAllByOrderByCreatedAtDesc();

    long countByStatus(LostStatus status);

    @Query("SELECT l FROM LostItem l WHERE " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:category IS NULL OR l.category = :category) AND " +
           "(:query IS NULL OR LOWER(l.itemName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(l.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(l.colorFeatures) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(l.locationLost) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:location IS NULL OR LOWER(l.locationLost) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:startDate IS NULL OR l.dateLost >= :startDate) AND " +
           "(:endDate IS NULL OR l.dateLost <= :endDate) " +
           "ORDER BY l.createdAt DESC")
    List<LostItem> searchAndFilter(
            @Param("status") LostStatus status,
            @Param("category") Category category,
            @Param("query") String query,
            @Param("location") String location,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<LostItem> findByCategoryAndStatus(Category category, LostStatus status);
}
