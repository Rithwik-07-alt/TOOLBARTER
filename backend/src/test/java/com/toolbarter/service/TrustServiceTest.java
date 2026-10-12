package com.toolbarter.service;

import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Item;
import com.toolbarter.entity.User;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.enums.TrustLevel;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.ReviewRepository;
import com.toolbarter.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrustServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ItemRepository itemRepository;

    private TrustService trustService;

    @BeforeEach
    void setUp() {
        trustService = new TrustService(userRepository, borrowRequestRepository, reviewRepository, itemRepository);
    }

    @Test
    @DisplayName("processReturnCompletion awards +5 points to both owner and borrower when on time")
    void processReturnCompletion_OnTime_AwardsFivePointsEach() {
        User owner = new User();
        owner.setId(1L);
        owner.setTrustScore(10);

        User borrower = new User();
        borrower.setId(2L);
        borrower.setTrustScore(5);

        Item item = new Item();
        item.setOwner(owner);

        LocalDateTime endTs = LocalDateTime.now().plusHours(1);
        BorrowRequest request = new BorrowRequest();
        request.setItem(item);
        request.setBorrower(borrower);
        request.setEndTs(endTs);
        request.setReturnConfirmedAt(LocalDateTime.now());
        request.setPointsAwarded(false);

        trustService.processReturnCompletion(request);

        assertEquals(15, owner.getTrustScore());
        assertEquals(10, borrower.getTrustScore());
        assertTrue(request.getPointsAwarded());

        verify(userRepository, times(1)).save(owner);
        verify(userRepository, times(1)).save(borrower);
        verify(borrowRequestRepository, times(1)).save(request);
    }

    @Test
    @DisplayName("processReturnCompletion penalizes borrower with -3 points if returned late > 2 hours")
    void processReturnCompletion_LateReturn_PenalizesBorrower() {
        User owner = new User();
        owner.setId(1L);
        owner.setTrustScore(10);

        User borrower = new User();
        borrower.setId(2L);
        borrower.setTrustScore(5);

        Item item = new Item();
        item.setOwner(owner);

        // Scheduled to end 3 hours ago, returned now -> >2 hours overdue!
        LocalDateTime endTs = LocalDateTime.now().minusHours(3);
        BorrowRequest request = new BorrowRequest();
        request.setItem(item);
        request.setBorrower(borrower);
        request.setEndTs(endTs);
        request.setReturnConfirmedAt(LocalDateTime.now());
        request.setPointsAwarded(false);

        trustService.processReturnCompletion(request);

        // Owner gets full +5
        assertEquals(15, owner.getTrustScore());
        // Borrower gets (+5 - 3 = +2)
        assertEquals(7, borrower.getTrustScore());
    }

    @Test
    @DisplayName("processReturnCompletion is idempotent and never awards points twice")
    void processReturnCompletion_AlreadyAwarded_DoesNothing() {
        BorrowRequest request = new BorrowRequest();
        request.setPointsAwarded(true);

        trustService.processReturnCompletion(request);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("applyReviewRating modifies trust score correctly according to rating scale")
    void applyReviewRating_AdjustsScoreCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setTrustScore(20);

        // 5-star adds +2
        trustService.applyReviewRating(user, 5);
        assertEquals(22, user.getTrustScore());

        // 1-star deducts -4
        trustService.applyReviewRating(user, 1);
        assertEquals(18, user.getTrustScore());

        // 2-star deducts -2
        trustService.applyReviewRating(user, 2);
        assertEquals(16, user.getTrustScore());
    }

    @Test
    @DisplayName("recalculateTrustLevel elevates user through levels as points and borrows grow")
    void recalculateTrustLevel_TransitionsProperly() {
        User user = new User();
        user.setId(1L);

        // Case 1: 0 points, 0 borrows -> NEW_NEIGHBOUR
        when(borrowRequestRepository.countByBorrowerIdAndStatus(1L, BorrowStatus.RETURNED)).thenReturn(0L);
        when(borrowRequestRepository.countByItemOwnerIdAndStatus(1L, BorrowStatus.RETURNED)).thenReturn(0L);
        when(reviewRepository.getAverageRatingForUser(1L)).thenReturn(5.0);
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of());

        user.setTrustScore(5);
        trustService.recalculateTrustLevel(user);
        assertEquals(TrustLevel.NEW_NEIGHBOUR, user.getTrustLevel());

        // Case 2: >= 15 pts, >= 2 borrows -> REGULAR_BORROWER
        when(borrowRequestRepository.countByBorrowerIdAndStatus(1L, BorrowStatus.RETURNED)).thenReturn(2L);
        user.setTrustScore(20);
        trustService.recalculateTrustLevel(user);
        assertEquals(TrustLevel.REGULAR_BORROWER, user.getTrustLevel());

        // Case 3: >= 100 pts, >= 20 completed, avg rating >= 4.6 -> STREET_CHAMPION
        when(borrowRequestRepository.countByBorrowerIdAndStatus(1L, BorrowStatus.RETURNED)).thenReturn(15L);
        when(borrowRequestRepository.countByItemOwnerIdAndStatus(1L, BorrowStatus.RETURNED)).thenReturn(10L);
        when(reviewRepository.getAverageRatingForUser(1L)).thenReturn(4.9);
        user.setTrustScore(110);
        trustService.recalculateTrustLevel(user);
        assertEquals(TrustLevel.STREET_CHAMPION, user.getTrustLevel());
    }
}
