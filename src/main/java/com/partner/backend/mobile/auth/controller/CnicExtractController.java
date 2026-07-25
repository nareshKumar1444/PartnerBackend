package com.partner.backend.mobile.auth.controller;

import com.partner.backend.common.entity.UserRole;
import com.partner.backend.common.service.CnicImageOcrService;
import com.partner.backend.common.repository.UserRepository;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.CnicNormalizer;
import com.partner.backend.mobile.auth.dto.CnicAvailabilityResponse;
import com.partner.backend.mobile.auth.dto.CnicExtractResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumSet;

@RestController
@RequestMapping("/api/auth/provider/cnic")
@RequiredArgsConstructor
public class CnicExtractController {

    private final CnicImageOcrService ocrService;
    private final UserRepository userRepository;

    /** Roles that attach a CNIC to {@code users.normalized_cnic} for duplicate checks */
    private static final EnumSet<UserRole> CNIC_REGISTRATION_ROLES =
            EnumSet.of(UserRole.DOCTOR, UserRole.LAB, UserRole.PHARMACY, UserRole.PATIENT);

    /**
     * Upload CNIC image; optional OCR extracts 13-digit number when configured.
     */
    @PostMapping(value = "/extract-from-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CnicExtractResponse>> extractFromImage(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ok("No image uploaded", CnicExtractResponse.builder()
                            .ocrConfigured(ocrService.isConfigured())
                            .message("Choose a CNIC photo first.")
                            .build()));
        }
        boolean configured = ocrService.isConfigured();
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ok("Unable to read file", CnicExtractResponse.builder()
                            .ocrConfigured(configured)
                            .message(e.getMessage())
                            .build()));
        }
        var found = ocrService.extractNormalizedCnic(bytes, file.getOriginalFilename());
        if (found.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok("CNIC detected", CnicExtractResponse.builder()
                    .ocrConfigured(configured)
                    .suggestedCnic(found.get())
                    .message("Review the CNIC and correct if needed before submitting.")
                    .build()));
        }
        String msg = configured
                ? "Could not read a valid 13-digit CNIC from this image. Enter CNIC manually."
                : "OCR is not configured on server. Enter your CNIC manually (13 digits).";
        return ResponseEntity.ok(ApiResponse.ok(msg, CnicExtractResponse.builder()
                .ocrConfigured(configured)
                .message(msg)
                .build()));
    }

    @GetMapping("/check-availability")
    public ResponseEntity<ApiResponse<CnicAvailabilityResponse>> checkAvailability(
            @RequestParam("cnic") String cnicRaw,
            @RequestParam("role") String roleRaw) {
        UserRole role;
        try {
            role = UserRole.valueOf(roleRaw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid role. Use DOCTOR, LAB, PHARMACY, or PATIENT."));
        }
        if (!CNIC_REGISTRATION_ROLES.contains(role)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CNIC check is only for DOCTOR, LAB, PHARMACY, or PATIENT."));
        }
        String normalized = CnicNormalizer.normalize(cnicRaw);
        if (!CnicNormalizer.isValid13(normalized)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CNIC must be exactly 13 digits."));
        }
        boolean available = !userRepository.existsByNormalizedCnicAndRole(normalized, role);
        String roleLabel = registrationRoleLabel(role);
        String message = available
                ? ("CNIC is available for registration as " + roleLabel + ".")
                : ("This CNIC is already registered as a " + roleLabel + ".");
        return ResponseEntity.ok(ApiResponse.ok(
                message,
                CnicAvailabilityResponse.builder()
                        .normalizedCnic(normalized)
                        .available(available)
                        .message(message)
                        .build()));
    }

    private static String registrationRoleLabel(UserRole role) {
        return switch (role) {
            case DOCTOR -> "Doctor";
            case LAB -> "Laboratory";
            case PHARMACY -> "Pharmacy";
            case PATIENT -> "Patient";
            default -> capitalizeWords(role.name().replace('_', ' '));
        };
    }

    private static String capitalizeWords(String s) {
        String[] parts = s.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            if (parts[i].isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }
}
