package com.partner.backend.admin.controller;

import com.partner.backend.admin.dto.*;
import com.partner.backend.admin.service.AdminPharmacyService;
import com.partner.backend.common.entity.ProviderStatus;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pharmacies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPharmacyController {

    private final AdminPharmacyService pharmacyService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResponseWrapper<PharmacySummaryResponse>>> list(
            @RequestParam(required = false) ProviderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(ResponseWrapper.from(pharmacyService.list(status, pageable))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PharmacySummaryResponse>> add(@Valid @RequestBody AddPharmacyRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Pharmacy added", pharmacyService.add(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PharmacySummaryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(pharmacyService.getById(id)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PharmacySummaryResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Pharmacy approved", pharmacyService.approve(id)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<PharmacySummaryResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Pharmacy rejected", pharmacyService.reject(id, req)));
    }

    @PutMapping("/{id}/bank")
    public ResponseEntity<ApiResponse<PharmacySummaryResponse>> updateBank(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBankAccountRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Bank details updated", pharmacyService.updateBankAccount(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        pharmacyService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Pharmacy deleted", null));
    }
}
