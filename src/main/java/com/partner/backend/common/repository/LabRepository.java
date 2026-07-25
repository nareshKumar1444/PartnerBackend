package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Lab;
import com.partner.backend.common.entity.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabRepository extends JpaRepository<Lab, Long> {
    Optional<Lab> findByUserId(Long userId);
    Optional<Lab> findByIdAndDeletedFalse(Long id);
    long countByStatusAndDeletedFalse(ProviderStatus status);
    Page<Lab> findByDeletedFalse(Pageable pageable);
    Page<Lab> findByStatusAndDeletedFalse(ProviderStatus status, Pageable pageable);
    List<Lab> findByStatusAndDeletedFalse(ProviderStatus status);

    Page<Lab> findByDeletedTrue(Pageable pageable);
    Optional<Lab> findByIdAndDeletedTrue(Long id);
    long countByDeletedTrue();
}
