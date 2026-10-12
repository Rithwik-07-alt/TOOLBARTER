package com.toolbarter.controller;

import com.toolbarter.dto.ReviewResponse;
import com.toolbarter.dto.UserStatisticsResponse;
import com.toolbarter.dto.UserSummaryResponse;
import com.toolbarter.dto.UserUpdateRequest;
import com.toolbarter.service.ReviewService;
import com.toolbarter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;

    public UserController(UserService userService, ReviewService reviewService) {
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserSummaryResponse> updateUser(@PathVariable Long id,
                                                         @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserStatistics(id));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(id));
    }
}
