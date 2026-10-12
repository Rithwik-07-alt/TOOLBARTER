package com.toolbarter.service;

import org.springframework.stereotype.Service;

@Service
public class GeoProximityService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculates the great-circle distance between two geographic coordinates using the Haversine formula.
     *
     * @param lat1 Latitude of point 1 in degrees
     * @param lon1 Longitude of point 1 in degrees
     * @param lat2 Latitude of point 2 in degrees
     * @param lon2 Longitude of point 2 in degrees
     * @return Distance in kilometers, rounded to 2 decimal places
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (!isValidCoordinate(lat1, lon1) || !isValidCoordinate(lat2, lon2)) {
            throw new IllegalArgumentException("Invalid latitude or longitude coordinate ranges.");
        }

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        // Round to 2 decimal places
        return Math.round(distance * 100.0) / 100.0;
    }

    /**
     * Checks if coordinates fall within valid geographic bounds.
     */
    public boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0;
    }

    /**
     * Checks whether a target point falls within a given radius in kilometers from the source point.
     */
    public boolean isWithinRadius(double sourceLat, double sourceLon, double targetLat, double targetLon, double radiusKm) {
        double dist = calculateDistance(sourceLat, sourceLon, targetLat, targetLon);
        return dist <= radiusKm;
    }
}
