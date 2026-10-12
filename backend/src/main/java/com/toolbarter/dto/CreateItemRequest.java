package com.toolbarter.dto;

import com.toolbarter.enums.ItemCategory;
import com.toolbarter.enums.ItemCondition;
import jakarta.validation.constraints.*;

public class CreateItemRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ItemCategory category;

    @NotNull(message = "Condition is required")
    private ItemCondition conditionGrade;

    private String conditionNotes;

    private Boolean oneTimeUse = false;

    @NotNull(message = "Pickup radius is required")
    @DecimalMin(value = "0.1", message = "Pickup radius must be at least 0.1 km")
    @DecimalMax(value = "100.0", message = "Pickup radius must not exceed 100 km")
    private Double pickupRadiusKm = 5.0;

    @Min(value = 1, message = "Minimum duration must be at least 1 hour")
    private Integer minDurationHours = 1;

    @Min(value = 1, message = "Maximum duration must be at least 1 hour")
    private Integer maxDurationHours = 72;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    public CreateItemRequest() {
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

    public ItemCondition getConditionGrade() {
        return conditionGrade;
    }

    public void setConditionGrade(ItemCondition conditionGrade) {
        this.conditionGrade = conditionGrade;
    }

    public void setCondition(ItemCondition condition) {
        this.conditionGrade = condition;
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

    public void setOneTimeUse(Boolean oneTimeUse) {
        this.oneTimeUse = oneTimeUse;
    }

    public void setIsOneTimeUse(Boolean isOneTimeUse) {
        this.oneTimeUse = isOneTimeUse;
    }

    public Double getPickupRadiusKm() {
        return pickupRadiusKm;
    }

    public void setPickupRadiusKm(Double pickupRadiusKm) {
        this.pickupRadiusKm = pickupRadiusKm;
    }

    public void setPickupRadius(Double pickupRadius) {
        this.pickupRadiusKm = pickupRadius;
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
}
