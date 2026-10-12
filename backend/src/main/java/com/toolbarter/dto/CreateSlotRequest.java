package com.toolbarter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateSlotRequest {

    @NotNull(message = "Slot start time is required")
    @Future(message = "Slot start time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime slotStart;

    @NotNull(message = "Slot end time is required")
    @Future(message = "Slot end time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime slotEnd;

    public CreateSlotRequest() {
    }

    public CreateSlotRequest(LocalDateTime slotStart, LocalDateTime slotEnd) {
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
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

    public void setSlotEnd(LocalDateTime slotEnd) {
        this.slotEnd = slotEnd;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.slotStart = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.slotEnd = endTime;
    }
}
