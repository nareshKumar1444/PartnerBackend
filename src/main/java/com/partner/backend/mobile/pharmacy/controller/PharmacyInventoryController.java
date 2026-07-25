package com.partner.backend.mobile.pharmacy.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.pharmacy.dto.*;
import com.partner.backend.mobile.pharmacy.service.PharmacyInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/pharmacy/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PHARMACY')")
public class PharmacyInventoryController {

    private final PharmacyInventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResponseWrapper<InventoryItemResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("medicineName"));
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(inventoryService.list(userDetails.getProviderId(), query, pageable))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryItemResponse>> add(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InventoryItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Item added", inventoryService.add(userDetails.getProviderId(), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Item updated",
                inventoryService.update(userDetails.getProviderId(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        inventoryService.delete(userDetails.getProviderId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Item removed"));
    }
}
