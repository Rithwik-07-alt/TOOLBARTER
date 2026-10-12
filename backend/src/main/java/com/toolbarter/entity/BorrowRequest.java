package com.toolbarter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toolbarter.enums.BorrowStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonIgnore
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    @JsonIgnore
    private User borrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    @JsonIgnore
    private AvailabilitySlot slot;

    @Column(name = "start_ts", nullable = false)
    private LocalDateTime startTs;

    @Column(name = "end_ts", nullable = false)
    private LocalDateTime endTs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BorrowStatus status = BorrowStatus.PENDING;

    @Column(name = "points_awarded", nullable = false)
    private Boolean pointsAwarded = false;

    @Column(name = "pickup_confirmed_at")
    private LocalDateTime pickupConfirmedAt;

    @Column(name = "return_confirmed_at")
    private LocalDateTime returnConfirmedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BorrowRequest() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = BorrowStatus.PENDING;
        }
        if (pointsAwarded == null) {
            pointsAwarded = false;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getBorrower() {
        return borrower;
    }

    public void setBorrower(User borrower) {
        this.borrower = borrower;
    }

    public AvailabilitySlot getSlot() {
        return slot;
    }

    public void setSlot(AvailabilitySlot slot) {
        this.slot = slot;
    }

    public LocalDateTime getStartTs() {
        return startTs;
    }

    public void setStartTs(LocalDateTime startTs) {
        this.startTs = startTs;
    }

    public LocalDateTime getEndTs() {
        return endTs;
    }

    public void setEndTs(LocalDateTime endTs) {
        this.endTs = endTs;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }

    public Boolean getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(Boolean pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public LocalDateTime getPickupConfirmedAt() {
        return pickupConfirmedAt;
    }

    public void setPickupConfirmedAt(LocalDateTime pickupConfirmedAt) {
        this.pickupConfirmedAt = pickupConfirmedAt;
    }

    public LocalDateTime getReturnConfirmedAt() {
        return returnConfirmedAt;
    }

    public void setReturnConfirmedAt(LocalDateTime returnConfirmedAt) {
        this.returnConfirmedAt = returnConfirmedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
