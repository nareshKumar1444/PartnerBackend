package com.partner.backend.patient.service.portal;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.InventoryItemRepository;
import com.partner.backend.common.repository.OrderRepository;
import com.partner.backend.common.repository.PatientRepository;
import com.partner.backend.common.repository.PharmacyRepository;
import com.partner.backend.common.service.ProviderNotificationService;
import com.partner.backend.patient.dto.portal.PatientOrderSummaryResponse;
import com.partner.backend.patient.dto.portal.PatientPlaceOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.partner.backend.common.util.InventoryItemExpiryRules;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class PatientOrderBookingService {

    private final OrderRepository orderRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PatientRepository patientRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ProviderNotificationService providerNotificationService;

    @Transactional(readOnly = true)
    public List<PatientOrderSummaryResponse> list(Long patientId) {
        return orderRepository.findByPatient_IdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public PatientOrderSummaryResponse place(Long patientId, @Valid PatientPlaceOrderRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        Pharmacy pharmacy = pharmacyRepository.findById(req.getPharmacyId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", req.getPharmacyId()));
        if (pharmacy.getStatus() != ProviderStatus.APPROVED) {
            throw new BadRequestException("This pharmacy is not available.");
        }

        PaymentMethod paymentMethod = req.getPaymentMethod();
        if (paymentMethod == null) {
            throw new BadRequestException("Payment method is required.");
        }
        OnlinePaymentChannel onlineChannel = req.getOnlineChannel();
        if (paymentMethod == PaymentMethod.ONLINE && onlineChannel == null) {
            throw new BadRequestException("Select card or bank transfer for online payment.");
        }
        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY && onlineChannel != null) {
            throw new BadRequestException("Online channel is only used for online payments.");
        }

        BigDecimal itemsTotal = BigDecimal.ZERO;
        List<OrderItem> built = new ArrayList<>();

        for (PatientPlaceOrderRequest.PatientOrderLineRequest line : req.getItems()) {
            InventoryItem inv = inventoryItemRepository.findByIdForUpdate(line.getInventoryItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", line.getInventoryItemId()));
            if (!inv.getPharmacy().getId().equals(pharmacy.getId())) {
                throw new BadRequestException("One of the medicines is not sold by this pharmacy.");
            }
            if (inv.getQuantity() < line.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + inv.getMedicineName());
            }
            if (!InventoryItemExpiryRules.isSellable(inv)) {
                throw new BadRequestException(inv.getMedicineName() + " has expired and cannot be ordered.");
            }
            BigDecimal unit = inv.getUnitPrice();
            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(line.getQuantity()));
            itemsTotal = itemsTotal.add(lineTotal);

            OrderItem oi = new OrderItem();
            oi.setInventoryItem(inv);
            oi.setMedicineName(inv.getMedicineName());
            oi.setQuantity(line.getQuantity());
            oi.setUnitPrice(unit);
            built.add(oi);

            inv.setQuantity(inv.getQuantity() - line.getQuantity());
        }

        BigDecimal deliveryFee = req.getDeliveryFee() != null ? req.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal grand = itemsTotal.add(deliveryFee);

        String address = req.getDeliveryAddress();
        if (address == null || address.isBlank()) {
            String city = patient.getCity() != null ? patient.getCity() : "";
            address = patient.getName() + " — " + city;
        }

        PaymentStatus paymentStatus = paymentMethod == PaymentMethod.ONLINE
                ? PaymentStatus.PAID
                : PaymentStatus.PENDING;
        String paymentReference = resolvePaymentReference(req, paymentMethod, onlineChannel);
        String paymentBankName = paymentMethod == PaymentMethod.ONLINE && onlineChannel == OnlinePaymentChannel.BANK
                ? trimToNull(req.getPaymentBankName())
                : null;

        Order order = Order.builder()
                .pharmacy(pharmacy)
                .patient(patient)
                .patientName(patient.getName())
                .patientPhone(patient.getPhone())
                .deliveryAddress(address.trim())
                .deliveryFee(deliveryFee)
                .status(OrderStatus.PENDING)
                .totalAmount(grand)
                .paymentMethod(paymentMethod)
                .onlinePaymentChannel(onlineChannel)
                .paymentStatus(paymentStatus)
                .paymentReference(paymentReference)
                .paymentBankName(paymentBankName)
                .build();

        for (OrderItem oi : built) {
            oi.setOrder(order);
            order.getItems().add(oi);
        }

        order = orderRepository.save(order);
        providerNotificationService.notifyPharmacyNewOrder(order);
        return toSummary(order);
    }

    private PatientOrderSummaryResponse toSummary(Order o) {
        List<PatientOrderSummaryResponse.PatientOrderLineResponse> meds = o.getItems().stream()
                .map(i -> PatientOrderSummaryResponse.PatientOrderLineResponse.builder()
                        .inventoryItemId(i.getInventoryItem() != null ? i.getInventoryItem().getId() : null)
                        .medicineName(i.getMedicineName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toList();

        return PatientOrderSummaryResponse.builder()
                .id(o.getId())
                .pharmacyId(o.getPharmacy().getId())
                .pharmacyName(o.getPharmacy().getName())
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
                .medicines(meds)
                .createdAt(o.getCreatedAt())
                .build();
    }

    private static String resolvePaymentReference(
            PatientPlaceOrderRequest req,
            PaymentMethod paymentMethod,
            OnlinePaymentChannel onlineChannel) {
        String clientRef = trimToNull(req.getPaymentReference());
        if (clientRef != null) {
            return clientRef;
        }
        if (paymentMethod != PaymentMethod.ONLINE) {
            return null;
        }
        String prefix = onlineChannel == OnlinePaymentChannel.CARD ? "CARD" : "BANK";
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + "-" + ts + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
