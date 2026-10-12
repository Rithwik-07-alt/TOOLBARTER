package com.toolbarter.dto;

import com.toolbarter.entity.AvailabilitySlot;
import java.time.LocalDateTime;

public class SlotResponse {

    private Long id;
    private Long itemId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private Boolean isBooked;

    public SlotResponse() {
    }

    public static SlotResponse fromEntity(AvailabilitySlot slot) {
        SlotResponse dto = new SlotResponse();
        dto.setId(slot.getId());
        if (slot.getItem() != null) {
            dto.setItemId(slot.getItem().getId());
        }
        dto.setSlotStart(slot.getSlotStart());
        dto.setSlotEnd(slot.getSlotEnd());
        dto.setIsBooked(slot.getIsBooked());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public LocalDateTime getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(LocalDateTime slotStart) {
        this.slotStart = slotStart;
    }

    public LocalDateTime getSlotEnd() {
        return slotEnd;
    }

    public LocalDateTime getStartTime() {
        return slotStart;
    }

    public LocalDateTime getEndTime() {
        return slotEnd;
    }

    public void setSlotEnd(LocalDateTime slotEnd) {
        this.slotEnd = slotEnd;
    }

    public Boolean getIsBooked() {
        return isBooked;
    }

    public void setIsBooked(Boolean isBooked) {
        this.isBooked = isBooked;
    }
}
