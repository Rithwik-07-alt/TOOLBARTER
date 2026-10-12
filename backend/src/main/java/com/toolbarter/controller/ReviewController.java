package com.toolbarter.controller;

import com.toolbarter.dto.CreateReviewRequest;
import com.toolbarter.dto.ReviewResponse;
import com.toolbarter.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<ReviewResponse> submitReview(@PathVariable Long id,
                                                       @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse created = reviewService.submitReview(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
