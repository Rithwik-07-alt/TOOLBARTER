package com.toolbarter.service;

import com.toolbarter.dto.NeighbourhoodImpactResponse;
import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Item;
import com.toolbarter.entity.User;
import com.toolbarter.enums.BorrowStatus;
import com.toolbarter.enums.ItemStatus;
import com.toolbarter.repository.BorrowRequestRepository;
import com.toolbarter.repository.ItemRepository;
import com.toolbarter.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {

    private final ItemRepository itemRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final UserRepository userRepository;
    private final GeoProximityService geoProximityService;

    public StatsService(ItemRepository itemRepository,
                        BorrowRequestRepository borrowRequestRepository,
                        UserRepository userRepository,
                        GeoProximityService geoProximityService) {
        this.itemRepository = itemRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.userRepository = userRepository;
        this.geoProximityService = geoProximityService;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public NeighbourhoodImpactResponse getNeighbourhoodImpact() {
        long itemsListed = itemRepository.count();

        List<BorrowRequest> completedRequests = borrowRequestRepository.findWithDetailsByStatus(BorrowStatus.RETURNED);

        long completedExchanges = completedRequests.size();

        // Calculate actual distance saved: 2x round-trip from borrower to item for each completed exchange
        double distanceSaved = 0.0;
        for (BorrowRequest req : completedRequests) {
            if (req.getBorrower() != null && req.getItem() != null &&
                    req.getBorrower().getLatitude() != null && req.getBorrower().getLongitude() != null &&
                    req.getItem().getLatitude() != null && req.getItem().getLongitude() != null) {
                double dist = geoProximityService.calculateDistance(
                        req.getBorrower().getLatitude(), req.getBorrower().getLongitude(),
                        req.getItem().getLatitude(), req.getItem().getLongitude()
                );
                // Assume trip to a commercial store is at least distance or 2x round trip local barter
                distanceSaved += (dist * 2);
            }
        }
        distanceSaved = Math.round(distanceSaved * 10.0) / 10.0;

        List<User> allUsers = userRepository.findAll();
        double avgTrust = 0.0;
        if (!allUsers.isEmpty()) {
            double totalScore = allUsers.stream().mapToInt(u -> u.getTrustScore() != null ? u.getTrustScore() : 0).sum();
            avgTrust = Math.round((totalScore / allUsers.size()) * 10.0) / 10.0;
        }

        return new NeighbourhoodImpactResponse(itemsListed, completedExchanges, distanceSaved, avgTrust);
    }
}
