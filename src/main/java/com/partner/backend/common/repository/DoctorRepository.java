package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Doctor;
import com.partner.backend.common.entity.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByIdAndDeletedFalse(Long id);
    boolean existsByPmdcNumber(String pmdcNumber);
    long countByStatusAndDeletedFalse(ProviderStatus status);
    Page<Doctor> findByDeletedFalse(Pageable pageable);
    Page<Doctor> findByStatusAndDeletedFalse(ProviderStatus status, Pageable pageable);
    List<Doctor> findByStatusAndDeletedFalse(ProviderStatus status);

    Page<Doctor> findByDeletedTrue(Pageable pageable);
    Optional<Doctor> findByIdAndDeletedTrue(Long id);
    long countByDeletedTrue();

    @Query("SELECT d FROM Doctor d WHERE d.deleted = false AND (d.name LIKE %:query% OR d.specialty LIKE %:query% OR d.city LIKE %:query%)")
    Page<Doctor> search(String query, Pageable pageable);
}
