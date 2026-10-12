package com.toolbarter.service;

import com.toolbarter.dto.CreateSlotRequest;
import com.toolbarter.dto.SlotResponse;
import com.toolbarter.entity.AvailabilitySlot;
import com.toolbarter.entity.Item;
import com.toolbarter.entity.User;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.exception.ForbiddenException;
import com.toolbarter.exception.ResourceNotFoundException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final ItemRepository itemRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final SchedulingService schedulingService;
    private final AuthService authService;

    public SlotService(SlotRepository slotRepository,
                       ItemRepository itemRepository,
                       BorrowRequestRepository borrowRequestRepository,
                       SchedulingService schedulingService,
                       AuthService authService) {
        this.slotRepository = slotRepository;
        this.itemRepository = itemRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.schedulingService = schedulingService;
        this.authService = authService;
    }

    @Transactional
    public SlotResponse createSlot(Long itemId, CreateSlotRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the owner can define availability slots for this tool.");
        }

        schedulingService.validateSlotTiming(request.getSlotStart(), request.getSlotEnd());

        AvailabilitySlot slot = new AvailabilitySlot(item, request.getSlotStart(), request.getSlotEnd());
        AvailabilitySlot saved = slotRepository.save(slot);
        return SlotResponse.fromEntity(saved);
    }

    public List<SlotResponse> getSlotsByItem(Long itemId) {
        return slotRepository.findByItemIdOrderBySlotStartAsc(itemId).stream()
                .map(SlotResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SlotResponse> getOpenSlotsByItem(Long itemId) {
        return slotRepository.findOpenSlotsByItemId(itemId, LocalDateTime.now()).stream()
                .map(SlotResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));

        if (!slot.getItem().getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the owner can delete this availability slot.");
        }

        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new BadRequestException("Cannot delete an availability slot that is already booked.");
        }

        slotRepository.delete(slot);
    }
}
