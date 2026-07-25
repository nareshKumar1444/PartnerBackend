package com.partner.backend.admin.controller;

import com.partner.backend.admin.dto.DeletedProviderResponse;
import com.partner.backend.admin.service.AdminDeletedProvidersService;
import com.partner.backend.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/deleted-providers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeletedProvidersController {

    private final AdminDeletedProvidersService deletedProvidersService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeletedProviderResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(deletedProvidersService.list()));
    }

    @PostMapping("/doctors/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreDoctor(@PathVariable Long id) {
        deletedProvidersService.restoreDoctor(id);
        return ResponseEntity.ok(ApiResponse.ok("Doctor restored", null));
    }

    @PostMapping("/pharmacies/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restorePharmacy(@PathVariable Long id) {
        deletedProvidersService.restorePharmacy(id);
        return ResponseEntity.ok(ApiResponse.ok("Pharmacy restored", null));
    }

    @PostMapping("/labs/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreLab(@PathVariable Long id) {
        deletedProvidersService.restoreLab(id);
        return ResponseEntity.ok(ApiResponse.ok("Lab restored", null));
    }
}
