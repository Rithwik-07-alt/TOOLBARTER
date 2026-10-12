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
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.ReviewRepository;
import com.toolbarter.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowRequestServiceTest {

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SchedulingService schedulingService;

    @Mock
    private TrustService trustService;

    @Mock
    private GeoProximityService geoProximityService;

    @Mock
    private AuthService authService;

    private BorrowRequestService borrowRequestService;

    private User owner;
    private User borrower;
    private Item item;
    private AvailabilitySlot slot;

    @BeforeEach
    void setUp() {
        borrowRequestService = new BorrowRequestService(
                borrowRequestRepository, itemRepository, slotRepository,
                reviewRepository, schedulingService, trustService,
                geoProximityService, authService
        );

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        borrower = new User();
        borrower.setId(2L);
        borrower.setEmail("borrower@example.com");

        item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setStatus(ItemStatus.LISTED);

        slot = new AvailabilitySlot();
        slot.setId(20L);
        slot.setItem(item);
        slot.setSlotStart(LocalDateTime.now().plusDays(1));
        slot.setSlotEnd(LocalDateTime.now().plusDays(2));
        slot.setIsBooked(false);
    }

    @Test
    @DisplayName("createBorrowRequest rejects owner attempting to borrow their own tool")
    void createBorrowRequest_OwnerBorrowingOwnTool_ThrowsBadRequest() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(owner);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        CreateBorrowRequest req = new CreateBorrowRequest(20L, LocalDateTime.now().plusDays(1).withHour(10), LocalDateTime.now().plusDays(1).withHour(14));

        assertThrows(BadRequestException.class, () -> borrowRequestService.createBorrowRequest(10L, req));
    }

    @Test
    @DisplayName("approveRequest locks slot and sets status to APPROVED")
    void approveRequest_Success() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(owner);

        BorrowRequest req = new BorrowRequest();
        req.setId(100L);
        req.setItem(item);
        req.setBorrower(borrower);
        req.setSlot(slot);
        req.setStatus(BorrowStatus.PENDING);

        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowRequestResponse res = borrowRequestService.approveRequest(100L);

        assertEquals(BorrowStatus.APPROVED, res.getStatus());
        assertTrue(slot.getIsBooked());
        verify(slotRepository, times(1)).save(slot);
    }

    @Test
    @DisplayName("approveRequest rejects unauthorized user")
    void approveRequest_UnauthorizedUser_ThrowsForbidden() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(borrower);

        BorrowRequest req = new BorrowRequest();
        req.setId(100L);
        req.setItem(item);
        req.setStatus(BorrowStatus.PENDING);

        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(req));

        assertThrows(ForbiddenException.class, () -> borrowRequestService.approveRequest(100L));
    }

    @Test
    @DisplayName("confirmPickup shifts item status to ON_LOAN and request to PICKED_UP")
    void confirmPickup_Success() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(owner);

        BorrowRequest req = new BorrowRequest();
        req.setId(100L);
        req.setItem(item);
        req.setBorrower(borrower);
        req.setStatus(BorrowStatus.APPROVED);

        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowRequestResponse res = borrowRequestService.confirmPickup(100L);

        assertEquals(BorrowStatus.PICKED_UP, res.getStatus());
        assertEquals(ItemStatus.ON_LOAN, item.getStatus());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    @DisplayName("confirmReturn shifts request to RETURNED, item to LISTED, and triggers trust update")
    void confirmReturn_Success() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(owner);

        BorrowRequest req = new BorrowRequest();
        req.setId(100L);
        req.setItem(item);
        req.setBorrower(borrower);
        req.setStatus(BorrowStatus.PICKED_UP);

        when(borrowRequestRepository.findById(100L)).thenReturn(Optional.of(req));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowRequestResponse res = borrowRequestService.confirmReturn(100L);

        assertEquals(BorrowStatus.RETURNED, res.getStatus());
        assertEquals(ItemStatus.LISTED, item.getStatus());
        verify(trustService, times(1)).processReturnCompletion(req);
    }
}
