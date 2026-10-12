package com.toolbarter.service;

import com.toolbarter.dto.CreateReviewRequest;
import com.toolbarter.dto.ReviewResponse;
import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Review;
import com.toolbarter.entity.User;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.exception.ForbiddenException;
import com.toolbarter.exception.ResourceNotFoundException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final TrustService trustService;
    private final AuthService authService;

    public ReviewService(ReviewRepository reviewRepository,
                         BorrowRequestRepository borrowRequestRepository,
                         TrustService trustService,
                         AuthService authService) {
        this.reviewRepository = reviewRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.trustService = trustService;
        this.authService = authService;
    }

    @Transactional
    public ReviewResponse submitReview(Long borrowRequestId, CreateReviewRequest request) {
        User reviewer = authService.getCurrentAuthenticatedUser();
        BorrowRequest borrowRequest = borrowRequestRepository.findById(borrowRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found with id: " + borrowRequestId));

        if (borrowRequest.getStatus() != BorrowStatus.RETURNED) {
            throw new BadRequestException("Reviews can only be submitted for completed, RETURNED borrow exchanges.");
        }

        boolean isBorrower = borrowRequest.getBorrower().getId().equals(reviewer.getId());
        boolean isOwner = borrowRequest.getItem().getOwner().getId().equals(reviewer.getId());

        if (!isBorrower && !isOwner) {
            throw new ForbiddenException("You are not a participant in this borrow exchange.");
        }

        if (reviewRepository.existsByBorrowRequestIdAndReviewerId(borrowRequestId, reviewer.getId())) {
            throw new BadRequestException("You have already submitted a review for this borrow exchange.");
        }

        User reviewee = isBorrower ? borrowRequest.getItem().getOwner() : borrowRequest.getBorrower();

        Review review = new Review();
        review.setBorrowRequest(borrowRequest);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(request.getRating());
        review.setComment(request.getComment() != null ? request.getComment().trim() : "");
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);

        // Apply rating points to reviewee trust score and level
        trustService.applyReviewRating(reviewee, request.getRating());

        return ReviewResponse.fromEntity(saved);
    }

    public List<ReviewResponse> getReviewsForUser(Long userId) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsForRequest(Long borrowRequestId) {
        return reviewRepository.findByBorrowRequestId(borrowRequestId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
