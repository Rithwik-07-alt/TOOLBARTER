package com.toolbarter.repository;

import com.toolbarter.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByItemIdOrderBySlotStartAsc(Long itemId);

    @Query("SELECT s FROM AvailabilitySlot s WHERE s.item.id = :itemId AND s.slotEnd > :now AND s.isBooked = false ORDER BY s.slotStart ASC")
    List<AvailabilitySlot> findOpenSlotsByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);
}
