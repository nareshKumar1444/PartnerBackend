package com.partner.backend.mobile.pharmacy.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.pharmacy.dto.PharmacyDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PharmacyDashboardService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final InventoryItemRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;

    public PharmacyDashboardResponse getDashboard(Long pharmacyId) {
        long totalItems = inventoryRepository.countByPharmacyId(pharmacyId);
        long lowStock = inventoryRepository.countByPharmacyIdAndQuantityLessThan(pharmacyId, LOW_STOCK_THRESHOLD);
        long expiring = inventoryRepository.countByPharmacyIdAndExpiryDateBefore(pharmacyId,
                LocalDate.now().plusDays(30));
        long pendingOrders = orderRepository.countByPharmacyIdAndStatus(pharmacyId, OrderStatus.PENDING);
        long totalOrders = orderRepository.countByPharmacyId(pharmacyId);

        Double rawRevenue = orderRepository.sumCompletedAmountByPharmacyId(pharmacyId);
        BigDecimal revenue = rawRevenue != null ? BigDecimal.valueOf(rawRevenue) : BigDecimal.ZERO;

        BigDecimal walletBalance = walletRepository
                .findByProviderIdAndProviderType(pharmacyId, ProviderType.PHARMACY)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        return PharmacyDashboardResponse.builder()
                .totalItems(totalItems)
                .lowStockAlerts(lowStock)
                .expiringItems(expiring)
                .pendingOrders(pendingOrders)
                .totalOrders(totalOrders)
                .totalRevenue(revenue)
                .walletBalance(walletBalance)
                .build();
    }
}
