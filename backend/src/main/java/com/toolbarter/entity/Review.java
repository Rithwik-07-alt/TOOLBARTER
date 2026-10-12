package com.toolbarter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_request_reviewer", columnNames = {"borrow_request_id", "reviewer_id"})
    }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_request_id", nullable = false)
    @JsonIgnore
    private BorrowRequest borrowRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonIgnore
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    @JsonIgnore
    private User reviewee;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Review() {
    }

    public Review(BorrowRequest borrowRequest, User reviewer, User reviewee, Integer rating, String comment) {
        this.borrowRequest = borrowRequest;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.rating = rating;
        this.comment = comment;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BorrowRequest getBorrowRequest() {
        return borrowRequest;
    }

    public void setBorrowRequest(BorrowRequest borrowRequest) {
        this.borrowRequest = borrowRequest;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public User getReviewee() {
        return reviewee;
    }

    public void setReviewee(User reviewee) {
        this.reviewee = reviewee;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
