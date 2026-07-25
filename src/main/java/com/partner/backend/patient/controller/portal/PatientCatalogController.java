package com.partner.backend.patient.controller.portal;

import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.patient.dto.portal.*;
import com.partner.backend.patient.service.portal.PatientCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patient/catalog")
@RequiredArgsConstructor
public class PatientCatalogController {

    private final PatientCatalogService catalogService;

    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<ResponseWrapper<PatientDoctorSummaryResponse>>> doctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(catalogService.listDoctors(pageable))));
    }

    @GetMapping("/doctors/{id}")
    public ResponseEntity<ApiResponse<PatientDoctorDetailResponse>> doctor(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getDoctor(id)));
    }

    @GetMapping("/doctors/{id}/slots")
    public ResponseEntity<ApiResponse<List<String>>> doctorSlots(
            @PathVariable Long id,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.slotsForDoctor(id, date)));
    }

    @GetMapping("/labs")
    public ResponseEntity<ApiResponse<ResponseWrapper<PatientLabSummaryResponse>>> labs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(catalogService.listLabs(pageable))));
    }

    @GetMapping("/labs/{id}/tests")
    public ResponseEntity<ApiResponse<List<PatientLabTestResponse>>> labTests(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.listTestsForLab(id)));
    }

    @GetMapping("/labs/{id}/slots")
    public ResponseEntity<ApiResponse<List<String>>> labSlots(
            @PathVariable Long id,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.slotsForLab(id, date)));
    }

    @GetMapping("/pharmacies")
    public ResponseEntity<ApiResponse<ResponseWrapper<PatientPharmacySummaryResponse>>> pharmacies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(catalogService.listPharmacies(pageable))));
    }

    @GetMapping("/pharmacies/{id}/inventory")
    public ResponseEntity<ApiResponse<ResponseWrapper<PatientInventoryItemResponse>>> inventory(
            @PathVariable Long id,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("medicineName"));
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(catalogService.inventory(id, query, pageable))));
    }
}
