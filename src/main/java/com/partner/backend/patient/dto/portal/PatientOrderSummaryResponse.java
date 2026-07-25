package com.partner.backend.patient.dto.portal;

import com.partner.backend.common.entity.OnlinePaymentChannel;
import com.partner.backend.common.entity.OrderStatus;
import com.partner.backend.common.entity.PaymentMethod;
import com.partner.backend.common.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientOrderSummaryResponse {
    private Long id;
    private Long pharmacyId;
    private String pharmacyName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String deliveryAddress;
    private PaymentMethod paymentMethod;
    private OnlinePaymentChannel onlineChannel;
    private PaymentStatus paymentStatus;
    private String paymentReference;
    private String paymentBankName;
    private String riderName;
    private String riderPhone;
    private LocalDateTime riderAssignedAt;
    private List<PatientOrderLineResponse> medicines;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientOrderLineResponse {
        private Long inventoryItemId;
        private String medicineName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
