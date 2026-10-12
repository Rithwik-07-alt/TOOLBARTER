package com.toolbarter.service;

import com.toolbarter.entity.AvailabilitySlot;
import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Item;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class SchedulingService {

    private final SlotRepository slotRepository;
    private final BorrowRequestRepository borrowRequestRepository;

    public SchedulingService(SlotRepository slotRepository, BorrowRequestRepository borrowRequestRepository) {
        this.slotRepository = slotRepository;
        this.borrowRequestRepository = borrowRequestRepository;
    }

    /**
     * Validates that an availability slot starts in the future and end is after start.
     */
    public void validateSlotTiming(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Slot start and end timestamps cannot be null.");
        }
        if (!end.isAfter(start)) {
            throw new BadRequestException("Slot end time must be strictly after slot start time.");
        }
        if (start.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BadRequestException("Slot start time cannot be in the past.");
        }
    }

    /**
     * Validates that requested borrow window fits inside the owner's availability slot and respects item duration constraints.
     */
    public void validateBorrowWindow(Item item, AvailabilitySlot slot, LocalDateTime requestStart, LocalDateTime requestEnd) {
        if (requestStart == null || requestEnd == null) {
            throw new BadRequestException("Requested start and end times cannot be null.");
        }
        if (!requestEnd.isAfter(requestStart)) {
            throw new BadRequestException("Borrow end time must be strictly after borrow start time.");
        }
        if (requestStart.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BadRequestException("Borrow start time cannot be in the past.");
        }

        // Must fit strictly within the slot
        if (requestStart.isBefore(slot.getSlotStart()) || requestEnd.isAfter(slot.getSlotEnd())) {
            throw new BadRequestException(String.format(
                    "Requested borrow window (%s to %s) does not fit inside the available slot window (%s to %s).",
                    requestStart, requestEnd, slot.getSlotStart(), slot.getSlotEnd()
            ));
        }

        // Duration limits check
        long durationHours = Duration.between(requestStart, requestEnd).toHours();
        if (durationHours < item.getMinDurationHours()) {
            throw new BadRequestException(String.format(
                    "Requested borrow duration (%d hours) is below the minimum allowed (%d hours).",
                    durationHours, item.getMinDurationHours()
            ));
        }
        if (durationHours > item.getMaxDurationHours()) {
            throw new BadRequestException(String.format(
                    "Requested borrow duration (%d hours) exceeds the maximum allowed (%d hours).",
                    durationHours, item.getMaxDurationHours()
            ));
        }
    }

    /**
     * Enforces double-booking prevention rule:
     * Checks if the proposed time window overlaps any existing APPROVED or PICKED_UP requests for the item.
     */
    @Transactional(readOnly = true)
    public void ensureNoConflictingBookings(Long itemId, LocalDateTime start, LocalDateTime end, Long excludeRequestId) {
        Set<BorrowStatus> activeStatuses = Set.of(BorrowStatus.APPROVED, BorrowStatus.PICKED_UP);
        List<BorrowRequest> conflicts = borrowRequestRepository.findConflictingRequests(itemId, start, end, activeStatuses);

        boolean hasConflict = conflicts.stream()
                .anyMatch(r -> excludeRequestId == null || !r.getId().equals(excludeRequestId));

        if (hasConflict) {
            throw new BadRequestException("Scheduling conflict: The item already has an approved or ongoing borrow during the requested time window.");
        }
    }
}
