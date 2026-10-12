package com.toolbarter.service;

import com.toolbarter.dto.BorrowRequestResponse;
import com.toolbarter.dto.CreateBorrowRequest;
import com.toolbarter.entity.AvailabilitySlot;
import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Item;
import com.toolbarter.entity.User;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.enums.ItemStatus;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.exception.ForbiddenException;
import com.toolbarter.exception.ResourceNotFoundException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.ReviewRepository;
import com.toolbarter.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final ItemRepository itemRepository;
    private final SlotRepository slotRepository;
    private final ReviewRepository reviewRepository;
    private final SchedulingService schedulingService;
    private final TrustService trustService;
    private final GeoProximityService geoProximityService;
    private final AuthService authService;

    public BorrowRequestService(BorrowRequestRepository borrowRequestRepository,
                                ItemRepository itemRepository,
                                SlotRepository slotRepository,
                                ReviewRepository reviewRepository,
                                SchedulingService schedulingService,
                                TrustService trustService,
                                GeoProximityService geoProximityService,
                                AuthService authService) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.itemRepository = itemRepository;
        this.slotRepository = slotRepository;
        this.reviewRepository = reviewRepository;
        this.schedulingService = schedulingService;
        this.trustService = trustService;
        this.geoProximityService = geoProximityService;
        this.authService = authService;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BorrowRequestResponse createBorrowRequest(Long itemId, CreateBorrowRequest request) {
        User borrower = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (item.getStatus() == ItemStatus.UNAVAILABLE) {
            throw new BadRequestException("This tool is currently marked as unavailable.");
        }

        if (item.getOwner().getId().equals(borrower.getId())) {
            throw new BadRequestException("You cannot borrow your own tool listing.");
        }

        AvailabilitySlot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + request.getSlotId()));

        if (!slot.getItem().getId().equals(item.getId())) {
            throw new BadRequestException("The specified availability slot does not belong to this tool.");
        }

        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new BadRequestException("The selected availability slot has already been booked.");
        }

        // Validate window and duration
        schedulingService.validateBorrowWindow(item, slot, request.getStartTs(), request.getEndTs());

        // Check for double-booking against existing approved or active borrows
        schedulingService.ensureNoConflictingBookings(item.getId(), request.getStartTs(), request.getEndTs(), null);

        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setItem(item);
        borrowRequest.setBorrower(borrower);
        borrowRequest.setSlot(slot);
        borrowRequest.setStartTs(request.getStartTs());
        borrowRequest.setEndTs(request.getEndTs());
        borrowRequest.setStatus(BorrowStatus.PENDING);
        borrowRequest.setPointsAwarded(false);
        borrowRequest.setCreatedAt(LocalDateTime.now());

        BorrowRequest saved = borrowRequestRepository.save(borrowRequest);
        return mapToResponse(saved, borrower);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BorrowRequestResponse approveRequest(Long id) {
        User owner = authService.getCurrentAuthenticatedUser();
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found with id: " + id));

        if (!request.getItem().getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("Only the tool owner can approve this borrow request.");
        }

        if (request.getStatus() != BorrowStatus.PENDING) {
            throw new BadRequestException("Only pending borrow requests can be approved. Current status: " + request.getStatus());
        }

        // Re-verify no conflicts have been created concurrently
        schedulingService.ensureNoConflictingBookings(request.getItem().getId(), request.getStartTs(), request.getEndTs(), request.getId());

        request.setStatus(BorrowStatus.APPROVED);
        if (request.getSlot() != null) {
            request.getSlot().setIsBooked(true);
            slotRepository.save(request.getSlot());
        }

        BorrowRequest saved = borrowRequestRepository.save(request);
        return mapToResponse(saved, owner);
    }

    @Transactional
    public BorrowRequestResponse declineRequest(Long id) {
        User owner = authService.getCurrentAuthenticatedUser();
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found with id: " + id));

        if (!request.getItem().getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("Only the tool owner can decline this borrow request.");
        }

        if (request.getStatus() != BorrowStatus.PENDING) {
            throw new BadRequestException("Only pending borrow requests can be declined. Current status: " + request.getStatus());
        }

        request.setStatus(BorrowStatus.DECLINED);
        BorrowRequest saved = borrowRequestRepository.save(request);
        return mapToResponse(saved, owner);
    }

    @Transactional
    public BorrowRequestResponse cancelRequest(Long id) {
        User borrower = authService.getCurrentAuthenticatedUser();
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found with id: " + id));

        if (!request.getBorrower().getId().equals(borrower.getId())) {
            throw new ForbiddenException("You can only cancel your own borrow requests.");
        }

        if (request.getStatus() != BorrowStatus.PENDING && request.getStatus() != BorrowStatus.APPROVED) {
            throw new BadRequestException("Cannot cancel request with status: " + request.getStatus());
        }

        if (request.getStatus() == BorrowStatus.APPROVED && request.getSlot() != null) {
            request.getSlot().setIsBooked(false);
            slotRepository.save(request.getSlot());
        }

        request.setStatus(BorrowStatus.CANCELLED);
        BorrowRequest saved = borrowRequestRepository.save(request);
        return mapToResponse(saved, borrower);
    }

    @Transactional
    public BorrowRequestResponse confirmPickup(Long id) {
        User owner = authService.getCurrentAuthenticatedUser();
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found with id: " + id));

        if (!request.getItem().getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("Only the tool owner can confirm handover pickup.");
        }

        if (request.getStatus() != BorrowStatus.APPROVED) {
            throw new BadRequestException("Handover pickup can only be confirmed for approved requests. Current status: " + request.getStatus());
        }

        request.setStatus(BorrowStatus.PICKED_UP);
        request.setPickupConfirmedAt(LocalDateTime.now());

        Item item = request.getItem();
        item.setStatus(ItemStatus.ON_LOAN);
        itemRepository.save(item);

        BorrowRequest saved = borrowRequestRepository.save(request);
        return mapToResponse(saved, owner);
    }

    @Transactional
    public BorrowRequestResponse confirmReturn(Long id) {
        User owner = authService.getCurrentAuthenticatedUser();
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found with id: " + id));

        if (!request.getItem().getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("Only the tool owner can confirm tool return.");
        }

        if (request.getStatus() != BorrowStatus.PICKED_UP) {
            throw new BadRequestException("Return can only be confirmed for picked-up tools. Current status: " + request.getStatus());
        }

        request.setStatus(BorrowStatus.RETURNED);
        request.setReturnConfirmedAt(LocalDateTime.now());

        Item item = request.getItem();
        item.setStatus(ItemStatus.LISTED);
        itemRepository.save(item);

        // Process trust score and level update (+5 points, late penalty check)
        trustService.processReturnCompletion(request);

        BorrowRequest saved = borrowRequestRepository.save(request);
        return mapToResponse(saved, owner);
    }

    public List<BorrowRequestResponse> getBorrowerRequests() {
        User borrower = authService.getCurrentAuthenticatedUser();
        return borrowRequestRepository.findByBorrowerIdOrderByCreatedAtDesc(borrower.getId()).stream()
                .map(r -> mapToResponse(r, borrower))
                .collect(Collectors.toList());
    }

    public List<BorrowRequestResponse> getOwnerRequests() {
        User owner = authService.getCurrentAuthenticatedUser();
        return borrowRequestRepository.findByItemOwnerIdOrderByCreatedAtDesc(owner.getId()).stream()
                .map(r -> mapToResponse(r, owner))
                .collect(Collectors.toList());
    }

    public BorrowRequestResponse getRequestById(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found with id: " + id));

        boolean isBorrower = request.getBorrower().getId().equals(currentUser.getId());
        boolean isOwner = request.getItem().getOwner().getId().equals(currentUser.getId());

        if (!isBorrower && !isOwner) {
            throw new ForbiddenException("You are not authorized to view this borrow request.");
        }

        return mapToResponse(request, currentUser);
    }

    private BorrowRequestResponse mapToResponse(BorrowRequest request, User currentUser) {
        BorrowRequestResponse dto = BorrowRequestResponse.fromEntity(request);

        if (currentUser != null && request.getItem() != null &&
                currentUser.getLatitude() != null && currentUser.getLongitude() != null &&
                request.getItem().getLatitude() != null && request.getItem().getLongitude() != null) {
            double distance = geoProximityService.calculateDistance(
                    currentUser.getLatitude(), currentUser.getLongitude(),
                    request.getItem().getLatitude(), request.getItem().getLongitude()
            );
            dto.setDistanceKm(distance);
        }

        if (currentUser != null) {
            boolean hasReviewed = reviewRepository.existsByBorrowRequestIdAndReviewerId(request.getId(), currentUser.getId());
            dto.setHasReviewed(hasReviewed);
            dto.setCanReview(request.getStatus() == BorrowStatus.RETURNED && !hasReviewed);
        }

        return dto;
    }
}
