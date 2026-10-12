package com.toolbarter.service;

import com.toolbarter.dto.CreateItemRequest;
import com.toolbarter.dto.ItemResponse;
import com.toolbarter.dto.UpdateItemRequest;
import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Item;
import com.toolbarter.entity.User;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.enums.ItemCategory;
import com.toolbarter.enums.ItemCondition;
import com.toolbarter.enums.ItemStatus;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.exception.ForbiddenException;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.SlotRepository;
import com.toolbarter.repository.UserFavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private UserFavoriteRepository userFavoriteRepository;

    @Mock
    private GeoProximityService geoProximityService;

    @Mock
    private AuthService authService;

    private ItemService itemService;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(
                itemRepository, slotRepository, borrowRequestRepository,
                userFavoriteRepository, geoProximityService, authService
        );

        owner = new User();
        owner.setId(1L);
        owner.setName("Owner User");
        owner.setEmail("owner@example.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");
    }

    @Test
    @DisplayName("createItem attaches authenticated owner and saves item")
    void createItem_Success() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(owner);

        CreateItemRequest req = new CreateItemRequest();
        req.setTitle("Cordless Drill");
        req.setDescription("18V Lithium-Ion");
        req.setCategory(ItemCategory.POWER_TOOLS);
        req.setConditionGrade(ItemCondition.GOOD);
        req.setLatitude(17.44);
        req.setLongitude(78.38);

        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(10L);
            return i;
        });

        ItemResponse res = itemService.createItem(req);

        assertNotNull(res);
        assertEquals("Cordless Drill", res.getTitle());
        assertEquals(owner.getId(), res.getOwnerId());
    }

    @Test
    @DisplayName("updateItem throws ForbiddenException if caller is not the owner")
    void updateItem_NonOwner_ThrowsForbidden() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(otherUser);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        UpdateItemRequest req = new UpdateItemRequest();
        req.setTitle("Hacked Title");

        assertThrows(ForbiddenException.class, () -> itemService.updateItem(10L, req));
    }

    @Test
    @DisplayName("deleteItem throws BadRequestException if active loans exist")
    void deleteItem_ActiveLoan_ThrowsBadRequest() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(owner);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        BorrowRequest activeLoan = new BorrowRequest();
        activeLoan.setStatus(BorrowStatus.PICKED_UP);
        when(borrowRequestRepository.findByItemIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(activeLoan));

        assertThrows(BadRequestException.class, () -> itemService.deleteItem(10L));
    }

    @Test
    @DisplayName("getNearbyItems calculates real distances and filters by radius")
    void getNearbyItems_FiltersAndSortsByDistance() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Item Close");
        item1.setStatus(ItemStatus.LISTED);
        item1.setLatitude(17.441);
        item1.setLongitude(78.381);
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Item Far");
        item2.setStatus(ItemStatus.LISTED);
        item2.setLatitude(17.500);
        item2.setLongitude(78.500);
        item2.setOwner(owner);

        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));

        when(geoProximityService.calculateDistance(eq(17.44), eq(78.38), eq(17.441), eq(78.381))).thenReturn(0.5);
        when(geoProximityService.calculateDistance(eq(17.44), eq(78.38), eq(17.500), eq(78.500))).thenReturn(15.0);

        // Search with radius 5.0 km
        List<ItemResponse> results = itemService.getNearbyItems(17.44, 78.38, 5.0);

        assertEquals(1, results.size());
        assertEquals("Item Close", results.get(0).getTitle());
        assertEquals(0.5, results.get(0).getDistanceKm());
    }
}
