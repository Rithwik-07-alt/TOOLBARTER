package com.toolbarter.controller;

import com.toolbarter.dto.DatabaseHealth;
import com.toolbarter.dto.HealthResponse;
import com.toolbarter.service.DatabaseHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DatabaseHealthService databaseHealthService;

    @Autowired
    public HealthController(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        DatabaseHealth dbHealth = databaseHealthService.checkDatabaseConnectivity();

        String overallStatus = "UP".equalsIgnoreCase(dbHealth.getStatus()) ? "UP" : "DEGRADED";
        String message = "UP".equals(overallStatus)
                ? "TOOLBARTER API and database connection are operational."
                : "TOOLBARTER API is running, but database connection requires attention: " + dbHealth.getDiagnostic();

        HealthResponse response = new HealthResponse(
                overallStatus,
                "TOOLBARTER",
                message,
                Instant.now(),
                dbHealth
        );

        return ResponseEntity.ok(response);
    }
}
