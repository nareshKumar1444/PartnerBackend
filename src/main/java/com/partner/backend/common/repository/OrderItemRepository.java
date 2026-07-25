package com.partner.backend.common.repository;

import com.partner.backend.common.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.inventoryItem WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderIdWithInventory(Long orderId);
}
