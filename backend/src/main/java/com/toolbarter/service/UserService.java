package com.toolbarter.service;

import com.toolbarter.dto.UserStatisticsResponse;
import com.toolbarter.dto.UserSummaryResponse;
import com.toolbarter.dto.UserUpdateRequest;
import com.toolbarter.entity.User;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.enums.ItemStatus;
import com.toolbarter.exception.ForbiddenException;
import com.toolbarter.exception.ResourceNotFoundException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.ReviewRepository;
import com.toolbarter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final ReviewRepository reviewRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository,
                       ItemRepository itemRepository,
                       BorrowRequestRepository borrowRequestRepository,
                       ReviewRepository reviewRepository,
                       AuthService authService) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.reviewRepository = reviewRepository;
        this.authService = authService;
    }

    public UserSummaryResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserSummaryResponse.fromEntity(user);
    }

    @Transactional
    public UserSummaryResponse updateUser(Long id, UserUpdateRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (!currentUser.getId().equals(id)) {
            throw new ForbiddenException("You cannot edit another user's profile.");
        }

        currentUser.setName(request.getName().trim());
        currentUser.setPhone(request.getPhone().trim());
        currentUser.setLatitude(request.getLatitude());
        currentUser.setLongitude(request.getLongitude());

        User saved = userRepository.save(currentUser);
        return UserSummaryResponse.fromEntity(saved);
    }

    public UserStatisticsResponse getUserStatistics(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserStatisticsResponse stats = new UserStatisticsResponse();
        stats.setUserId(user.getId());
        stats.setItemsListed(itemRepository.findByOwnerId(user.getId()).size());
        stats.setItemsBorrowed(borrowRequestRepository.countByBorrowerIdAndStatus(user.getId(), BorrowStatus.RETURNED));
        stats.setActiveRequests(borrowRequestRepository.countByBorrowerIdAndStatus(user.getId(), BorrowStatus.PENDING)
                + borrowRequestRepository.countByBorrowerIdAndStatus(user.getId(), BorrowStatus.APPROVED)
                + borrowRequestRepository.countByBorrowerIdAndStatus(user.getId(), BorrowStatus.PICKED_UP));
        stats.setActiveLoans(borrowRequestRepository.countByItemOwnerIdAndStatus(user.getId(), BorrowStatus.PICKED_UP));
        stats.setCompletedExchanges(borrowRequestRepository.countByBorrowerIdAndStatus(user.getId(), BorrowStatus.RETURNED)
                + borrowRequestRepository.countByItemOwnerIdAndStatus(user.getId(), BorrowStatus.RETURNED));
        stats.setTrustScore(user.getTrustScore());
        stats.setTrustLevel(user.getTrustLevel());
        stats.setTrustLevelDisplay(user.getTrustLevel() != null ? user.getTrustLevel().getDisplayName() : "New Neighbour");
        stats.setAverageRating(reviewRepository.getAverageRatingForUser(user.getId()));
        stats.setReviewsCount(reviewRepository.countByRevieweeId(user.getId()));

        return stats;
    }
}
