package com.toolbarter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toolbarter.enums.ItemCategory;
import com.toolbarter.enums.ItemCondition;
import com.toolbarter.enums.ItemStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private User owner;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_grade", nullable = false, length = 50)
    private ItemCondition conditionGrade;

    @Column(name = "condition_notes", columnDefinition = "TEXT")
    private String conditionNotes;

    @Column(name = "one_time_use", nullable = false)
    private Boolean oneTimeUse = false;

    @Column(name = "pickup_radius_km", nullable = false)
    private Double pickupRadiusKm = 5.0;

    @Column(name = "min_duration_hours", nullable = false)
    private Integer minDurationHours = 1;

    @Column(name = "max_duration_hours", nullable = false)
    private Integer maxDurationHours = 72;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemStatus status = ItemStatus.LISTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AvailabilitySlot> slots = new ArrayList<>();

    public Item() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ItemStatus.LISTED;
        }
        if (oneTimeUse == null) {
            oneTimeUse = false;
        }
        if (pickupRadiusKm == null) {
            pickupRadiusKm = 5.0;
        }
        if (minDurationHours == null) {
            minDurationHours = 1;
        }
        if (maxDurationHours == null) {
            maxDurationHours = 72;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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

    public Double getPickupRadiusKm() {
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

    public List<AvailabilitySlot> getSlots() {
        return slots;
    }

    public void setSlots(List<AvailabilitySlot> slots) {
        this.slots = slots;
    }
}
