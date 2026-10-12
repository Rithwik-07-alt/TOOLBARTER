package com.toolbarter.repository;

import com.toolbarter.entity.BorrowRequest;
import com.toolbarter.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    List<BorrowRequest> findByBorrowerIdOrderByCreatedAtDesc(Long borrowerId);

    @Query("SELECT r FROM BorrowRequest r WHERE r.item.owner.id = :ownerId ORDER BY r.createdAt DESC")
    List<BorrowRequest> findByItemOwnerIdOrderByCreatedAtDesc(@Param("ownerId") Long ownerId);

    List<BorrowRequest> findByItemIdOrderByCreatedAtDesc(Long itemId);

    @Query("SELECT r FROM BorrowRequest r WHERE r.item.id = :itemId AND r.status IN :statuses " +
           "AND r.startTs < :endTs AND r.endTs > :startTs")
    List<BorrowRequest> findConflictingRequests(@Param("itemId") Long itemId,
                                                @Param("startTs") LocalDateTime startTs,
                                                @Param("endTs") LocalDateTime endTs,
                                                @Param("statuses") Collection<BorrowStatus> statuses);

    long countByBorrowerIdAndStatus(Long borrowerId, BorrowStatus status);

    @Query("SELECT COUNT(r) FROM BorrowRequest r WHERE r.item.owner.id = :ownerId AND r.status = :status")
    long countByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BorrowStatus status);

    long countByStatus(BorrowStatus status);

    @Query("SELECT r FROM BorrowRequest r JOIN FETCH r.borrower JOIN FETCH r.item WHERE r.status = :status")
    List<BorrowRequest> findWithDetailsByStatus(@Param("status") BorrowStatus status);
}
