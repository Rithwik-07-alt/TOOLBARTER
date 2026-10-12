package com.toolbarter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateBorrowRequest {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Start timestamp is required")
    @Future(message = "Start timestamp must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTs;

    @NotNull(message = "End timestamp is required")
    @Future(message = "End timestamp must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTs;

    public CreateBorrowRequest() {
    }

    public CreateBorrowRequest(Long slotId, LocalDateTime startTs, LocalDateTime endTs) {
        this.slotId = slotId;
        this.startTs = startTs;
        this.endTs = endTs;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getStartTs() {
        return startTs;
    }

    public void setStartTs(LocalDateTime startTs) {
        this.startTs = startTs;
    }

    public LocalDateTime getEndTs() {
        return endTs;
    }

    public void setEndTs(LocalDateTime endTs) {
        this.endTs = endTs;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTs = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTs = endTime;
    }
}
