package com.campusfind.lostfound.repository;

import com.campusfind.lostfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCollegeEmail(String collegeEmail);
    Optional<User> findByCampusId(String campusId);
    boolean existsByCollegeEmail(String collegeEmail);
    boolean existsByCampusId(String campusId);
}
