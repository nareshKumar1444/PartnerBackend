package com.partner.backend.patient.dto.portal;

import com.partner.backend.common.entity.OnlinePaymentChannel;
import com.partner.backend.common.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PatientPlaceOrderRequest {
    @NotNull
    private Long pharmacyId;
    /** Optional — defaults to patient's city line if blank */
    private String deliveryAddress;

    /** Optional — pharmacy may waive; default treated as BigDecimal.ZERO if null */
    private BigDecimal deliveryFee;

    @NotNull
    private PaymentMethod paymentMethod;

    /** Required when paymentMethod is ONLINE */
    private OnlinePaymentChannel onlineChannel;

    /** Optional client reference (card/bank txn id); server may generate if blank */
    @Size(max = 64)
    private String paymentReference;

    /** Bank display name when onlineChannel is BANK */
    @Size(max = 120)
    private String paymentBankName;

    @NotEmpty
    @Valid
    private List<PatientOrderLineRequest> items;

    @Data
    public static class PatientOrderLineRequest {
        @NotNull
        private Long inventoryItemId;
        @NotNull
        @Positive
        private Integer quantity;
    }
}
