package com.partner.backend.mobile.pharmacy.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.util.InventoryItemExpiryRules;
import com.partner.backend.mobile.pharmacy.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PharmacyInventoryService {

    private final InventoryItemRepository inventoryRepository;
    private final PharmacyRepository pharmacyRepository;

    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> list(Long pharmacyId, String query, Pageable pageable) {
        Page<InventoryItem> page = (query != null && !query.isBlank())
                ? inventoryRepository.searchByPharmacyId(pharmacyId, query, pageable)
                : inventoryRepository.findByPharmacyId(pharmacyId, pageable);
        return page.map(this::toResponse);
    }

    @Transactional
    public InventoryItemResponse add(Long pharmacyId, InventoryItemRequest req) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", pharmacyId));

        InventoryItem item = InventoryItem.builder()
                .pharmacy(pharmacy)
                .medicineName(req.getMedicineName())
                .quantity(req.getQuantity())
                .unitPrice(req.getUnitPrice())
                .expiryDate(req.getExpiryDate())
                .category(req.getCategory())
                .manufacturer(req.getManufacturer())
                .build();
        return toResponse(inventoryRepository.save(item));
    }

    @Transactional
    public InventoryItemResponse update(Long pharmacyId, Long itemId, InventoryItemRequest req) {
        InventoryItem item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", itemId));

        if (!item.getPharmacy().getId().equals(pharmacyId)) {
            throw new UnauthorizedException("Access denied");
        }

        item.setMedicineName(req.getMedicineName());
        item.setQuantity(req.getQuantity());
        item.setUnitPrice(req.getUnitPrice());
        item.setExpiryDate(req.getExpiryDate());
        if (req.getCategory() != null) item.setCategory(req.getCategory());
        if (req.getManufacturer() != null) item.setManufacturer(req.getManufacturer());
        if (req.getExpiryDate() == null || !req.getExpiryDate().isBefore(InventoryItemExpiryRules.todayInPakistan())) {
            item.setStatus(InventoryItemStatus.ACTIVE);
        }

        return toResponse(inventoryRepository.save(item));
    }

    @Transactional
    public void delete(Long pharmacyId, Long itemId) {
        InventoryItem item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", itemId));

        if (!item.getPharmacy().getId().equals(pharmacyId)) {
            throw new UnauthorizedException("Access denied");
        }
        inventoryRepository.delete(item);
    }

    private InventoryItemResponse toResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .medicineName(item.getMedicineName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .expiryDate(item.getExpiryDate())
                .category(item.getCategory())
                .manufacturer(item.getManufacturer())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
