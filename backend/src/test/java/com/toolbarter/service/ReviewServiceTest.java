package com.toolbarter.service;

import com.toolbarter.dto.CreateReviewRequest;
import com.toolbarter.dto.ReviewResponse;
import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Item;
import com.toolbarter.entity.Review;
import com.toolbarter.entity.User;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.exception.ForbiddenException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private TrustService trustService;

    @Mock
    private AuthService authService;

    private ReviewService reviewService;

    private User owner;
    private User borrower;
    private User thirdParty;
    private Item item;
    private BorrowRequest completedRequest;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, borrowRequestRepository, trustService, authService);

        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");

        borrower = new User();
        borrower.setId(2L);
        borrower.setName("Borrower");

        thirdParty = new User();
        thirdParty.setId(3L);
        thirdParty.setName("Third Party");

        item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        completedRequest = new BorrowRequest();
        completedRequest.setId(100L);
        completedRequest.setItem(item);
        completedRequest.setBorrower(borrower);
        completedRequest.setStatus(BorrowStatus.RETURNED);
    }

    @Test
    @DisplayName("submitReview rejects non-RETURNED requests")
    void submitReview_NotReturned_ThrowsBadRequest() {
        completedRequest.setStatus(BorrowStatus.PICKED_UP);
        when(authService.getCurrentAuthenticatedUser()).thenReturn(borrower);
        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(completedRequest));

        CreateReviewRequest req = new CreateReviewRequest(5, "Great tool!");

        assertThrows(BadRequestException.class, () -> reviewService.submitReview(100L, req));
    }

    @Test
    @DisplayName("submitReview rejects user who is neither borrower nor owner")
    void submitReview_ThirdParty_ThrowsForbidden() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(thirdParty);
        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(completedRequest));

        CreateReviewRequest req = new CreateReviewRequest(5, "Great tool!");

        assertThrows(ForbiddenException.class, () -> reviewService.submitReview(100L, req));
    }

    @Test
    @DisplayName("submitReview rejects duplicate review by same reviewer")
    void submitReview_Duplicate_ThrowsBadRequest() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(borrower);
        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(completedRequest));
        when(reviewRepository.existsByBorrowRequestIdAndReviewerId(100L, borrower.getId())).thenReturn(true);

        CreateReviewRequest req = new CreateReviewRequest(5, "Great tool!");

        assertThrows(BadRequestException.class, () -> reviewService.submitReview(100L, req));
    }

    @Test
    @DisplayName("submitReview saves review and applies rating to reviewee trust")
    void submitReview_Success() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(borrower);
        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(completedRequest));
        when(reviewRepository.existsByBorrowRequestIdAndReviewerId(100L, borrower.getId())).thenReturn(false);

        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(500L);
            return r;
        });

        CreateReviewRequest req = new CreateReviewRequest(5, "Owner was punctual and tool was clean!");
        ReviewResponse res = reviewService.submitReview(100L, req);

        assertNotNull(res);
        assertEquals(5, res.getRating());
        assertEquals("Borrower", res.getReviewerName());
        assertEquals("Owner", res.getRevieweeName());

        verify(trustService, times(1)).applyReviewRating(owner, 5);
    }
}
