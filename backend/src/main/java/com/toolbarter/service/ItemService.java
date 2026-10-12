package com.toolbarter.service;

import com.toolbarter.dto.CreateItemRequest;
import com.toolbarter.dto.ItemResponse;
import com.toolbarter.dto.UpdateItemRequest;
import com.toolbarter.entity.AvailabilitySlot;
import com.toolbarter.entity.Item;
import com.toolbarter.entity.User;
import com.toolbarter.entity.UserFavorite;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.enums.ItemCategory;
import com.toolbarter.enums.ItemStatus;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.exception.ForbiddenException;
import com.toolbarter.exception.ResourceNotFoundException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.SlotRepository;
import com.toolbarter.repository.UserFavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final SlotRepository slotRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final GeoProximityService geoProximityService;
    private final AuthService authService;

    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, h:mm a");

    public ItemService(ItemRepository itemRepository,
                       SlotRepository slotRepository,
                       BorrowRequestRepository borrowRequestRepository,
                       UserFavoriteRepository userFavoriteRepository,
                       GeoProximityService geoProximityService,
                       AuthService authService) {
        this.itemRepository = itemRepository;
        this.slotRepository = slotRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.userFavoriteRepository = userFavoriteRepository;
        this.geoProximityService = geoProximityService;
        this.authService = authService;
    }

    @Transactional
    public ItemResponse createItem(CreateItemRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();

        Item item = new Item();
        item.setOwner(currentUser);
        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription().trim());
        item.setCategory(request.getCategory());
        item.setConditionGrade(request.getConditionGrade());
        item.setConditionNotes(request.getConditionNotes());
        item.setOneTimeUse(Boolean.TRUE.equals(request.getOneTimeUse()));
        item.setPickupRadiusKm(request.getPickupRadiusKm() != null ? request.getPickupRadiusKm() : 5.0);
        item.setMinDurationHours(request.getMinDurationHours() != null ? request.getMinDurationHours() : 1);
        item.setMaxDurationHours(request.getMaxDurationHours() != null ? request.getMaxDurationHours() : 72);
        item.setPhotoUrl(request.getPhotoUrl());
        item.setLatitude(request.getLatitude());
        item.setLongitude(request.getLongitude());
        item.setStatus(ItemStatus.LISTED);
        item.setCreatedAt(LocalDateTime.now());

        Item saved = itemRepository.save(item);
        return populateItemResponse(saved, currentUser.getLatitude(), currentUser.getLongitude(), currentUser);
    }

    @Transactional
    public ItemResponse updateItem(Long id, UpdateItemRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        if (!item.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the owner can modify this tool listing.");
        }

        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription().trim());
        item.setCategory(request.getCategory());
        item.setConditionGrade(request.getConditionGrade());
        item.setConditionNotes(request.getConditionNotes());
        if (request.getOneTimeUse() != null) {
            item.setOneTimeUse(request.getOneTimeUse());
        }
        if (request.getPickupRadiusKm() != null) {
            item.setPickupRadiusKm(request.getPickupRadiusKm());
        }
        if (request.getMinDurationHours() != null) {
            item.setMinDurationHours(request.getMinDurationHours());
        }
        if (request.getMaxDurationHours() != null) {
            item.setMaxDurationHours(request.getMaxDurationHours());
        }
        item.setPhotoUrl(request.getPhotoUrl());
        item.setLatitude(request.getLatitude());
        item.setLongitude(request.getLongitude());

        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }

        Item saved = itemRepository.save(item);
        return populateItemResponse(saved, currentUser.getLatitude(), currentUser.getLongitude(), currentUser);
    }

    @Transactional
    public void deleteItem(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        if (!item.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the owner can delete or unlist this item.");
        }

        // Check if there are active loans
        Set<BorrowStatus> activeStatuses = Set.of(BorrowStatus.APPROVED, BorrowStatus.PICKED_UP);
        boolean hasActiveLoans = borrowRequestRepository.findByItemIdOrderByCreatedAtDesc(id).stream()
                .anyMatch(r -> activeStatuses.contains(r.getStatus()));

        if (hasActiveLoans) {
            throw new BadRequestException("Cannot delete or unlist this item while it has active approved or picked-up requests.");
        }

        // Soft delete / unlist
        item.setStatus(ItemStatus.UNAVAILABLE);
        itemRepository.save(item);
    }

    public ItemResponse getItemById(Long id, Double userLat, Double userLng) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        User currentUser = null;
        try {
            currentUser = authService.getCurrentAuthenticatedUser();
            if (userLat == null && currentUser != null) {
                userLat = currentUser.getLatitude();
                userLng = currentUser.getLongitude();
            }
        } catch (Exception ignored) {
        }

        return populateItemResponse(item, userLat, userLng, currentUser);
    }

    public List<ItemResponse> searchItems(ItemCategory category,
                                         ItemStatus status,
                                         String keyword,
                                         Double userLat,
                                         Double userLng,
                                         Double radiusKm) {
        User currentUser = null;
        try {
            currentUser = authService.getCurrentAuthenticatedUser();
            if (userLat == null && currentUser != null) {
                userLat = currentUser.getLatitude();
                userLng = currentUser.getLongitude();
            }
        } catch (Exception ignored) {
        }

        List<Item> items = itemRepository.searchItems(category, status, keyword);

        final Double fLat = userLat;
        final Double fLng = userLng;
        final User fUser = currentUser;

        return items.stream()
                .filter(item -> item.getStatus() != ItemStatus.UNAVAILABLE)
                .map(item -> populateItemResponse(item, fLat, fLng, fUser))
                .filter(dto -> {
                    if (radiusKm != null && dto.getDistanceKm() != null) {
                        return dto.getDistanceKm() <= radiusKm;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(ItemResponse::getDistanceKm, Comparator.nullsLast(Double::compareTo)))
                .collect(Collectors.toList());
    }

    public List<ItemResponse> getNearbyItems(double lat, double lng, Double radiusKm) {
        double radius = (radiusKm != null && radiusKm > 0) ? radiusKm : 10.0;
        User currentUser = null;
        try {
            currentUser = authService.getCurrentAuthenticatedUser();
        } catch (Exception ignored) {
        }

        final User fUser = currentUser;

        return itemRepository.findAll().stream()
                .filter(item -> item.getStatus() != ItemStatus.UNAVAILABLE)
                .map(item -> populateItemResponse(item, lat, lng, fUser))
                .filter(dto -> dto.getDistanceKm() != null && dto.getDistanceKm() <= radius)
                .sorted(Comparator.comparingDouble(ItemResponse::getDistanceKm))
                .collect(Collectors.toList());
    }

    public List<ItemResponse> getMyItems() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return itemRepository.findByOwnerId(currentUser.getId()).stream()
                .map(item -> populateItemResponse(item, currentUser.getLatitude(), currentUser.getLongitude(), currentUser))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean toggleFavorite(Long itemId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        boolean exists = userFavoriteRepository.existsByUserIdAndItemId(currentUser.getId(), item.getId());
        if (exists) {
            userFavoriteRepository.deleteByUserIdAndItemId(currentUser.getId(), item.getId());
            return false;
        } else {
            userFavoriteRepository.save(new UserFavorite(currentUser.getId(), item.getId()));
            return true;
        }
    }

    public List<ItemResponse> getFavoriteItems() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<UserFavorite> favorites = userFavoriteRepository.findByUserId(currentUser.getId());
        List<Long> itemIds = favorites.stream().map(UserFavorite::getItemId).collect(Collectors.toList());

        return itemRepository.findAllById(itemIds).stream()
                .filter(item -> item.getStatus() != ItemStatus.UNAVAILABLE)
                .map(item -> populateItemResponse(item, currentUser.getLatitude(), currentUser.getLongitude(), currentUser))
                .collect(Collectors.toList());
    }

    private ItemResponse populateItemResponse(Item item, Double viewerLat, Double viewerLng, User viewer) {
        ItemResponse dto = ItemResponse.fromEntity(item);

        if (viewerLat != null && viewerLng != null &&
                item.getLatitude() != null && item.getLongitude() != null) {
            double distance = geoProximityService.calculateDistance(
                    viewerLat, viewerLng, item.getLatitude(), item.getLongitude()
            );
            dto.setDistanceKm(distance);
        }

        List<AvailabilitySlot> openSlots = slotRepository.findOpenSlotsByItemId(item.getId(), LocalDateTime.now());
        dto.setOpenSlotsCount(openSlots.size());
        if (!openSlots.isEmpty()) {
            AvailabilitySlot next = openSlots.get(0);
            dto.setNextAvailableSlot(String.format("%s – %s",
                    next.getSlotStart().format(SLOT_FORMATTER),
                    next.getSlotEnd().format(SLOT_FORMATTER)));
        } else {
            dto.setNextAvailableSlot("No open slots");
        }

        if (viewer != null) {
            dto.setIsFavorite(userFavoriteRepository.existsByUserIdAndItemId(viewer.getId(), item.getId()));
        }

        return dto;
    }
}
