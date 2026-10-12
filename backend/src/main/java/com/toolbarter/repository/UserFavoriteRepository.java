package com.toolbarter.repository;

import com.toolbarter.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UserFavorite.UserFavoriteId> {
    List<UserFavorite> findByUserId(Long userId);
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
    void deleteByUserIdAndItemId(Long userId, Long itemId);
}
