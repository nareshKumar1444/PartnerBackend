package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.DeletedProviderResponse;
import com.partner.backend.common.entity.ProviderStatus;
import com.partner.backend.common.repository.DoctorRepository;
import com.partner.backend.common.repository.LabRepository;
import com.partner.backend.common.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDeletedProvidersService {

    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LabRepository labRepository;
    private final AdminProviderDeletionService providerDeletionService;

    @Transactional(readOnly = true)
    public List<DeletedProviderResponse> list() {
        List<DeletedProviderResponse> out = new ArrayList<>();
        doctorRepository.findByDeletedTrue(Pageable.unpaged())
                .forEach(d -> out.add(toResponse("doctor", d.getId(), d.getName(), d.getEmail(), d.getCity(), d.getStatus(), d.getDeletedAt())));
        pharmacyRepository.findByDeletedTrue(Pageable.unpaged())
                .forEach(p -> out.add(toResponse("pharmacy", p.getId(), p.getName(), p.getEmail(), p.getCity(), p.getStatus(), p.getDeletedAt())));
        labRepository.findByDeletedTrue(Pageable.unpaged())
                .forEach(l -> out.add(toResponse("lab", l.getId(), l.getName(), l.getEmail(), l.getCity(), l.getStatus(), l.getDeletedAt())));
        out.sort(Comparator.comparing(DeletedProviderResponse::getDeletedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return out;
    }

    @Transactional(readOnly = true)
    public long count() {
        return doctorRepository.countByDeletedTrue()
                + pharmacyRepository.countByDeletedTrue()
                + labRepository.countByDeletedTrue();
    }

    @Transactional
    public void restoreDoctor(Long id) {
        providerDeletionService.restoreDoctor(id);
    }

    @Transactional
    public void restorePharmacy(Long id) {
        providerDeletionService.restorePharmacy(id);
    }

    @Transactional
    public void restoreLab(Long id) {
        providerDeletionService.restoreLab(id);
    }

    private static DeletedProviderResponse toResponse(
            String providerType,
            Long id,
            String name,
            String email,
            String city,
            ProviderStatus status,
            LocalDateTime deletedAt) {
        return DeletedProviderResponse.builder()
                .id(id)
                .providerType(providerType)
                .name(name)
                .email(email)
                .city(city)
                .status(status)
                .deletedAt(deletedAt)
                .build();
    }
}
