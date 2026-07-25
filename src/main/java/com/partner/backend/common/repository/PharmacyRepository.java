package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Pharmacy;
import com.partner.backend.common.entity.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByUserId(Long userId);
    Optional<Pharmacy> findByIdAndDeletedFalse(Long id);
    long countByStatusAndDeletedFalse(ProviderStatus status);
    Page<Pharmacy> findByDeletedFalse(Pageable pageable);
    Page<Pharmacy> findByStatusAndDeletedFalse(ProviderStatus status, Pageable pageable);
    List<Pharmacy> findByStatusAndDeletedFalse(ProviderStatus status);

    Page<Pharmacy> findByDeletedTrue(Pageable pageable);
    Optional<Pharmacy> findByIdAndDeletedTrue(Long id);
    long countByDeletedTrue();
}
