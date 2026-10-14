package com.campusfind.lostfound.repository;

import com.campusfind.lostfound.model.Category;
import com.campusfind.lostfound.model.FoundItem;
import com.campusfind.lostfound.model.FoundStatus;
import com.campusfind.lostfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {

    List<FoundItem> findByUserOrderByCreatedAtDesc(User user);

    List<FoundItem> findByStatusOrderByCreatedAtDesc(FoundStatus status);

    List<FoundItem> findAllByOrderByCreatedAtDesc();

    long countByStatus(FoundStatus status);

    @Query("SELECT f FROM FoundItem f WHERE " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:category IS NULL OR f.category = :category) AND " +
           "(:query IS NULL OR LOWER(f.itemName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(f.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(f.colorFeatures) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "  OR LOWER(f.locationFound) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:location IS NULL OR LOWER(f.locationFound) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:startDate IS NULL OR f.dateFound >= :startDate) AND " +
           "(:endDate IS NULL OR f.dateFound <= :endDate) " +
           "ORDER BY f.createdAt DESC")
    List<FoundItem> searchAndFilter(
            @Param("status") FoundStatus status,
            @Param("category") Category category,
            @Param("query") String query,
            @Param("location") String location,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<FoundItem> findByCategoryAndStatus(Category category, FoundStatus status);
}
