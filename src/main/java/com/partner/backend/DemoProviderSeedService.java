package com.partner.backend;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoProviderSeedService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LabRepository labRepository;
    private final WalletRepository walletRepository;
    private final AvailabilityRepository availabilityRepository;
    private final LabAvailabilityRepository labAvailabilityRepository;
    private final LabTestRepository labTestRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seedDemoProvidersIfAbsent() {
        seedDoctor();
        seedPharmacy();
        seedLab();
        hydrateDemoCatalogForDemoAnchors();
    }

    /** Adds slots, catalog items to demo providers when DB already had users but lacked tests/inventory */
    private void hydrateDemoCatalogForDemoAnchors() {
        doctorRepository.findAll().stream()
                .filter(d -> "PMD-DEMO-DOC".equalsIgnoreCase(Optional.ofNullable(d.getPmdcNumber()).orElse("")))
                .findFirst()
                .ifPresent(this::ensureDoctorAvailability);

        pharmacyRepository.findAll().stream()
                .filter(p -> "DRAP-DEMO-P".equalsIgnoreCase(Optional.ofNullable(p.getDrapLicense()).orElse("")))
                .findFirst()
                .ifPresent(this::ensurePharmacyInventory);

        labRepository.findAll().stream()
                .filter(l -> "DNLC-DEMO-L".equalsIgnoreCase(Optional.ofNullable(l.getDnlcLicense()).orElse("")))
                .findFirst()
                .ifPresent(lab -> {
                    ensureLabTests(lab);
                    ensureLabAvailability(lab);
                });
    }

    private void ensureLabAvailability(Lab lab) {
        if (!labAvailabilityRepository.findByLabId(lab.getId()).isEmpty()) {
            return;
        }
        for (WeekDay day : WeekDay.values()) {
            LabAvailability a = LabAvailability.builder()
                    .lab(lab)
                    .dayOfWeek(day)
                    .startTime("09:00")
                    .endTime("17:00")
                    .available(day != WeekDay.SUN)
                    .build();
            labAvailabilityRepository.save(a);
        }
        log.info("Seeded weekday availability for demo lab id={}", lab.getId());
    }

    private void ensureDoctorAvailability(Doctor doctor) {
        if (!availabilityRepository.findByDoctorId(doctor.getId()).isEmpty()) {
            return;
        }
        for (WeekDay day : WeekDay.values()) {
            Availability a = Availability.builder()
                    .doctor(doctor)
                    .dayOfWeek(day)
                    .startTime("09:00")
                    .endTime("17:00")
                    .available(day != WeekDay.SUN)
                    .build();
            availabilityRepository.save(a);
        }
        log.info("Seeded weekday availability for demo doctor id={}", doctor.getId());
    }

    private void ensureLabTests(Lab lab) {
        if (labTestRepository.countByLabId(lab.getId()) > 0) {
            return;
        }
        labTestRepository.save(LabTest.builder().lab(lab).testName("Complete Blood Count (CBC)")
                .normalPrice(new BigDecimal("500")).discountedPrice(new BigDecimal("450"))
                .reportTimeHours(6).category("Blood").description("Screens blood cells").build());
        labTestRepository.save(LabTest.builder().lab(lab).testName("HbA1c (Diabetes)")
                .normalPrice(new BigDecimal("1200")).discountedPrice(new BigDecimal("1050"))
                .reportTimeHours(24).category("Blood").description("Avg blood sugar").build());
        labTestRepository.save(LabTest.builder().lab(lab).testName("Lipid Profile")
                .normalPrice(new BigDecimal("1500")).discountedPrice(new BigDecimal("1300"))
                .reportTimeHours(24).category("Blood").description("Cholesterol panel").build());
        log.info("Seeded demo lab tests for lab id={}", lab.getId());
    }

    private void ensurePharmacyInventory(Pharmacy pharmacy) {
        if (inventoryItemRepository.countByPharmacyId(pharmacy.getId()) > 0) {
            return;
        }
        inventoryItemRepository.save(InventoryItem.builder().pharmacy(pharmacy).medicineName("Paracetamol 500mg")
                .quantity(200).unitPrice(new BigDecimal("3.50")).category("Pain relief").build());
        inventoryItemRepository.save(InventoryItem.builder().pharmacy(pharmacy).medicineName("Amoxicillin 500mg Capsule")
                .quantity(80).unitPrice(new BigDecimal("25.00")).category("Antibiotic").build());
        inventoryItemRepository.save(InventoryItem.builder().pharmacy(pharmacy).medicineName("ORS Sachets (10-pack)")
                .quantity(120).unitPrice(new BigDecimal("120.00")).category("Hydration").build());
        inventoryItemRepository.save(InventoryItem.builder().pharmacy(pharmacy).medicineName("Vitamin D 5000 IU")
                .quantity(60).unitPrice(new BigDecimal("850.00")).category("Vitamin").build());
        log.info("Seeded demo inventory for pharmacy id={}", pharmacy.getId());
    }

    private void seedDoctor() {
        String email = "demo.doctor@healthwallet.pk";
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("demo123"))
                .role(UserRole.DOCTOR)
                .build();
        userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .name("Dr. Demo Cardiologist")
                .phone("03001234567")
                .email(email)
                .pmdcNumber("PMD-DEMO-DOC")
                .specialty("Cardiology")
                .experienceYears(10)
                .clinicName("Sehat Clinic")
                .clinicAddress("Gulberg, Lahore")
                .city("Lahore")
                .virtualFee(new BigDecimal("1500.00"))
                .physicalFee(new BigDecimal("2000.00"))
                .bio("Demo provider for local Partner app integration.")
                .status(ProviderStatus.APPROVED)
                .build();
        doctor = doctorRepository.save(doctor);

        walletRepository.save(Wallet.builder()
                .providerId(doctor.getId())
                .providerType(ProviderType.DOCTOR)
                .balance(BigDecimal.ZERO)
                .build());

        log.info("Seeded demo doctor: {} / demo123", email);
    }

    private void seedPharmacy() {
        String email = "demo.pharmacy@healthwallet.pk";
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("demo123"))
                .role(UserRole.PHARMACY)
                .build();
        userRepository.save(user);

        Pharmacy pharmacy = Pharmacy.builder()
                .user(user)
                .name("Demo Sehat Pharmacy")
                .ownerName("Demo Owner")
                .phone("03001234568")
                .email(email)
                .address("Model Town")
                .city("Lahore")
                .drapLicense("DRAP-DEMO-P")
                .status(ProviderStatus.APPROVED)
                .build();
        pharmacy = pharmacyRepository.save(pharmacy);

        walletRepository.save(Wallet.builder()
                .providerId(pharmacy.getId())
                .providerType(ProviderType.PHARMACY)
                .balance(BigDecimal.ZERO)
                .build());

        log.info("Seeded demo pharmacy: {} / demo123", email);
    }

    private void seedLab() {
        String email = "demo.lab@healthwallet.pk";
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("demo123"))
                .role(UserRole.LAB)
                .build();
        userRepository.save(user);

        Lab lab = Lab.builder()
                .user(user)
                .name("Demo Al-Shifa Lab")
                .ownerName("Demo Owner")
                .phone("03001234569")
                .email(email)
                .address("Johar Town")
                .city("Lahore")
                .dnlcLicense("DNLC-DEMO-L")
                .status(ProviderStatus.APPROVED)
                .build();
        lab = labRepository.save(lab);

        walletRepository.save(Wallet.builder()
                .providerId(lab.getId())
                .providerType(ProviderType.LAB)
                .balance(BigDecimal.ZERO)
                .build());

        ensureLabTests(lab);
        ensureLabAvailability(lab);

        log.info("Seeded demo lab: {} / demo123", email);
    }
}
