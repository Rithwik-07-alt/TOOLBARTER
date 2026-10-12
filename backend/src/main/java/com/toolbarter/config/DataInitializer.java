package com.toolbarter.config;

import com.toolbarter.entity.AvailabilitySlot;
import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.entity.Item;
import com.toolbarter.entity.Review;
import com.toolbarter.entity.User;
import com.toolbarter.enums.*;
import com.toolbarter.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final SlotRepository slotRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ItemRepository itemRepository,
                           SlotRepository slotRepository,
                           BorrowRequestRepository borrowRequestRepository,
                           ReviewRepository reviewRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.slotRepository = slotRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // Base coordinates: Hyderabad Tech Corridor (17.4401, 78.3826)
        double baseLat = 17.4401;
        double baseLng = 78.3826;

        // 1. Create Users
        User alice = new User();
        alice.setName("Alice Sharma");
        alice.setEmail("alice@toolbarter.com");
        alice.setPasswordHash(passwordEncoder.encode("password123"));
        alice.setPhone("+91 98765 43210");
        alice.setLatitude(baseLat);
        alice.setLongitude(baseLng);
        alice.setRoles(Set.of(Role.ROLE_OWNER, Role.ROLE_BORROWER));
        alice.setTrustScore(45);
        alice.setTrustLevel(TrustLevel.RELIABLE_LENDER);
        alice.setCreatedAt(LocalDateTime.now().minusDays(30));
        userRepository.save(alice);

        User bob = new User();
        bob.setName("Bob Rao");
        bob.setEmail("bob@toolbarter.com");
        bob.setPasswordHash(passwordEncoder.encode("password123"));
        bob.setPhone("+91 98765 43211");
        bob.setLatitude(baseLat + 0.0035); // ~0.4 km away
        bob.setLongitude(baseLng + 0.0028);
        bob.setRoles(Set.of(Role.ROLE_OWNER, Role.ROLE_BORROWER));
        bob.setTrustScore(78);
        bob.setTrustLevel(TrustLevel.TRUSTED_NEIGHBOUR);
        bob.setCreatedAt(LocalDateTime.now().minusDays(45));
        userRepository.save(bob);

        User carol = new User();
        carol.setName("Carol Reddy");
        carol.setEmail("carol@toolbarter.com");
        carol.setPasswordHash(passwordEncoder.encode("password123"));
        carol.setPhone("+91 98765 43212");
        carol.setLatitude(baseLat - 0.0062); // ~0.7 km away
        carol.setLongitude(baseLng - 0.0041);
        carol.setRoles(Set.of(Role.ROLE_BORROWER));
        carol.setTrustScore(20);
        carol.setTrustLevel(TrustLevel.REGULAR_BORROWER);
        carol.setCreatedAt(LocalDateTime.now().minusDays(15));
        userRepository.save(carol);

        User david = new User();
        david.setName("David Varma");
        david.setEmail("david@toolbarter.com");
        david.setPasswordHash(passwordEncoder.encode("password123"));
        david.setPhone("+91 98765 43213");
        david.setLatitude(baseLat + 0.0110); // ~1.3 km away
        david.setLongitude(baseLng + 0.0085);
        david.setRoles(Set.of(Role.ROLE_OWNER));
        david.setTrustScore(105);
        david.setTrustLevel(TrustLevel.STREET_CHAMPION);
        david.setCreatedAt(LocalDateTime.now().minusDays(90));
        userRepository.save(david);

        // 2. Create Items
        Item item1 = new Item();
        item1.setOwner(alice);
        item1.setTitle("Bosch Professional 750W Rotary Hammer Drill");
        item1.setDescription("Heavy-duty hammer drill with SDS-plus chuck and full masonry bit set. Perfect for concrete wall anchors.");
        item1.setCategory(ItemCategory.POWER_TOOLS);
        item1.setConditionGrade(ItemCondition.EXCELLENT);
        item1.setConditionNotes("Cleaned after every use. Includes safety goggles and auxiliary handle.");
        item1.setOneTimeUse(true);
        item1.setPickupRadiusKm(5.0);
        item1.setMinDurationHours(2);
        item1.setMaxDurationHours(24);
        item1.setLatitude(alice.getLatitude());
        item1.setLongitude(alice.getLongitude());
        item1.setStatus(ItemStatus.LISTED);
        item1.setCreatedAt(LocalDateTime.now().minusDays(10));
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setOwner(bob);
        item2.setTitle("Little Giant 22ft Multi-Position Extension Ladder");
        item2.setDescription("Heavy-duty aircraft-grade aluminum alloy ladder. Can be converted to A-frame, staircase, or 90-degree position.");
        item2.setCategory(ItemCategory.LADDERS_ACCESS);
        item2.setConditionGrade(ItemCondition.GOOD);
        item2.setConditionNotes("Stable rubber feet, supports up to 150 kg.");
        item2.setOneTimeUse(true);
        item2.setPickupRadiusKm(8.0);
        item2.setMinDurationHours(4);
        item2.setMaxDurationHours(48);
        item2.setLatitude(bob.getLatitude());
        item2.setLongitude(bob.getLongitude());
        item2.setStatus(ItemStatus.LISTED);
        item2.setCreatedAt(LocalDateTime.now().minusDays(8));
        itemRepository.save(item2);

        Item item3 = new Item();
        item3.setOwner(david);
        item3.setTitle("Kärcher K4 High Pressure Washer (130 Bar)");
        item3.setDescription("Includes dirt blaster, Vario Power spray lance, and patio surface cleaner attachment. Perfect for driveway restoration.");
        item3.setCategory(ItemCategory.CLEANING_EQUIPMENT);
        item3.setConditionGrade(ItemCondition.EXCELLENT);
        item3.setConditionNotes("6m high-pressure hose. Requires standard outdoor garden tap connection.");
        item3.setOneTimeUse(true);
        item3.setPickupRadiusKm(10.0);
        item3.setMinDurationHours(3);
        item3.setMaxDurationHours(24);
        item3.setLatitude(david.getLatitude());
        item3.setLongitude(david.getLongitude());
        item3.setStatus(ItemStatus.LISTED);
        item3.setCreatedAt(LocalDateTime.now().minusDays(14));
        itemRepository.save(item3);

        Item item4 = new Item();
        item4.setOwner(alice);
        item4.setTitle("Stihl Cordless Lawn Edger & Grass Trimmer");
        item4.setDescription("Battery-powered with twin fast-charging lithium packs. Lightweight, ergonomic, quiet running.");
        item4.setCategory(ItemCategory.GARDEN_YARD);
        item4.setConditionGrade(ItemCondition.GOOD);
        item4.setConditionNotes("Fresh spool of 2.0mm nylon line installed.");
        item4.setOneTimeUse(false);
        item4.setPickupRadiusKm(5.0);
        item4.setMinDurationHours(2);
        item4.setMaxDurationHours(12);
        item4.setLatitude(alice.getLatitude());
        item4.setLongitude(alice.getLongitude());
        item4.setStatus(ItemStatus.LISTED);
        item4.setCreatedAt(LocalDateTime.now().minusDays(5));
        itemRepository.save(item4);

        Item item5 = new Item();
        item5.setOwner(david);
        item5.setTitle("Wagner Control Pro 250M Airless Paint Sprayer");
        item5.setDescription("Compact high-efficiency airless paint sprayer for wall emulsions, varnishes, and wood preservatives.");
        item5.setCategory(ItemCategory.PAINTING_DECORATING);
        item5.setConditionGrade(ItemCondition.EXCELLENT);
        item5.setConditionNotes("Flushed and cleaned thoroughly with mineral spirit. Includes 517 tip.");
        item5.setOneTimeUse(true);
        item5.setPickupRadiusKm(12.0);
        item5.setMinDurationHours(6);
        item5.setMaxDurationHours(72);
        item5.setLatitude(david.getLatitude());
        item5.setLongitude(david.getLongitude());
        item5.setStatus(ItemStatus.LISTED);
        item5.setCreatedAt(LocalDateTime.now().minusDays(20));
        itemRepository.save(item5);

        // 3. Create Availability Slots (future dates)
        LocalDateTime now = LocalDateTime.now();
        AvailabilitySlot slot1 = new AvailabilitySlot(item1, now.plusDays(1).withHour(9).withMinute(0), now.plusDays(1).withHour(18).withMinute(0));
        AvailabilitySlot slot2 = new AvailabilitySlot(item1, now.plusDays(2).withHour(9).withMinute(0), now.plusDays(2).withHour(18).withMinute(0));
        AvailabilitySlot slot3 = new AvailabilitySlot(item2, now.plusDays(1).withHour(8).withMinute(0), now.plusDays(3).withHour(17).withMinute(0));
        AvailabilitySlot slot4 = new AvailabilitySlot(item3, now.plusDays(1).withHour(10).withMinute(0), now.plusDays(2).withHour(16).withMinute(0));
        AvailabilitySlot slot5 = new AvailabilitySlot(item4, now.plusDays(1).withHour(7).withMinute(0), now.plusDays(1).withHour(19).withMinute(0));
        AvailabilitySlot slot6 = new AvailabilitySlot(item5, now.plusDays(2).withHour(9).withMinute(0), now.plusDays(5).withHour(18).withMinute(0));

        slotRepository.save(slot1);
        slotRepository.save(slot2);
        slotRepository.save(slot3);
        slotRepository.save(slot4);
        slotRepository.save(slot5);
        slotRepository.save(slot6);

        // 4. Create one historical completed exchange for Carol borrowing from Bob
        AvailabilitySlot historicalSlot = new AvailabilitySlot(
                item2,
                now.minusDays(6).withHour(9).withMinute(0),
                now.minusDays(5).withHour(18).withMinute(0)
        );
        historicalSlot.setIsBooked(true);
        slotRepository.save(historicalSlot);

        BorrowRequest completedReq = new BorrowRequest();
        completedReq.setItem(item2);
        completedReq.setBorrower(carol);
        completedReq.setSlot(historicalSlot);
        completedReq.setStartTs(now.minusDays(6).withHour(10).withMinute(0));
        completedReq.setEndTs(now.minusDays(5).withHour(16).withMinute(0));
        completedReq.setStatus(BorrowStatus.RETURNED);
        completedReq.setPointsAwarded(true);
        completedReq.setPickupConfirmedAt(now.minusDays(6).withHour(10).withMinute(15));
        completedReq.setReturnConfirmedAt(now.minusDays(5).withHour(15).withMinute(45));
        completedReq.setCreatedAt(now.minusDays(7));
        borrowRequestRepository.save(completedReq);

        // Add 5-star review from Carol to Bob
        Review review1 = new Review(
                completedReq,
                carol,
                bob,
                5,
                "Bob was super helpful and demonstrated how to lock the extension latches safely. Ladder was in pristine condition!"
        );
        review1.setCreatedAt(now.minusDays(5).withHour(17).withMinute(0));
        reviewRepository.save(review1);
    }
}
