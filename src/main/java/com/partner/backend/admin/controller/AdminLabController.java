package com.partner.backend.admin.controller;

import com.partner.backend.admin.dto.*;
import com.partner.backend.admin.service.AdminLabService;
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
@RequestMapping("/api/admin/labs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLabController {

    private final AdminLabService labService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResponseWrapper<LabSummaryResponse>>> list(
            @RequestParam(required = false) ProviderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(ResponseWrapper.from(labService.list(status, pageable))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LabSummaryResponse>> add(@Valid @RequestBody AddLabRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Lab added", labService.add(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabSummaryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(labService.getById(id)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LabSummaryResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lab approved", labService.approve(id)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LabSummaryResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Lab rejected", labService.reject(id, req)));
    }

    @PutMapping("/{id}/bank")
    public ResponseEntity<ApiResponse<LabSummaryResponse>> updateBank(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBankAccountRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Bank details updated", labService.updateBankAccount(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        labService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Lab deleted", null));
    }
}
