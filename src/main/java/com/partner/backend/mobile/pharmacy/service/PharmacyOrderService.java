package com.partner.backend.mobile.pharmacy.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.service.ProviderNotificationService;
import com.partner.backend.mobile.pharmacy.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacyOrderService {
    private static final String[][] RIDER_POOL = {
            {"Ali Rider", "0300-1112233"},
            {"Usman Courier", "0311-9876543"},
            {"Hassan Delivery", "0321-4567890"},
            {"Saad Express", "0333-7412589"}
    };

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ProviderNotificationService providerNotificationService;

    @Transactional(readOnly = true)
    public Page<OrderResponse> list(Long pharmacyId, Pageable pageable) {
        return orderRepository.findByPharmacyId(pharmacyId, pageable).map(this::toResponse);
    }

    @Transactional
    public OrderResponse updateStatus(Long pharmacyId, Long orderId, OrderStatusUpdateRequest req) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getPharmacy().getId().equals(pharmacyId)) {
            throw new UnauthorizedException("Access denied");
        }

        OrderStatus current = order.getStatus();
        OrderStatus next = req.getStatus();
        if (current == next) {
            return toResponse(order);
        }

        if (!isAllowedTransition(current, next)) {
            throw new BadRequestException(
                    "Cannot change order status from " + current + " to " + next);
        }

        if (shouldRestoreInventory(current, next)) {
            restoreInventory(orderId);
        }

        order.setStatus(next);
        if (next == OrderStatus.COMPLETED && (order.getRiderName() == null || order.getRiderName().isBlank())) {
            assignRider(order);
        }
        order = orderRepository.save(order);
        providerNotificationService.notifyPatientOrderStatus(order, current);
        return toResponse(order);
    }

    private static boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return allowedTargets(from).contains(to);
    }

    private static Set<OrderStatus> allowedTargets(OrderStatus from) {
        return switch (from) {
            case PENDING -> EnumSet.of(OrderStatus.ACCEPTED, OrderStatus.REJECTED, OrderStatus.CANCELLED);
            case ACCEPTED -> EnumSet.of(OrderStatus.PREPARING, OrderStatus.PROCESSING, OrderStatus.CANCELLED);
            case PREPARING, PROCESSING -> EnumSet.of(OrderStatus.READY, OrderStatus.CANCELLED);
            case READY -> EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED);
            case COMPLETED, REJECTED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    private static boolean shouldRestoreInventory(OrderStatus from, OrderStatus to) {
        return from == OrderStatus.PENDING
                && (to == OrderStatus.REJECTED || to == OrderStatus.CANCELLED);
    }

    private void restoreInventory(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderIdWithInventory(orderId);
        for (OrderItem item : items) {
            if (item.getInventoryItem() == null || item.getInventoryItem().getId() == null) {
                continue;
            }
            inventoryItemRepository.findByIdForUpdate(item.getInventoryItem().getId())
                    .ifPresent(inv -> inv.setQuantity(inv.getQuantity() + item.getQuantity()));
        }
    }

    private static void assignRider(Order order) {
        int idx = (int) (order.getId() % RIDER_POOL.length);
        String[] rider = RIDER_POOL[idx];
        order.setRiderName(rider[0]);
        order.setRiderPhone(rider[1]);
        order.setRiderAssignedAt(LocalDateTime.now());
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdWithInventory(o.getId());
        List<OrderResponse.OrderItemResponse> items = orderItems.stream()
                .map(i -> OrderResponse.OrderItemResponse.builder()
                        .id(i.getId())
                        .medicineName(i.getMedicineName() != null ? i.getMedicineName()
                                : (i.getInventoryItem() != null ? i.getInventoryItem().getMedicineName() : ""))
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId())
                .patientName(o.getPatientName())
                .patientPhone(o.getPatientPhone())
                .status(o.getStatus())
                .totalAmount(o.getTotalAmount())
                .deliveryFee(o.getDeliveryFee())
                .deliveryAddress(o.getDeliveryAddress())
                .paymentMethod(o.getPaymentMethod())
                .onlineChannel(o.getOnlinePaymentChannel())
                .paymentStatus(o.getPaymentStatus())
                .paymentReference(o.getPaymentReference())
                .paymentBankName(o.getPaymentBankName())
                .riderName(o.getRiderName())
                .riderPhone(o.getRiderPhone())
                .riderAssignedAt(o.getRiderAssignedAt())
                .items(items)
                .createdAt(o.getCreatedAt())
                .build();
    }
}
