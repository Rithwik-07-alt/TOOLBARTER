package com.toolbarter.controller;

import com.toolbarter.dto.BorrowRequestResponse;
import com.toolbarter.dto.CreateBorrowRequest;
import com.toolbarter.service.BorrowRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RequestController {

    private final BorrowRequestService borrowRequestService;

    public RequestController(BorrowRequestService borrowRequestService) {
        this.borrowRequestService = borrowRequestService;
    }

    @PostMapping("/api/items/{itemId}/requests")
    public ResponseEntity<BorrowRequestResponse> createBorrowRequest(@PathVariable Long itemId,
                                                                     @Valid @RequestBody CreateBorrowRequest request) {
        BorrowRequestResponse created = borrowRequestService.createBorrowRequest(itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/requests/borrower")
    public ResponseEntity<List<BorrowRequestResponse>> getBorrowerRequests() {
        return ResponseEntity.ok(borrowRequestService.getBorrowerRequests());
    }

    @GetMapping("/api/requests/owner")
    public ResponseEntity<List<BorrowRequestResponse>> getOwnerRequests() {
        return ResponseEntity.ok(borrowRequestService.getOwnerRequests());
    }

    @GetMapping("/api/requests/{id}")
    public ResponseEntity<BorrowRequestResponse> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(borrowRequestService.getRequestById(id));
    }

    @PutMapping("/api/requests/{id}/approve")
    public ResponseEntity<BorrowRequestResponse> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(borrowRequestService.approveRequest(id));
    }

    @PutMapping("/api/requests/{id}/decline")
    public ResponseEntity<BorrowRequestResponse> declineRequest(@PathVariable Long id) {
        return ResponseEntity.ok(borrowRequestService.declineRequest(id));
    }

    @PutMapping("/api/requests/{id}/cancel")
    public ResponseEntity<BorrowRequestResponse> cancelRequest(@PathVariable Long id) {
        return ResponseEntity.ok(borrowRequestService.cancelRequest(id));
    }

    @PutMapping("/api/requests/{id}/pickup")
    public ResponseEntity<BorrowRequestResponse> pickupRequest(@PathVariable Long id) {
        return ResponseEntity.ok(borrowRequestService.confirmPickup(id));
    }

    @PutMapping("/api/requests/{id}/return")
    public ResponseEntity<BorrowRequestResponse> returnRequest(@PathVariable Long id) {
        return ResponseEntity.ok(borrowRequestService.confirmReturn(id));
    }
}
