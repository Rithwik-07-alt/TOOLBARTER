package com.toolbarter.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_favorites")
@IdClass(UserFavorite.UserFavoriteId.class)
public class UserFavorite {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserFavorite() {
    }

    public UserFavorite(Long userId, Long itemId) {
        this.userId = userId;
        this.itemId = itemId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class UserFavoriteId implements Serializable {
        private Long userId;
        private Long itemId;

        public UserFavoriteId() {
        }

        public UserFavoriteId(Long userId, Long itemId) {
            this.userId = userId;
            this.itemId = itemId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserFavoriteId that = (UserFavoriteId) o;
            return Objects.equals(userId, that.userId) && Objects.equals(itemId, that.itemId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, itemId);
        }
    }
}
