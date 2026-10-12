package com.toolbarter.repository;

import com.toolbarter.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    List<Review> findByBorrowRequestId(Long borrowRequestId);

    boolean existsByBorrowRequestIdAndReviewerId(Long borrowRequestId, Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double getAverageRatingForUser(@Param("userId") Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getOverallAverageRating();

    long countByRevieweeId(Long revieweeId);
}
