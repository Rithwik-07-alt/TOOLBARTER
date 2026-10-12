package com.toolbarter.controller;

import com.toolbarter.dto.CreateItemRequest;
import com.toolbarter.dto.ItemResponse;
import com.toolbarter.dto.UpdateItemRequest;
import com.toolbarter.enums.ItemCategory;
import com.toolbarter.enums.ItemStatus;
import com.toolbarter.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> searchItems(
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radius) {
        return ResponseEntity.ok(itemService.searchItems(category, status, keyword, lat, lng, radius));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ItemResponse>> getNearbyItems(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "10.0") Double radius) {
        return ResponseEntity.ok(itemService.getNearbyItems(lat, lng, radius));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(
            @PathVariable Long id,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        return ResponseEntity.ok(itemService.getItemById(id, lat, lng));
    }

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody CreateItemRequest request) {
        ItemResponse created = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-items")
    public ResponseEntity<List<ItemResponse>> getMyItems() {
        return ResponseEntity.ok(itemService.getMyItems());
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(@PathVariable Long id) {
        boolean isFav = itemService.toggleFavorite(id);
        return ResponseEntity.ok(Map.of("favorite", isFav));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<ItemResponse>> getFavoriteItems() {
        return ResponseEntity.ok(itemService.getFavoriteItems());
    }
}
