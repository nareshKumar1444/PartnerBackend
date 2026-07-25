package com.partner.backend.mobile.pharmacy.dto;

import com.partner.backend.common.entity.InventoryItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemResponse {
    private Long id;
    private String medicineName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDate expiryDate;
    private String category;
    private String manufacturer;
    private InventoryItemStatus status;
    private LocalDateTime createdAt;
}
