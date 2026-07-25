package com.partner.backend.common.repository;

import com.partner.backend.common.entity.InventoryItem;
import com.partner.backend.common.entity.InventoryItemStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Page<InventoryItem> findByPharmacyId(Long pharmacyId, Pageable pageable);
    long countByPharmacyId(Long pharmacyId);
    long countByPharmacyIdAndQuantityLessThan(Long pharmacyId, int threshold);
    long countByPharmacyIdAndExpiryDateBefore(Long pharmacyId, LocalDate date);

    @Query("SELECT i FROM InventoryItem i WHERE i.pharmacy.id = :pharmacyId AND " +
           "(LOWER(i.medicineName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<InventoryItem> searchByPharmacyId(Long pharmacyId, String query, Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE i.pharmacy.id = :pharmacyId AND i.status = :status " +
           "AND i.quantity > 0 AND (i.expiryDate IS NULL OR i.expiryDate >= :today)")
    Page<InventoryItem> findSellableByPharmacyId(
            @Param("pharmacyId") Long pharmacyId,
            @Param("status") InventoryItemStatus status,
            @Param("today") LocalDate today,
            Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE i.pharmacy.id = :pharmacyId AND i.status = :status " +
           "AND i.quantity > 0 AND (i.expiryDate IS NULL OR i.expiryDate >= :today) AND " +
           "LOWER(i.medicineName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<InventoryItem> searchSellableByPharmacyId(
            @Param("pharmacyId") Long pharmacyId,
            @Param("query") String query,
            @Param("status") InventoryItemStatus status,
            @Param("today") LocalDate today,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE InventoryItem i SET i.status = :expired WHERE i.status = :active " +
           "AND i.expiryDate IS NOT NULL AND i.expiryDate < :today")
    int markExpiredBefore(
            @Param("today") LocalDate today,
            @Param("active") InventoryItemStatus active,
            @Param("expired") InventoryItemStatus expired);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE inventory_items SET status = 'ACTIVE' WHERE status IS NULL", nativeQuery = true)
    int backfillNullStatus();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.id = :id")
    Optional<InventoryItem> findByIdForUpdate(Long id);
}
