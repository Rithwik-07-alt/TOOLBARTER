package com.toolbarter.dto;

public class NeighbourhoodImpactResponse {

    private long itemsListed;
    private long completedExchanges;
    private double distanceSavedKm;
    private double averageTrustScore;

    public NeighbourhoodImpactResponse() {
    }

    public NeighbourhoodImpactResponse(long itemsListed, long completedExchanges, double distanceSavedKm, double averageTrustScore) {
        this.itemsListed = itemsListed;
        this.completedExchanges = completedExchanges;
        this.distanceSavedKm = distanceSavedKm;
        this.averageTrustScore = averageTrustScore;
    }

    public long getItemsListed() {
        return itemsListed;
    }

    public void setItemsListed(long itemsListed) {
        this.itemsListed = itemsListed;
    }

    public long getCompletedExchanges() {
        return completedExchanges;
    }

    public void setCompletedExchanges(long completedExchanges) {
        this.completedExchanges = completedExchanges;
    }

    public double getDistanceSavedKm() {
        return distanceSavedKm;
    }

    public void setDistanceSavedKm(double distanceSavedKm) {
        this.distanceSavedKm = distanceSavedKm;
    }

    public double getAverageTrustScore() {
        return averageTrustScore;
    }

    public void setAverageTrustScore(double averageTrustScore) {
        this.averageTrustScore = averageTrustScore;
    }
}
