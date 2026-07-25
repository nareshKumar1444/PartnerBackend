package com.partner.backend.mobile.pharmacy.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.pharmacy.dto.*;
import com.partner.backend.mobile.pharmacy.service.PharmacyOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/pharmacy/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PHARMACY')")
public class PharmacyOrderController {

    private final PharmacyOrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResponseWrapper<OrderResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(orderService.list(userDetails.getProviderId(), pageable))));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Order status updated",
                orderService.updateStatus(userDetails.getProviderId(), id, req)));
    }
}
