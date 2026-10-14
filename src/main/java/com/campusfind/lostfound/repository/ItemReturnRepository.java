package com.campusfind.lostfound.repository;

import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.ItemReturn;
import com.campusfind.lostfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemReturnRepository extends JpaRepository<ItemReturn, Long> {
    Optional<ItemReturn> findByClaim(Claim claim);
    List<ItemReturn> findByReturnedToUserOrderByHandoverTimestampDesc(User user);
    List<ItemReturn> findAllByOrderByHandoverTimestampDesc();
}
