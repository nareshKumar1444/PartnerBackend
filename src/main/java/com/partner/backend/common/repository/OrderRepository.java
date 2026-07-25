package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Order;
import com.partner.backend.common.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByPharmacyId(Long pharmacyId, Pageable pageable);
    long countByPharmacyIdAndStatus(Long pharmacyId, OrderStatus status);
    long countByPharmacyId(Long pharmacyId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.pharmacy.id = :pharmacyId AND o.status = 'COMPLETED'")
    Double sumCompletedAmountByPharmacyId(Long pharmacyId);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.pharmacy.id = :pharmacyId AND o.status = 'COMPLETED'
            AND o.createdAt >= :start AND o.createdAt < :end
            """)
    BigDecimal sumCompletedAmountByPharmacyBetween(Long pharmacyId, LocalDateTime start, LocalDateTime end);

    List<Order> findByPatient_IdOrderByCreatedAtDesc(Long patientId);
}
