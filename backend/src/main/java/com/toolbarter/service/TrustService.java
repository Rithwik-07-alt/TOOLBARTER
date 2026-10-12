package com.toolbarter.service;

import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.User;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.enums.ItemStatus;
import com.toolbarter.enums.TrustLevel;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.ReviewRepository;
import com.toolbarter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TrustService {

    private final UserRepository userRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;

    public TrustService(UserRepository userRepository,
                        BorrowRequestRepository borrowRequestRepository,
                        ReviewRepository reviewRepository,
                        ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.reviewRepository = reviewRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Handles completion of a borrow request:
     * - Grants +5 trust points to both borrower and owner if undisputed.
     * - Deducts -3 points if returned late by more than 2 hours.
     * - Recalculates trust levels for both parties.
     */
    @Transactional
    public void processReturnCompletion(BorrowRequest request) {
        if (Boolean.TRUE.equals(request.getPointsAwarded())) {
            // Idempotency: points cannot be awarded more than once
            return;
        }

        User owner = request.getItem().getOwner();
        User borrower = request.getBorrower();

        // 1. Base completion points (+5 points each)
        int ownerPointsToAdd = 5;
        int borrowerPointsToAdd = 5;

        // 2. Check punctuality: late return (> 2 hours past scheduled endTs)
        LocalDateTime returnTime = request.getReturnConfirmedAt() != null ?
                request.getReturnConfirmedAt() : LocalDateTime.now();

        if (returnTime.isAfter(request.getEndTs().plusHours(2))) {
            // Borrower is penalized 3 points for overdue return
            borrowerPointsToAdd -= 3;
        }

        owner.setTrustScore(Math.max(0, owner.getTrustScore() + ownerPointsToAdd));
        borrower.setTrustScore(Math.max(0, borrower.getTrustScore() + borrowerPointsToAdd));

        request.setPointsAwarded(true);

        recalculateTrustLevel(owner);
        recalculateTrustLevel(borrower);

        userRepository.save(owner);
        userRepository.save(borrower);
        borrowRequestRepository.save(request);
    }

    /**
     * Applies review rating points to the reviewee's trust score and recalculates level.
     * 5 stars: +2
     * 4 stars: +1
     * 3 stars:  0
     * 2 stars: -2
     * 1 star:  -4
     */
    @Transactional
    public void applyReviewRating(User reviewee, int rating) {
        int delta;
        switch (rating) {
            case 5:
                delta = 2;
                break;
            case 4:
                delta = 1;
                break;
            case 3:
                delta = 0;
                break;
            case 2:
                delta = -2;
                break;
            case 1:
                delta = -4;
                break;
            default:
                delta = 0;
        }

        reviewee.setTrustScore(Math.max(0, reviewee.getTrustScore() + delta));
        recalculateTrustLevel(reviewee);
        userRepository.save(reviewee);
    }

    /**
     * Recalculates user's TrustLevel based on total points, completed exchanges count, and ratings.
     */
    public void recalculateTrustLevel(User user) {
        int score = user.getTrustScore() != null ? user.getTrustScore() : 0;
        long completedBorrows = borrowRequestRepository.countByBorrowerIdAndStatus(user.getId(), BorrowStatus.RETURNED);
        long completedLends = borrowRequestRepository.countByItemOwnerIdAndStatus(user.getId(), BorrowStatus.RETURNED);
        long totalCompleted = completedBorrows + completedLends;

        Double avgRating = reviewRepository.getAverageRatingForUser(user.getId());
        double rating = avgRating != null ? avgRating : 5.0;
        long itemsListed = itemRepository.findByOwnerId(user.getId()).size();

        TrustLevel level;

        if (score >= 100 && totalCompleted >= 20 && rating >= 4.6) {
            level = TrustLevel.STREET_CHAMPION;
        } else if (score >= 65 && totalCompleted >= 10 && rating >= 4.2) {
            level = TrustLevel.TRUSTED_NEIGHBOUR;
        } else if (score >= 35 && totalCompleted >= 5 && itemsListed >= 1) {
            level = TrustLevel.RELIABLE_LENDER;
        } else if (score >= 15 && totalCompleted >= 2) {
            level = TrustLevel.REGULAR_BORROWER;
        } else {
            level = TrustLevel.NEW_NEIGHBOUR;
        }

        user.setTrustLevel(level);
    }
}
