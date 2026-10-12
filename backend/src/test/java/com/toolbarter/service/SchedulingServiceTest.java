package com.toolbarter.service;

import com.toolbarter.entity.AvailabilitySlot;
import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Item;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    private SchedulingService schedulingService;

    @BeforeEach
    void setUp() {
        schedulingService = new SchedulingService(slotRepository, borrowRequestRepository);
    }

    @Test
    @DisplayName("validateSlotTiming rejects end before start or past start")
    void validateSlotTiming_Invalid_ThrowsBadRequest() {
        LocalDateTime now = LocalDateTime.now();
        // End before start
        assertThrows(BadRequestException.class, () ->
                schedulingService.validateSlotTiming(now.plusDays(2), now.plusDays(1)));

        // Past start
        assertThrows(BadRequestException.class, () ->
                schedulingService.validateSlotTiming(now.minusDays(1), now.plusDays(1)));
    }

    @Test
    @DisplayName("validateBorrowWindow rejects window outside slot")
    void validateBorrowWindow_OutsideSlot_ThrowsBadRequest() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item();
        item.setMinDurationHours(1);
        item.setMaxDurationHours(24);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setSlotStart(now.plusDays(1).withHour(9));
        slot.setSlotEnd(now.plusDays(1).withHour(17));

        // Request starts before slot
        assertThrows(BadRequestException.class, () ->
                schedulingService.validateBorrowWindow(item, slot, now.plusDays(1).withHour(8), now.plusDays(1).withHour(12)));

        // Request ends after slot
        assertThrows(BadRequestException.class, () ->
                schedulingService.validateBorrowWindow(item, slot, now.plusDays(1).withHour(10), now.plusDays(1).withHour(18)));
    }

    @Test
    @DisplayName("validateBorrowWindow rejects duration violating item min/max limits")
    void validateBorrowWindow_DurationLimits_ThrowsBadRequest() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item();
        item.setMinDurationHours(3);
        item.setMaxDurationHours(8);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setSlotStart(now.plusDays(1).withHour(8));
        slot.setSlotEnd(now.plusDays(1).withHour(20));

        // Duration is 1 hour (less than min 3)
        assertThrows(BadRequestException.class, () ->
                schedulingService.validateBorrowWindow(item, slot, now.plusDays(1).withHour(9), now.plusDays(1).withHour(10)));

        // Duration is 10 hours (more than max 8)
        assertThrows(BadRequestException.class, () ->
                schedulingService.validateBorrowWindow(item, slot, now.plusDays(1).withHour(9), now.plusDays(1).withHour(19)));
    }

    @Test
    @DisplayName("ensureNoConflictingBookings throws exception when active booking overlaps")
    void ensureNoConflictingBookings_ConflictExists_ThrowsBadRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10);
        LocalDateTime end = LocalDateTime.now().plusDays(1).withHour(14);

        BorrowRequest conflict = new BorrowRequest();
        conflict.setId(101L);
        conflict.setStatus(BorrowStatus.APPROVED);

        when(borrowRequestRepository.findConflictingRequests(eq(1L), eq(start), eq(end), any()))
                .thenReturn(List.of(conflict));

        assertThrows(BadRequestException.class, () ->
                schedulingService.ensureNoConflictingBookings(1L, start, end, null));
    }

    @Test
    @DisplayName("ensureNoConflictingBookings passes when no active bookings overlap")
    void ensureNoConflictingBookings_NoConflict_Succeeds() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10);
        LocalDateTime end = LocalDateTime.now().plusDays(1).withHour(14);

        when(borrowRequestRepository.findConflictingRequests(eq(1L), eq(start), eq(end), any()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                schedulingService.ensureNoConflictingBookings(1L, start, end, null));
    }
}
