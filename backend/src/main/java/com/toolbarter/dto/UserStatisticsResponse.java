package com.toolbarter.dto;

import com.toolbarter.enums.TrustLevel;

public class UserStatisticsResponse {

    private Long userId;
    private long itemsListed;
    private long itemsBorrowed;
    private long activeRequests;
    private long activeLoans;
    private long completedExchanges;
    private int trustScore;
    private TrustLevel trustLevel;
    private String trustLevelDisplay;
    private Double averageRating;
    private long reviewsCount;

    public UserStatisticsResponse() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getItemsListed() {
        return itemsListed;
    }

    public void setItemsListed(long itemsListed) {
        this.itemsListed = itemsListed;
    }

    public long getItemsBorrowed() {
        return itemsBorrowed;
    }

    public void setItemsBorrowed(long itemsBorrowed) {
        this.itemsBorrowed = itemsBorrowed;
    }

    public long getActiveRequests() {
        return activeRequests;
    }

    public void setActiveRequests(long activeRequests) {
        this.activeRequests = activeRequests;
    }

    public long getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(long activeLoans) {
        this.activeLoans = activeLoans;
    }

    public long getCompletedExchanges() {
        return completedExchanges;
    }

    public void setCompletedExchanges(long completedExchanges) {
        this.completedExchanges = completedExchanges;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
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

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public long getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(long reviewsCount) {
        this.reviewsCount = reviewsCount;
    }
}
