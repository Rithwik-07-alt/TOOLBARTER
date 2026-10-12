package com.toolbarter.dto;

import com.toolbarter.entity.User;
import com.toolbarter.enums.Role;
import com.toolbarter.enums.TrustLevel;
import java.time.LocalDateTime;
import java.util.Set;

public class UserSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double latitude;
    private Double longitude;
    private Set<Role> roles;
    private Integer trustScore;
    private TrustLevel trustLevel;
    private String trustLevelDisplay;
    private LocalDateTime createdAt;

    public UserSummaryResponse() {
    }

    public static UserSummaryResponse fromEntity(User user) {
        UserSummaryResponse dto = new UserSummaryResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setLatitude(user.getLatitude());
        dto.setLongitude(user.getLongitude());
        dto.setRoles(user.getRoles());
        dto.setTrustScore(user.getTrustScore());
        dto.setTrustLevel(user.getTrustLevel());
        dto.setTrustLevelDisplay(user.getTrustLevel() != null ? user.getTrustLevel().getDisplayName() : "New Neighbour");
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhoneNumber() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Integer getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(Integer trustScore) {
        this.trustScore = trustScore;
    }

    public TrustLevel getTrustLevel() {
        return trustLevel;
    }

    public void setTrustLevel(TrustLevel trustLevel) {
        this.trustLevel = trustLevel;
    }

    public String getTrustLevelDisplay() {
        return trustLevelDisplay;
    }

    public void setTrustLevelDisplay(String trustLevelDisplay) {
        this.trustLevelDisplay = trustLevelDisplay;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
