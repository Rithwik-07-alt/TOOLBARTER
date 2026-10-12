package com.toolbarter.dto;

import com.toolbarter.entity.Item;
import com.toolbarter.enums.ItemCategory;
import com.toolbarter.enums.ItemCondition;
import com.toolbarter.enums.ItemStatus;
import com.toolbarter.enums.TrustLevel;
import java.time.LocalDateTime;

public class ItemResponse {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private Integer ownerTrustScore;
    private TrustLevel ownerTrustLevel;
    private String ownerTrustLevelDisplay;

    private String title;
    private String description;
    private ItemCategory category;
    private String categoryDisplay;
    private ItemCondition conditionGrade;
    private String conditionGradeDisplay;
    private String conditionNotes;
    private Boolean oneTimeUse;
    private Double pickupRadiusKm;
    private Integer minDurationHours;
    private Integer maxDurationHours;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private ItemStatus status;
    private LocalDateTime createdAt;

    private Double distanceKm;
    private String nextAvailableSlot;
    private int openSlotsCount;
    private Boolean isFavorite = false;

    public ItemResponse() {
    }

    public static ItemResponse fromEntity(Item item) {
        ItemResponse dto = new ItemResponse();
        dto.setId(item.getId());
        if (item.getOwner() != null) {
            dto.setOwnerId(item.getOwner().getId());
            dto.setOwnerName(item.getOwner().getName());
            dto.setOwnerTrustScore(item.getOwner().getTrustScore());
            dto.setOwnerTrustLevel(item.getOwner().getTrustLevel());
            dto.setOwnerTrustLevelDisplay(item.getOwner().getTrustLevel() != null ?
                    item.getOwner().getTrustLevel().getDisplayName() : "New Neighbour");
        }
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setCategory(item.getCategory());
        dto.setCategoryDisplay(item.getCategory() != null ? item.getCategory().getDisplayName() : "Other");
        dto.setConditionGrade(item.getConditionGrade());
        dto.setConditionGradeDisplay(item.getConditionGrade() != null ? item.getConditionGrade().getDisplayName() : "");
        dto.setConditionNotes(item.getConditionNotes());
        dto.setOneTimeUse(item.getOneTimeUse());
        dto.setPickupRadiusKm(item.getPickupRadiusKm());
        dto.setMinDurationHours(item.getMinDurationHours());
        dto.setMaxDurationHours(item.getMaxDurationHours());
        dto.setPhotoUrl(item.getPhotoUrl());
        dto.setLatitude(item.getLatitude());
        dto.setLongitude(item.getLongitude());
        dto.setStatus(item.getStatus());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public String getCategoryDisplay() {
        return categoryDisplay;
    }

    public void setCategoryDisplay(String categoryDisplay) {
        this.categoryDisplay = categoryDisplay;
    }

    public ItemCondition getConditionGrade() {
        return conditionGrade;
    }

    public ItemCondition getCondition() {
        return conditionGrade;
    }

    public void setConditionGrade(ItemCondition conditionGrade) {
        this.conditionGrade = conditionGrade;
    }

    public String getConditionGradeDisplay() {
        return conditionGradeDisplay;
    }

    public void setConditionGradeDisplay(String conditionGradeDisplay) {
        this.conditionGradeDisplay = conditionGradeDisplay;
    }

    public String getConditionNotes() {
        return conditionNotes;
    }

    public void setConditionNotes(String conditionNotes) {
        this.conditionNotes = conditionNotes;
    }

    public Boolean getOneTimeUse() {
        return oneTimeUse;
    }

    public Boolean getIsOneTimeUse() {
        return oneTimeUse;
    }

    public void setOneTimeUse(Boolean oneTimeUse) {
        this.oneTimeUse = oneTimeUse;
    }

    public Double getPickupRadiusKm() {
        return pickupRadiusKm;
    }

    public Double getPickupRadius() {
        return pickupRadiusKm;
    }

    public void setPickupRadiusKm(Double pickupRadiusKm) {
        this.pickupRadiusKm = pickupRadiusKm;
    }

    public Integer getMinDurationHours() {
        return minDurationHours;
    }

    public void setMinDurationHours(Integer minDurationHours) {
        this.minDurationHours = minDurationHours;
    }

    public Integer getMaxDurationHours() {
        return maxDurationHours;
    }

    public void setMaxDurationHours(Integer maxDurationHours) {
        this.maxDurationHours = maxDurationHours;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
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

    public String getNextAvailableSlot() {
        return nextAvailableSlot;
    }

    public void setNextAvailableSlot(String nextAvailableSlot) {
        this.nextAvailableSlot = nextAvailableSlot;
    }

    public int getOpenSlotsCount() {
        return openSlotsCount;
    }

    public void setOpenSlotsCount(int openSlotsCount) {
        this.openSlotsCount = openSlotsCount;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
}
