package com.toolbarter.controller;

import com.toolbarter.dto.NeighbourhoodImpactResponse;
import com.toolbarter.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<NeighbourhoodImpactResponse> getNeighbourhoodImpact() {
        return ResponseEntity.ok(statsService.getNeighbourhoodImpact());
    }
}
