package com.toolbarter.service;

import com.toolbarter.service.GeoProximityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoProximityServiceTest {

    private GeoProximityService geoService;

    @BeforeEach
    void setUp() {
        geoService = new GeoProximityService();
    }

    @Test
    @DisplayName("Haversine distance between identical points should be 0.0 km")
    void calculateDistance_IdenticalPoints_ReturnsZero() {
        double dist = geoService.calculateDistance(17.4401, 78.3826, 17.4401, 78.3826);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    @DisplayName("Haversine distance between known coordinates should match great-circle distance")
    void calculateDistance_KnownCoordinates_ReturnsExpectedValue() {
        // Hitec City (17.4401, 78.3826) to Gachibowli (17.4401, 78.3489) ~ 3.57 km
        double dist = geoService.calculateDistance(17.4401, 78.3826, 17.4401, 78.3489);
        assertTrue(dist > 3.0 && dist < 4.0, "Expected approx 3.57 km, got: " + dist);
    }

    @Test
    @DisplayName("Invalid coordinate ranges should throw IllegalArgumentException")
    void calculateDistance_InvalidCoordinates_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                geoService.calculateDistance(95.0, 78.0, 17.0, 78.0));

        assertThrows(IllegalArgumentException.class, () ->
                geoService.calculateDistance(17.0, -190.0, 17.0, 78.0));
    }

    @Test
    @DisplayName("isWithinRadius correctly checks proximity")
    void isWithinRadius_CorrectlyFilters() {
        // ~0.4 km apart
        assertTrue(geoService.isWithinRadius(17.4401, 78.3826, 17.4436, 78.3854, 1.0));
        // Outside 0.2 km
        assertFalse(geoService.isWithinRadius(17.4401, 78.3826, 17.4436, 78.3854, 0.2));
    }
}
