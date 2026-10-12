package com.toolbarter.repository;

import com.toolbarter.entity.Item;
import com.toolbarter.enums.ItemCategory;
import com.toolbarter.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByStatus(ItemStatus status);

    @Query("SELECT i FROM Item i WHERE " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Item> searchItems(@Param("category") ItemCategory category,
                           @Param("status") ItemStatus status,
                           @Param("keyword") String keyword);

    long countByStatus(ItemStatus status);
}
