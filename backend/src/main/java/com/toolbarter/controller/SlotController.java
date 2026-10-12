package com.toolbarter.controller;

import com.toolbarter.dto.CreateSlotRequest;
import com.toolbarter.dto.SlotResponse;
import com.toolbarter.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/api/items/{itemId}/slots")
    public ResponseEntity<SlotResponse> createSlot(@PathVariable Long itemId,
                                                   @Valid @RequestBody CreateSlotRequest request) {
        SlotResponse created = slotService.createSlot(itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/items/{itemId}/slots")
    public ResponseEntity<List<SlotResponse>> getSlotsByItem(@PathVariable Long itemId,
                                                             @RequestParam(required = false, defaultValue = "false") boolean openOnly) {
        if (openOnly) {
            return ResponseEntity.ok(slotService.getOpenSlotsByItem(itemId));
        }
        return ResponseEntity.ok(slotService.getSlotsByItem(itemId));
    }

    @DeleteMapping("/api/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        slotService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}
