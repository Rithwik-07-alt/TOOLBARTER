package com.toolbarter.dto;

import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.enums.ItemCategory;
import com.toolbarter.enums.TrustLevel;
import java.time.LocalDateTime;

public class BorrowRequestResponse {

    private Long id;

    // Item Info
    private Long itemId;
    private String itemTitle;
    private ItemCategory itemCategory;
    private String itemCategoryDisplay;
    private String itemPhotoUrl;
    private Double itemLatitude;
    private Double itemLongitude;

    // Owner Info
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
    private Integer ownerTrustScore;
    private TrustLevel ownerTrustLevel;
    private String ownerTrustLevelDisplay;

    // Borrower Info
    private Long borrowerId;
    private String borrowerName;
    private String borrowerPhone;
    private Integer borrowerTrustScore;
    private TrustLevel borrowerTrustLevel;
    private String borrowerTrustLevelDisplay;

    // Schedule & Status
    private Long slotId;
    private LocalDateTime startTs;
    private LocalDateTime endTs;
    private BorrowStatus status;
    private Boolean pointsAwarded;
    private LocalDateTime pickupConfirmedAt;
    private LocalDateTime returnConfirmedAt;
    private LocalDateTime createdAt;

    // Dynamic Context
    private Double distanceKm;
    private Boolean canReview = false;
    private Boolean hasReviewed = false;

    public BorrowRequestResponse() {
    }

    public static BorrowRequestResponse fromEntity(BorrowRequest request) {
        BorrowRequestResponse dto = new BorrowRequestResponse();
        dto.setId(request.getId());

        if (request.getItem() != null) {
            dto.setItemId(request.getItem().getId());
            dto.setItemTitle(request.getItem().getTitle());
            dto.setItemCategory(request.getItem().getCategory());
            dto.setItemCategoryDisplay(request.getItem().getCategory() != null ?
                    request.getItem().getCategory().getDisplayName() : "Other");
            dto.setItemPhotoUrl(request.getItem().getPhotoUrl());
            dto.setItemLatitude(request.getItem().getLatitude());
            dto.setItemLongitude(request.getItem().getLongitude());

            if (request.getItem().getOwner() != null) {
                dto.setOwnerId(request.getItem().getOwner().getId());
                dto.setOwnerName(request.getItem().getOwner().getName());
                dto.setOwnerPhone(request.getItem().getOwner().getPhone());
                dto.setOwnerTrustScore(request.getItem().getOwner().getTrustScore());
                dto.setOwnerTrustLevel(request.getItem().getOwner().getTrustLevel());
                dto.setOwnerTrustLevelDisplay(request.getItem().getOwner().getTrustLevel() != null ?
                        request.getItem().getOwner().getTrustLevel().getDisplayName() : "New Neighbour");
            }
        }

        if (request.getBorrower() != null) {
            dto.setBorrowerId(request.getBorrower().getId());
            dto.setBorrowerName(request.getBorrower().getName());
            dto.setBorrowerPhone(request.getBorrower().getPhone());
            dto.setBorrowerTrustScore(request.getBorrower().getTrustScore());
            dto.setBorrowerTrustLevel(request.getBorrower().getTrustLevel());
            dto.setBorrowerTrustLevelDisplay(request.getBorrower().getTrustLevel() != null ?
                    request.getBorrower().getTrustLevel().getDisplayName() : "New Neighbour");
        }

        if (request.getSlot() != null) {
            dto.setSlotId(request.getSlot().getId());
        }

        dto.setStartTs(request.getStartTs());
        dto.setEndTs(request.getEndTs());
        dto.setStatus(request.getStatus());
        dto.setPointsAwarded(request.getPointsAwarded());
        dto.setPickupConfirmedAt(request.getPickupConfirmedAt());
        dto.setReturnConfirmedAt(request.getReturnConfirmedAt());
        dto.setCreatedAt(request.getCreatedAt());

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public ItemCategory getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(ItemCategory itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemCategoryDisplay() {
        return itemCategoryDisplay;
    }

    public void setItemCategoryDisplay(String itemCategoryDisplay) {
        this.itemCategoryDisplay = itemCategoryDisplay;
    }

    public String getItemPhotoUrl() {
        return itemPhotoUrl;
    }

    public void setItemPhotoUrl(String photoUrl) {
        this.itemPhotoUrl = photoUrl;
    }

    public Double getItemLatitude() {
        return itemLatitude;
    }

    public void setItemLatitude(Double itemLatitude) {
        this.itemLatitude = itemLatitude;
    }

    public Double getItemLongitude() {
        return itemLongitude;
    }

    public void setItemLongitude(Double itemLongitude) {
        this.itemLongitude = itemLongitude;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public Integer getOwnerTrustScore() {
        return ownerTrustScore;
    }

    public void setOwnerTrustScore(Integer ownerTrustScore) {
        this.ownerTrustScore = ownerTrustScore;
    }

    public TrustLevel getOwnerTrustLevel() {
        return ownerTrustLevel;
    }

    public void setOwnerTrustLevel(TrustLevel ownerTrustLevel) {
        this.ownerTrustLevel = ownerTrustLevel;
    }

    public String getOwnerTrustLevelDisplay() {
        return ownerTrustLevelDisplay;
    }

    public void setOwnerTrustLevelDisplay(String ownerTrustLevelDisplay) {
        this.ownerTrustLevelDisplay = ownerTrustLevelDisplay;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getBorrowerPhone() {
        return borrowerPhone;
    }

    public void setBorrowerPhone(String borrowerPhone) {
        this.borrowerPhone = borrowerPhone;
    }

    public Integer getBorrowerTrustScore() {
        return borrowerTrustScore;
    }

    public void setBorrowerTrustScore(Integer borrowerTrustScore) {
        this.borrowerTrustScore = borrowerTrustScore;
    }

    public TrustLevel getBorrowerTrustLevel() {
        return borrowerTrustLevel;
    }

    public void setBorrowerTrustLevel(TrustLevel borrowerTrustLevel) {
        this.borrowerTrustLevel = borrowerTrustLevel;
    }

    public String getBorrowerTrustLevelDisplay() {
        return borrowerTrustLevelDisplay;
    }

    public void setBorrowerTrustLevelDisplay(String borrowerTrustLevelDisplay) {
        this.borrowerTrustLevelDisplay = borrowerTrustLevelDisplay;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getStartTs() {
        return startTs;
    }

    public LocalDateTime getStartTime() {
        return startTs;
    }

    public void setStartTs(LocalDateTime startTs) {
        this.startTs = startTs;
    }

    public LocalDateTime getEndTs() {
        return endTs;
    }

    public LocalDateTime getEndTime() {
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

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Boolean getCanReview() {
        return canReview;
    }

    public void setCanReview(Boolean canReview) {
        this.canReview = canReview;
    }

    public Boolean getHasReviewed() {
        return hasReviewed;
    }

    public void setHasReviewed(Boolean hasReviewed) {
        this.hasReviewed = hasReviewed;
    }
}
